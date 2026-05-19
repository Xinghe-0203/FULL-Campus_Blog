package com.example.edu_project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.entity.BlogCollect;
import com.example.edu_project.entity.BlogPost;
import com.example.edu_project.entity.BlogPostTag;
import com.example.edu_project.entity.BlogTag;
import com.example.edu_project.entity.SysUser;
import com.example.edu_project.common.enums.PostStatus;
import com.example.edu_project.event.CollectCreatedEvent;
import com.example.edu_project.mapper.BlogCollectMapper;
import com.example.edu_project.mapper.BlogPostMapper;
import com.example.edu_project.mapper.BlogPostTagMapper;
import com.example.edu_project.mapper.BlogTagMapper;
import com.example.edu_project.mapper.SysUserMapper;
import com.example.edu_project.service.BlogCollectService;
import com.example.edu_project.service.BlogPostService;
import com.example.edu_project.vo.CollectItemVO;
import com.example.edu_project.vo.CollectResultVO;
import com.example.edu_project.vo.CollectStatusVO;
import com.example.edu_project.utils.FineGrainedLockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 收藏服务实现类
 */
@Service
public class BlogCollectServiceImpl extends ServiceImpl<BlogCollectMapper, BlogCollect> implements BlogCollectService {

    @Autowired
    private BlogPostMapper blogPostMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BlogPostTagMapper blogPostTagMapper;

    @Autowired
    private BlogTagMapper blogTagMapper;

    @Autowired
    private BlogPostService blogPostService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private BlogCollectMapper blogCollectMapper;

    private final FineGrainedLockManager lockManager = FineGrainedLockManager.getInstance();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectResultVO toggleCollect(Long postId, Long userId) {
        // 检查文章是否存在
        BlogPost post = blogPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "文章不存在");
        }

        CollectResultVO result = new CollectResultVO();

        // 使用细粒度锁：同一用户对同一文章的收藏操作串行执行
        String lockKey = userId + "-" + postId;
        synchronized (lockManager.getLock(lockKey)) {
            // 检查是否存在非删除的收藏记录
            LambdaQueryWrapper<BlogCollect> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(BlogCollect::getUserId, userId)
                  .eq(BlogCollect::getPostId, postId)
                  .ne(BlogCollect::getIsDeleted, 1);
            BlogCollect activeCollect = this.getOne(activeWrapper);

            if (activeCollect != null) {
                // 取消收藏：逻辑删除记录（解决软删除+唯一约束冲突）
                blogCollectMapper.logicalDeleteById(activeCollect.getId());
                blogPostService.decrementCollectCount(postId);
                result.setAction("uncollect");
            } else {
                // 检查是否存在已软删除的记录（重新收藏时恢复）
                LambdaQueryWrapper<BlogCollect> deletedWrapper = new LambdaQueryWrapper<>();
                deletedWrapper.eq(BlogCollect::getUserId, userId)
                      .eq(BlogCollect::getPostId, postId)
                      .eq(BlogCollect::getIsDeleted, 1);
                BlogCollect deletedCollect = this.getOne(deletedWrapper);

                if (deletedCollect != null) {
                    deletedCollect.setIsDeleted(0);
                    this.updateById(deletedCollect);
                    blogPostService.incrementCollectCount(postId);
                    result.setAction("collect");
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            eventPublisher.publishEvent(new CollectCreatedEvent(userId, post.getUserId(), postId, post.getTitle()));
                        }
                    });
                } else {
                    // 首次收藏
                    BlogCollect newCollect = new BlogCollect();
                    newCollect.setUserId(userId);
                    newCollect.setPostId(postId);
                    try {
                        this.save(newCollect);
                        blogPostService.incrementCollectCount(postId);
                        result.setAction("collect");
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                eventPublisher.publishEvent(new CollectCreatedEvent(userId, post.getUserId(), postId, post.getTitle()));
                            }
                        });
                    } catch (DuplicateKeyException e) {
                        LambdaQueryWrapper<BlogCollect> concurrentWrapper = new LambdaQueryWrapper<>();
                        concurrentWrapper.eq(BlogCollect::getUserId, userId)
                              .eq(BlogCollect::getPostId, postId)
                              .ne(BlogCollect::getIsDeleted, 1);
                        BlogCollect concurrentCollect = this.getOne(concurrentWrapper);
                        if (concurrentCollect != null) {
                            blogCollectMapper.logicalDeleteById(concurrentCollect.getId());
                            blogPostService.decrementCollectCount(postId);
                            result.setAction("uncollect");
                        } else {
                            blogPostService.incrementCollectCount(postId);
                            result.setAction("collect");
                        }
                    }
                }
            }

            // 获取实时收藏数，避免基于缓存值+1/-1计算的TOCTOU风险
            BlogPost updatedPost = blogPostMapper.selectById(postId);
            int realTimeCount = updatedPost != null && updatedPost.getCollectCount() != null ? updatedPost.getCollectCount() : 0;
            result.setCollectCount(realTimeCount);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectStatusVO checkCollectStatus(Long postId, Long userId) {
        CollectStatusVO status = new CollectStatusVO();
        if (userId == null) {
            status.setCollected(false);
            status.setCollectCount(0);
            return status;
        }

        LambdaQueryWrapper<BlogCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogCollect::getUserId, userId)
              .eq(BlogCollect::getPostId, postId)
              .ne(BlogCollect::getIsDeleted, 1);
        status.setCollected(this.count(wrapper) > 0);

        BlogPost post = blogPostMapper.selectById(postId);
        status.setCollectCount(post != null && post.getCollectCount() != null ? post.getCollectCount() : 0);
        return status;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<CollectItemVO> getMyCollections(Long userId, Integer page, Integer pageSize) {
        // 分页查询收藏记录
        Page<BlogCollect> collectPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<BlogCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogCollect::getUserId, userId)
              .ne(BlogCollect::getIsDeleted, 1)
              .orderByDesc(BlogCollect::getCreateTime);
        IPage<BlogCollect> collectResult = this.page(collectPage, wrapper);

        // 如果没有收藏记录
        if (collectResult.getRecords().isEmpty()) {
            return new Page<>(page, pageSize, 0);
        }

        // 获取所有相关文章ID
        List<Long> postIds = collectResult.getRecords().stream()
                .map(BlogCollect::getPostId)
                .collect(Collectors.toList());

        // 批量查询文章
        List<BlogPost> posts = blogPostMapper.selectBatchIds(postIds);
        // 过滤掉未发布和已删除的文章
        posts = posts.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == PostStatus.PUBLISHED.getValue())
                .filter(p -> p.getIsDeleted() == null || p.getIsDeleted() != 1)
                .collect(Collectors.toList());
        Map<Long, BlogPost> postMap = posts.stream()
                .collect(Collectors.toMap(BlogPost::getId, p -> p, (a, b) -> a));

        // 获取所有作者ID
        List<Long> userIds = posts.stream()
                .map(BlogPost::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        // 获取所有文章标签
        LambdaQueryWrapper<BlogPostTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.in(BlogPostTag::getPostId, postIds);
        List<BlogPostTag> postTags = blogPostTagMapper.selectList(tagWrapper);

        // 获取标签详情
        List<Long> tagIds = postTags.stream()
                .map(BlogPostTag::getTagId)
                .distinct()
                .collect(Collectors.toList());
        List<BlogTag> tags = tagIds.isEmpty() ? List.of() : blogTagMapper.selectBatchIds(tagIds);
        Map<Long, String> tagNameMap = tags.stream()
                .collect(Collectors.toMap(BlogTag::getId, BlogTag::getName, (a, b) -> a));

        // 按文章分组标签
        Map<Long, List<String>> postTagsMap = postTags.stream()
                .collect(Collectors.groupingBy(
                        BlogPostTag::getPostId,
                        Collectors.mapping(pt -> tagNameMap.get(pt.getTagId()), Collectors.toList())
                ));

        // 构建返回结果
        IPage<CollectItemVO> resultPage = new Page<>(
                collectResult.getCurrent(),
                collectResult.getSize(),
                collectResult.getTotal()
        );

        List<CollectItemVO> items = collectResult.getRecords().stream()
                .map(collect -> {
                    CollectItemVO item = new CollectItemVO();
                    item.setCollectId(collect.getId());
                    item.setCollectTime(collect.getCreateTime());

                    BlogPost post = postMap.get(collect.getPostId());
                    if (post != null) {
                        item.setPostId(post.getId());
                        item.setTitle(post.getTitle());
                        item.setSummary(post.getSummary());
                        item.setCategory(post.getCategory());
                        item.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0L);
                        item.setLikeCount(post.getLikeCount());
                        item.setCommentCount(post.getCommentCount());
                        item.setCollectCount(post.getCollectCount());

                        SysUser author = userMap.get(post.getUserId());
                        if (author != null) {
                            item.setAuthorNickname(author.getNickname());
                        }

                        item.setTags(postTagsMap.getOrDefault(post.getId(), List.of()));
                    } else {
                        item.setPostId(collect.getPostId());
                        item.setTitle("文章已删除");
                    }
                    return item;
                })
                .collect(Collectors.toList());

        resultPage.setRecords(items);
        return resultPage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Boolean> checkCollectStatusBatch(List<Long> postIds, Long userId) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<BlogCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogCollect::getUserId, userId)
               .in(BlogCollect::getPostId, postIds)
               .ne(BlogCollect::getIsDeleted, 1);
        List<BlogCollect> collectedList = this.list(wrapper);
        List<Long> collectedPostIds = collectedList.stream()
                .map(BlogCollect::getPostId)
                .collect(Collectors.toList());

        return postIds.stream()
                .map(postId -> collectedPostIds.contains(postId))
                .collect(Collectors.toList());
    }
}
