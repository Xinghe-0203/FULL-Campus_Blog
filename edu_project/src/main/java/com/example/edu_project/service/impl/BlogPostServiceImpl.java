package com.example.edu_project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.dto.AdminPostQueryRequest;
import com.example.edu_project.dto.PostAdvancedSearchRequest;
import com.example.edu_project.dto.PostCreateRequest;
import com.example.edu_project.dto.PostQueryRequest;
import com.example.edu_project.dto.SaveDraftRequest;
import com.example.edu_project.entity.BlogCollect;
import com.example.edu_project.entity.BlogComment;
import com.example.edu_project.entity.BlogDraft;
import com.example.edu_project.entity.BlogLike;
import com.example.edu_project.entity.BlogPost;
import com.example.edu_project.entity.BlogPostTag;
import com.example.edu_project.entity.BlogTag;
import com.example.edu_project.entity.SysUser;
import com.example.edu_project.mapper.BlogCollectMapper;
import com.example.edu_project.mapper.BlogCommentMapper;
import com.example.edu_project.mapper.BlogDraftMapper;
import com.example.edu_project.mapper.BlogLikeMapper;
import com.example.edu_project.mapper.BlogPostMapper;
import com.example.edu_project.mapper.BlogPostTagMapper;
import com.example.edu_project.mapper.BlogTagMapper;
import com.example.edu_project.mapper.SysUserMapper;
import com.example.edu_project.entity.Topic;
import com.example.edu_project.service.BlogPostService;
import com.example.edu_project.service.BlogTagService;
import com.example.edu_project.service.NotificationService;
import com.example.edu_project.service.TopicService;
import com.example.edu_project.service.TrendingService;
import com.example.edu_project.config.CaffeineCacheConfig;
import com.example.edu_project.utils.HtmlSanitizer;
import com.example.edu_project.utils.SecurityUtils;
import com.example.edu_project.vo.PostDetailResponse;
import com.example.edu_project.vo.PostListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 */
@Slf4j
@Service
public class BlogPostServiceImpl extends ServiceImpl<BlogPostMapper, BlogPost> implements BlogPostService {

    @Autowired
    private BlogPostTagMapper blogPostTagMapper;

    @Autowired
    private BlogTagMapper blogTagMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BlogCommentMapper blogCommentMapper;

    @Autowired
    private BlogLikeMapper blogLikeMapper;

    @Autowired
    private BlogCollectMapper blogCollectMapper;

    @Autowired
    private HtmlSanitizer htmlSanitizer;

    @Autowired
    private BlogDraftMapper blogDraftMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TrendingService trendingService;

    @Autowired
    private BlogTagService blogTagService;

    @Autowired
    private TopicService topicService;

    private static final int MAX_VIEW_COUNT_CACHE_SIZE = 10000;
    private final ConcurrentMap<String, AtomicLong> viewCountCache = new ConcurrentHashMap<>();
    private static final long VIEW_COUNT_INTERVAL_MS = 60000;
    private static final long CACHE_EXPIRE_MS = 3600000;

    private String getUserIdentifier(Long userId, String ip, String userAgent) {
        if (userId != null) {
            return "user-" + userId;
        }
        if (ip == null || ip.isEmpty()) {
            ip = "unknown";
        }
        if (userAgent == null || userAgent.isEmpty()) {
            userAgent = "unknown";
        }
        return "guest-" + ip + "-" + userAgent.hashCode();
    }

    private void cleanupViewCountCache() {
        long now = System.currentTimeMillis();
        viewCountCache.entrySet().removeIf(entry ->
            entry.getValue().get() < now - CACHE_EXPIRE_MS
        );
    }

    @Override
    @CacheEvict(value = CaffeineCacheConfig.TRENDING_CACHE, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostCreateRequest request, Long userId, boolean isAdmin) {
        // 参数校验
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "文章标题不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "文章内容不能为空");
        }
        if (request.getTitle().length() > 200) {
            throw new BusinessException(400, "文章标题不能超过200字符");
        }
        if (request.getContent() != null && request.getContent().length() > 50000) {
            throw new BusinessException(400, "文章内容不能超过50000字符");
        }

        // XSS 防护：对用户输入进行 HTML 过滤
        String sanitizedTitle = htmlSanitizer.sanitizeRichText(request.getTitle());
        String sanitizedSummary = htmlSanitizer.sanitizeRichText(request.getSummary());
        String sanitizedContent = htmlSanitizer.sanitizeMarkdown(request.getContent());

        // 处理标签：优先使用 tagIds，若为空则尝试从 tagNames 转换
        List<Long> tagIds = resolveTagIds(request.getTagIds(), request.getTagNames());
        if (tagIds != null && !tagIds.isEmpty()) {
            validateTagIds(tagIds);
        }

        // 创建文章
        BlogPost post = new BlogPost();
        post.setUserId(userId);
        post.setTitle(sanitizedTitle);
        post.setSummary(sanitizedSummary);
        post.setContent(sanitizedContent);
        post.setCategory(request.getCategory() != null ? htmlSanitizer.sanitizePlainText(request.getCategory()) : "默认分类");
        post.setViewCount(0L);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        // 管理员和普通用户均可直接发布，无需审核
        post.setStatus(1);
        post.setCoverUrl(request.getCoverImage());
        post.setTopicId(request.getTopicId());

        this.save(post);

        // 保存标签关联
        if (tagIds != null && !tagIds.isEmpty()) {
            savePostTags(post.getId(), tagIds);
        }

        log.info("文章创建成功: postId={}, userId={}, title={}", post.getId(), userId, post.getTitle());
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostCreateRequest request, Long userId) {
        return createPost(request, userId, false);
    }

    @Override
    @CacheEvict(value = CaffeineCacheConfig.TRENDING_CACHE, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(PostCreateRequest request, Long userId, boolean isAdmin, boolean isPostAuthor) {
        // 参数校验
        if (request.getId() == null) {
            throw new BusinessException(400, "文章ID不能为空");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "文章标题不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "文章内容不能为空");
        }
        if (request.getTitle().length() > 200) {
            throw new BusinessException(400, "文章标题不能超过200字符");
        }
        if (request.getContent() != null && request.getContent().length() > 50000) {
            throw new BusinessException(400, "文章内容不能超过50000字符");
        }

        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        // XSS 防护：对用户输入进行 HTML 过滤
        String sanitizedTitle = htmlSanitizer.sanitizeRichText(request.getTitle());
        String sanitizedSummary = htmlSanitizer.sanitizeRichText(request.getSummary());
        String sanitizedContent = htmlSanitizer.sanitizeMarkdown(request.getContent());

        // 处理标签：优先使用 tagIds，若为空则尝试从 tagNames 转换
        List<Long> tagIds = resolveTagIds(request.getTagIds(), request.getTagNames());
        if (tagIds != null && !tagIds.isEmpty()) {
            validateTagIds(tagIds);
        }

        BlogPost post = this.getById(request.getId());
        if (post == null) {
            throw new BusinessException(404, "文章不存在");
        }
        // 检查权限：作者本人或管理员可以修改
        if (!Objects.equals(userId, post.getUserId()) && !isAdmin) {
            throw new BusinessException(403, "无权修改此文章");
        }

        post.setTitle(sanitizedTitle);
        post.setSummary(sanitizedSummary);
        post.setContent(sanitizedContent);
        if (request.getCategory() != null) {
            post.setCategory(htmlSanitizer.sanitizePlainText(request.getCategory()));
        }
        post.setCoverUrl(request.getCoverImage());
        if (request.getTopicId() != null) {
            post.setTopicId(request.getTopicId());
        }

        // 编辑文章后保持已发布状态（无需重新审核）
        if (post.getStatus() == null) {
            post.setStatus(1);
        }

        this.updateById(post);

        // 更新标签关联 - 先删后插，保证事务原子性
        LambdaQueryWrapper<BlogPostTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogPostTag::getPostId, post.getId());
        blogPostTagMapper.delete(wrapper);

        if (tagIds != null && !tagIds.isEmpty()) {
            savePostTags(post.getId(), tagIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(PostCreateRequest request, Long userId, boolean isAdmin) {
        updatePost(request, userId, isAdmin, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(PostCreateRequest request, Long userId) {
        updatePost(request, userId, false, false);
    }

    @Override
    @CacheEvict(value = CaffeineCacheConfig.TRENDING_CACHE, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        BlogPost post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(404, "文章不存在");
        }
        // 检查是否已被删除
        if (post.getIsDeleted() != null && post.getIsDeleted() == 1) {
            throw new BusinessException(404, "文章不存在");
        }
        // 检查权限：作者本人或管理员可以删除
        if (!Objects.equals(userId, post.getUserId()) && !isAdmin) {
            throw new BusinessException(403, "无权删除此文章");
        }
        // 作者删除时，如果文章在待审核状态，禁止删除
        // DEAD CODE: createPost always sets status=1, so status==0 never occurs
        if (!isAdmin && post.getStatus() != null && post.getStatus() == 0) {
            throw new BusinessException(400, "待审核文章不能被删除，请等待审核结果");
        }

        // 删除文章
        this.removeById(postId);

        // 删除标签关联（标签是共享资源，文章删除后关联关系需要清除）
        LambdaQueryWrapper<BlogPostTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogPostTag::getPostId, postId);
        blogPostTagMapper.delete(wrapper);

        log.info("文章删除成功: postId={}, userId={}, title={}", postId, userId, post.getTitle());
        // 保留关联数据（评论、点赞、收藏），
        // 评论/点赞/收藏保留便于数据恢复或审计，关联的文章ID在展示时做判断即可
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId) {
        deletePost(postId, userId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long postId) {
        BlogPost post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(404, "文章不存在");
        }
        if (post.getIsDeleted() != null && post.getIsDeleted() == 1) {
            throw new BusinessException(404, "文章不存在");
        }

        Long currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        boolean isAuthor = currentUserId != null && currentUserId.equals(post.getUserId());

        if (post.getStatus() == null || post.getStatus() != 1) {
            if (!isAuthor) {
                throw new BusinessException(404, "文章不存在");
            }
        }

        PostDetailResponse response = new PostDetailResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setTitle(post.getTitle());
        response.setSummary(post.getSummary());
        response.setContent(post.getContent());
        response.setCategory(post.getCategory());
        response.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0L);
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setCollectCount(post.getCollectCount());
        response.setStatus(post.getStatus());
        response.setCreateTime(post.getCreateTime());
        response.setUpdateTime(post.getUpdateTime());
        response.setCoverImage(post.getCoverUrl());

        // 获取作者信息
        SysUser user = sysUserMapper.selectById(post.getUserId());
        if (user != null) {
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
        }

        // 获取标签列表
        response.setTags(getTagsByPostId(post.getId()));

        // 获取话题信息
        response.setTopicId(post.getTopicId());
        if (post.getTopicId() != null) {
            try {
                Topic topic = topicService.getTopicById(post.getTopicId());
                response.setTopicName(topic.getName());
            } catch (BusinessException e) {
                response.setTopicName(null);
            }
        }

        return response;
    }

    @Override
    @Cacheable(value = CaffeineCacheConfig.TRENDING_CACHE,
            key = "'postList:' + #request.pageNum + ':' + #request.pageSize + ':' + #request.category + ':' + #request.sort")
    @Transactional(readOnly = true)
    public IPage<PostListResponse> getPostList(PostQueryRequest request) {
        Page<BlogPost> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogPost::getStatus, 1) // 只查询已发布的文章
                .ne(BlogPost::getIsDeleted, 1); // 排除已删除的文章

        // 关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(BlogPost::getTitle, request.getKeyword())
                    .or()
                    .like(BlogPost::getContent, request.getKeyword()));
        }

        // 分类筛选
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            wrapper.eq(BlogPost::getCategory, request.getCategory());
        }

        // 作者筛选
        if (request.getUserId() != null) {
            wrapper.eq(BlogPost::getUserId, request.getUserId());
        }

        // 标签筛选
        if (request.getTagId() != null) {
            // 查询指定标签关联的文章ID列表
            LambdaQueryWrapper<BlogPostTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(BlogPostTag::getTagId, request.getTagId());
            List<BlogPostTag> taggedPosts = blogPostTagMapper.selectList(tagWrapper);
            if (taggedPosts.isEmpty()) {
                // 没有文章匹配该标签，返回空结果
                return new Page<>(request.getPageNum(), request.getPageSize(), 0);
            }
            List<Long> taggedPostIds = taggedPosts.stream()
                    .map(BlogPostTag::getPostId)
                    .collect(Collectors.toList());
            wrapper.in(BlogPost::getId, taggedPostIds);
        }

        // 排序
        if (request.getSort() != null) {
            switch (request.getSort()) {
                case "hot":
                    wrapper.orderByDesc(BlogPost::getViewCount);
                    break;
                case "essence":
                    wrapper.orderByDesc(BlogPost::getLikeCount);
                    break;
                default:
                    wrapper.orderByDesc(BlogPost::getCreateTime);
                    break;
            }
        } else {
            wrapper.orderByDesc(BlogPost::getCreateTime);
        }

        IPage<BlogPost> postPage = this.page(page, wrapper);

        // 批量获取用户信息和标签信息，避免 N+1 查询
        List<BlogPost> posts = postPage.getRecords();
        if (posts.isEmpty()) {
            return new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        }

        // 收集所有需要的用户ID
        List<Long> userIds = posts.stream()
                .map(BlogPost::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 收集所有文章ID
        List<Long> postIds = posts.stream()
                .map(BlogPost::getId)
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        // 批量查询标签信息
        Map<Long, List<PostDetailResponse.TagVO>> postTagsMap = getTagsMapByPostIds(postIds);

        // 批量查询话题名称
        Map<Long, String> topicNameMap = getTopicNamesMapByPostIds(postIds, posts);

        // 转换为列表响应
        IPage<PostListResponse> result = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        result.setRecords(posts.stream()
                .map(post -> convertToListResponse(post, userMap.get(post.getUserId()), postTagsMap.get(post.getId()), topicNameMap.get(post.getTopicId())))
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long postId, String userKey) {
        if (viewCountCache.size() >= MAX_VIEW_COUNT_CACHE_SIZE) {
            cleanupViewCountCache();
        }
        String cacheKey = userKey + "-" + postId;
        long now = System.currentTimeMillis();
        AtomicLong lastViewTime = viewCountCache.computeIfAbsent(cacheKey, k -> new AtomicLong(0));

        while (true) {
            long lastTime = lastViewTime.get();
            if (now - lastTime < VIEW_COUNT_INTERVAL_MS) {
                return;
            }
            if (lastViewTime.compareAndSet(lastTime, now)) {
                break;
            }
        }

        cleanupViewCountCache();
        baseMapper.incrementViewCount(postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long postId) {
        baseMapper.incrementViewCount(postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementLikeCount(Long postId) {
        baseMapper.incrementLikeCount(postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementLikeCount(Long postId) {
        baseMapper.decrementLikeCount(postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementCommentCount(Long postId) {
        baseMapper.incrementCommentCount(postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementCommentCount(Long postId, int count) {
        baseMapper.decrementCommentCount(postId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementCollectCount(Long postId) {
        baseMapper.incrementCollectCount(postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementCollectCount(Long postId) {
        baseMapper.decrementCollectCount(postId);
    }

    private void savePostTags(Long postId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        blogPostTagMapper.batchInsertPostTags(postId, tagIds);
    }

    private void validateTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<BlogTag> existingTags = blogTagMapper.selectBatchIds(tagIds);
        if (existingTags.size() != tagIds.size()) {
            throw new BusinessException(400, "部分标签ID不存在");
        }
    }

    private List<Long> resolveTagIds(List<Long> tagIds, List<String> tagNames) {
        if (tagIds != null && !tagIds.isEmpty()) {
            return tagIds;
        }
        if (tagNames == null || tagNames.isEmpty()) {
            return null;
        }
        return tagNames.stream()
                .map(name -> blogTagService.getOrCreateTag(name))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Map<Long, String> getTopicNamesMapByPostIds(List<Long> postIds, List<BlogPost> posts) {
        Map<Long, String> map = new HashMap<>();
        List<Long> topicIds = posts.stream()
                .map(BlogPost::getTopicId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (topicIds.isEmpty()) {
            return map;
        }
        List<Topic> topics = topicService.listByIds(topicIds);
        return topics.stream().collect(Collectors.toMap(Topic::getId, Topic::getName, (a, b) -> a));
    }

    @Cacheable(value = CaffeineCacheConfig.CATEGORY_CACHE, key = "'postTags:' + #postIds.hashCode()")
    public Map<Long, List<PostDetailResponse.TagVO>> getTagsMapByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 查询所有相关的文章-标签关联
        LambdaQueryWrapper<BlogPostTag> postTagWrapper = new LambdaQueryWrapper<>();
        postTagWrapper.in(BlogPostTag::getPostId, postIds);
        List<BlogPostTag> postTags = blogPostTagMapper.selectList(postTagWrapper);

        if (postTags == null || postTags.isEmpty()) {
            return Collections.emptyMap();
        }

        // 收集所有标签ID
        List<Long> tagIds = postTags.stream()
                .map(BlogPostTag::getTagId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询标签信息
        List<BlogTag> tags = blogTagMapper.selectBatchIds(tagIds);
        Map<Long, String> tagNameMap = tags.stream()
                .collect(Collectors.toMap(BlogTag::getId, BlogTag::getName, (a, b) -> a));

        // 按文章ID分组
        return postTags.stream()
                .collect(Collectors.groupingBy(
                        BlogPostTag::getPostId,
                        Collectors.mapping(tag -> {
                            PostDetailResponse.TagVO tagVO = new PostDetailResponse.TagVO();
                            tagVO.setId(tag.getTagId());
                            tagVO.setName(tagNameMap.get(tag.getTagId()));
                            return tagVO;
                        }, Collectors.toList())
                ));
    }

    private PostListResponse convertToListResponse(BlogPost post, SysUser user, List<PostDetailResponse.TagVO> tags, String topicName) {
        PostListResponse response = new PostListResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setTitle(post.getTitle());
        response.setSummary(post.getSummary());
        response.setCategory(post.getCategory());
        response.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0L);
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setCollectCount(post.getCollectCount());
        response.setShareCount(post.getShareCount());
        response.setCreateTime(post.getCreateTime());
        response.setCoverImage(post.getCoverUrl());
        response.setTopicId(post.getTopicId());
        response.setTopicName(topicName);

        // 使用预获取的作者信息
        if (user != null) {
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
        }

        // 使用预获取的标签信息
        if (tags != null) {
            response.setTags(tags.stream()
                    .map(tag -> {
                        PostListResponse.TagVO tagVO = new PostListResponse.TagVO();
                        tagVO.setId(tag.getId());
                        tagVO.setName(tag.getName());
                        return tagVO;
                    })
                    .collect(Collectors.toList()));
        } else {
            response.setTags(Collections.emptyList());
        }

        return response;
    }

    @Cacheable(value = CaffeineCacheConfig.CATEGORY_CACHE, key = "'tagsByPost:' + #postId")
    public List<PostDetailResponse.TagVO> getTagsByPostId(Long postId) {
        LambdaQueryWrapper<BlogPostTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogPostTag::getPostId, postId);
        List<BlogPostTag> postTags = blogPostTagMapper.selectList(wrapper);

        if (postTags == null || postTags.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> tagIds = postTags.stream()
                .map(BlogPostTag::getTagId)
                .collect(Collectors.toList());

        List<BlogTag> tags = blogTagMapper.selectBatchIds(tagIds);

        return tags.stream()
                .map(tag -> {
                    PostDetailResponse.TagVO tagVO = new PostDetailResponse.TagVO();
                    tagVO.setId(tag.getId());
                    tagVO.setName(tag.getName());
                    return tagVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(Long userId, SaveDraftRequest request) {
        // XSS 防护
        String sanitizedTitle = request.getTitle() != null ? htmlSanitizer.sanitizeRichText(request.getTitle()) : null;
        String sanitizedSummary = request.getSummary() != null ? htmlSanitizer.sanitizeRichText(request.getSummary()) : null;
        String sanitizedContent = request.getContent() != null ? htmlSanitizer.sanitizeMarkdown(request.getContent()) : null;
        String sanitizedCategory = request.getCategory() != null ? htmlSanitizer.sanitizePlainText(request.getCategory()) : null;
        String sanitizedCoverImage = request.getCoverImage() != null ? htmlSanitizer.sanitizePlainText(request.getCoverImage()) : null;

        // 处理标签：优先使用 tagIds，若为空则尝试从 tagNames 转换
        List<Long> mergedTagIds = resolveTagIds(request.getTagIds(), request.getTagNames());
        if (mergedTagIds != null && !mergedTagIds.isEmpty()) {
            validateTagIds(mergedTagIds);
        }

        // 将 tagIds 列表转换为逗号分隔字符串
        String tagIdsStr = null;
        if (mergedTagIds != null && !mergedTagIds.isEmpty()) {
            tagIdsStr = mergedTagIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

        // 如果指定了 draftId，则更新指定草稿
        if (request.getDraftId() != null) {
            BlogDraft targetDraft = blogDraftMapper.selectById(request.getDraftId());
            if (targetDraft == null) {
                throw new BusinessException(404, "草稿不存在");
            }
            if (!Objects.equals(userId, targetDraft.getUserId())) {
                throw new BusinessException(403, "无权修改此草稿");
            }
            targetDraft.setTitle(sanitizedTitle);
            targetDraft.setContent(sanitizedContent);
            targetDraft.setSummary(sanitizedSummary);
            targetDraft.setCategory(sanitizedCategory);
            targetDraft.setTagIds(tagIdsStr);
            targetDraft.setCoverImage(sanitizedCoverImage);
            targetDraft.setPostId(request.getPostId());
            targetDraft.setTopicId(request.getTopicId());
            blogDraftMapper.updateById(targetDraft);
            return targetDraft.getId();
        }

        // 如果指定了 postId，先查找对应文章的草稿
        if (request.getPostId() != null) {
            LambdaQueryWrapper<BlogDraft> postWrapper = new LambdaQueryWrapper<>();
            postWrapper.eq(BlogDraft::getUserId, userId)
                    .eq(BlogDraft::getPostId, request.getPostId());
            BlogDraft existingByPost = blogDraftMapper.selectOne(postWrapper);
            if (existingByPost != null) {
                existingByPost.setTitle(sanitizedTitle);
                existingByPost.setContent(sanitizedContent);
                existingByPost.setSummary(sanitizedSummary);
                existingByPost.setCategory(sanitizedCategory);
                existingByPost.setTagIds(tagIdsStr);
                existingByPost.setCoverImage(sanitizedCoverImage);
                existingByPost.setPostId(request.getPostId());
                existingByPost.setTopicId(request.getTopicId());
                blogDraftMapper.updateById(existingByPost);
                return existingByPost.getId();
            }
        }

        // 创建新草稿
        BlogDraft draft = new BlogDraft();
        draft.setUserId(userId);
        draft.setTitle(sanitizedTitle);
        draft.setContent(sanitizedContent);
        draft.setSummary(sanitizedSummary);
        draft.setCategory(sanitizedCategory);
        draft.setTagIds(tagIdsStr);
        draft.setCoverImage(sanitizedCoverImage);
        draft.setPostId(request.getPostId());
        draft.setTopicId(request.getTopicId());
        blogDraftMapper.insert(draft);
        return draft.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public SaveDraftRequest getLatestDraft(Long userId) {
        LambdaQueryWrapper<BlogDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogDraft::getUserId, userId)
                .orderByDesc(BlogDraft::getUpdateTime)
                .last("LIMIT 1");
        BlogDraft draft = blogDraftMapper.selectOne(wrapper);

        if (draft == null) {
            return null;
        }

        return convertToSaveDraftRequest(draft);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long draftId, Long userId, boolean isAdmin) {
        BlogDraft draft = blogDraftMapper.selectById(draftId);
        if (draft == null) {
            throw new BusinessException(404, "草稿不存在");
        }
        // 检查权限：只能删除自己的草稿，管理员除外
        if (!Objects.equals(userId, draft.getUserId()) && !isAdmin) {
            throw new BusinessException(403, "无权删除此草稿");
        }

        // 逻辑删除
        blogDraftMapper.deleteById(draftId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long draftId, Long userId) {
        deleteDraft(draftId, userId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public SaveDraftRequest getDraft(Long draftId, Long userId) {
        BlogDraft draft = blogDraftMapper.selectById(draftId);
        if (draft == null) {
            throw new BusinessException(404, "草稿不存在");
        }
        // 检查权限：只能查看自己的草稿
        if (!Objects.equals(userId, draft.getUserId())) {
            throw new BusinessException(403, "无权查看此草稿");
        }

        return convertToSaveDraftRequest(draft);
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<PostListResponse> advancedSearch(PostAdvancedSearchRequest request) {
        // Check if this is a keyword-only search (no category/userId/tagId filters)
        boolean isKeywordOnlySearch = request.getKeyword() != null
                && !request.getKeyword().trim().isEmpty()
                && (request.getCategory() == null || request.getCategory().trim().isEmpty())
                && request.getUserId() == null
                && request.getTagId() == null;

        // For keyword-only search, use MySQL full-text search for better performance
        if (isKeywordOnlySearch) {
            return advancedSearchFullText(request);
        }

        // For searches with additional filters, use traditional LIKE-based query
        return advancedSearchLike(request);
    }

    /**
     * Advanced search using MySQL full-text search (MATCH AGAINST)
     * Used when only keyword filtering is needed (no category/user/tag filters)
     */
    private IPage<PostListResponse> advancedSearchFullText(PostAdvancedSearchRequest request) {
        int pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        int offset = (pageNum - 1) * pageSize;
        String keyword = request.getKeyword().trim();

        // Determine sort order and call the appropriate method
        String sortBy = request.getSortBy();
        List<Long> postIds;
        if ("view".equalsIgnoreCase(sortBy)) {
            postIds = baseMapper.fullTextSearchByView(keyword, 1, 0, offset, pageSize);
        } else if ("like".equalsIgnoreCase(sortBy)) {
            postIds = baseMapper.fullTextSearchByLike(keyword, 1, 0, offset, pageSize);
        } else {
            postIds = baseMapper.fullTextSearch(keyword, 1, 0, offset, pageSize);
        }
        Long total = baseMapper.countFullTextSearch(keyword, 1, 0);

        if (postIds.isEmpty()) {
            return new Page<>(pageNum, pageSize, total != null ? total : 0);
        }

        // Fetch posts by IDs to maintain order from full-text search
        List<BlogPost> posts = this.list(new LambdaQueryWrapper<BlogPost>()
                .in(BlogPost::getId, postIds)
                .eq(BlogPost::getStatus, 1)
                .ne(BlogPost::getIsDeleted, 1));

        // Sort posts to match full-text search relevance order
        Map<Long, Integer> orderMap = new java.util.HashMap<>();
        for (int i = 0; i < postIds.size(); i++) {
            orderMap.put(postIds.get(i), i);
        }
        posts.sort((a, b) -> {
            Integer orderA = orderMap.getOrDefault(a.getId(), Integer.MAX_VALUE);
            Integer orderB = orderMap.getOrDefault(b.getId(), Integer.MAX_VALUE);
            return orderA.compareTo(orderB);
        });

        // Build user and tag maps
        List<Long> userIds = posts.stream().map(BlogPost::getUserId).distinct().collect(Collectors.toList());
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));
        Map<Long, List<PostDetailResponse.TagVO>> postTagsMap = getTagsMapByPostIds(postIds);
        Map<Long, String> topicNameMap = getTopicNamesMapByPostIds(postIds, posts);

        IPage<PostListResponse> result = new Page<>(pageNum, pageSize, total != null ? total : 0);
        result.setRecords(posts.stream()
                .map(post -> convertToListResponse(post, userMap.get(post.getUserId()), postTagsMap.get(post.getId()), topicNameMap.get(post.getTopicId())))
                .collect(Collectors.toList()));

        return result;
    }

    /**
     * Advanced search using traditional LIKE-based query
     * Used when additional filters (category/user/tag) are needed along with keyword
     */
    private IPage<PostListResponse> advancedSearchLike(PostAdvancedSearchRequest request) {
        Page<BlogPost> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogPost::getStatus, 1) // 只查询已发布的文章
                .ne(BlogPost::getIsDeleted, 1); // 排除已删除的文章

        // 关键词搜索 - 使用 LIKE 而不是 FULLTEXT 以支持与其他过滤条件组合
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w.like(BlogPost::getTitle, request.getKeyword())
                    .or()
                    .like(BlogPost::getContent, request.getKeyword()));
        }

        // 分类筛选
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            wrapper.eq(BlogPost::getCategory, request.getCategory());
        }

        // 作者筛选
        if (request.getUserId() != null) {
            wrapper.eq(BlogPost::getUserId, request.getUserId());
        }

        // 标签筛选
        if (request.getTagId() != null) {
            LambdaQueryWrapper<BlogPostTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(BlogPostTag::getTagId, request.getTagId());
            List<BlogPostTag> taggedPosts = blogPostTagMapper.selectList(tagWrapper);
            if (taggedPosts.isEmpty()) {
                return new Page<>(request.getPageNum(), request.getPageSize(), 0);
            }
            List<Long> taggedPostIds = taggedPosts.stream()
                    .map(BlogPostTag::getPostId)
                    .collect(Collectors.toList());
            wrapper.in(BlogPost::getId, taggedPostIds);
        }

        // 排序
        String sortBy = request.getSortBy();
        if ("view".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(BlogPost::getViewCount);
        } else if ("like".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(BlogPost::getLikeCount);
        } else {
            // 默认按时间
            wrapper.orderByDesc(BlogPost::getCreateTime);
        }

        IPage<BlogPost> postPage = this.page(page, wrapper);

        List<BlogPost> posts = postPage.getRecords();
        if (posts.isEmpty()) {
            return new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        }

        // 批量获取用户信息和标签信息
        List<Long> userIds = posts.stream()
                .map(BlogPost::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> postIds = posts.stream()
                .map(BlogPost::getId)
                .collect(Collectors.toList());

        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));
        Map<Long, List<PostDetailResponse.TagVO>> postTagsMap = getTagsMapByPostIds(postIds);
        Map<Long, String> topicNameMap = getTopicNamesMapByPostIds(postIds, posts);

        IPage<PostListResponse> result = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        result.setRecords(posts.stream()
                .map(post -> convertToListResponse(post, userMap.get(post.getUserId()), postTagsMap.get(post.getId()), topicNameMap.get(post.getTopicId())))
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 限制关键词长度，防止过长关键词导致性能问题
        if (keyword.trim().length() > 200) {
            keyword = keyword.trim().substring(0, 200);
        }

        // 最小长度限制，防止单字符搜索触发全表扫描
        if (keyword.trim().length() < 2) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogPost::getStatus, 1)
                .ne(BlogPost::getIsDeleted, 1)
                .like(BlogPost::getTitle, keyword.trim())
                .select(BlogPost::getTitle)
                .orderByDesc(BlogPost::getViewCount);

        List<BlogPost> posts = this.page(new Page<>(1, 10), wrapper).getRecords();
        return posts.stream()
                .map(BlogPost::getTitle)
                .collect(Collectors.toList());
    }

    private SaveDraftRequest convertToSaveDraftRequest(BlogDraft draft) {
        SaveDraftRequest request = new SaveDraftRequest();
        request.setDraftId(draft.getId());
        request.setTitle(draft.getTitle());
        request.setContent(draft.getContent());
        request.setSummary(draft.getSummary());
        request.setCategory(draft.getCategory());
        request.setPostId(draft.getPostId());
        request.setTopicId(draft.getTopicId());

        // 将逗号分隔的 tagIds 转换为列表
        if (draft.getTagIds() != null && !draft.getTagIds().isEmpty()) {
            try {
                String[] tagIdStrs = draft.getTagIds().split(",");
                request.setTagIds(java.util.Arrays.stream(tagIdStrs)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList()));
            } catch (NumberFormatException e) {
                log.warn("草稿 tagIds 格式错误: draftId={}, tagIds={}", draft.getId(), draft.getTagIds());
            }
        }

        request.setCoverImage(draft.getCoverImage());

        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<SaveDraftRequest> getMyDrafts(Long userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Page<BlogDraft> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<BlogDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogDraft::getUserId, userId)
                .orderByDesc(BlogDraft::getUpdateTime);

        IPage<BlogDraft> draftPage = blogDraftMapper.selectPage(pageObj, wrapper);

        IPage<SaveDraftRequest> result = new Page<>(draftPage.getCurrent(), draftPage.getSize(), draftPage.getTotal());
        result.setRecords(draftPage.getRecords().stream()
                .map(this::convertToSaveDraftRequest)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<PostListResponse> getMyPosts(Long userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Page<BlogPost> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogPost::getUserId, userId)
                .eq(BlogPost::getStatus, 1) // 只显示已发布的文章
                .ne(BlogPost::getIsDeleted, 1) // 排除已删除的文章
                .orderByDesc(BlogPost::getCreateTime);

        IPage<BlogPost> postPage = this.page(pageObj, wrapper);

        List<BlogPost> posts = postPage.getRecords();
        if (posts.isEmpty()) {
            return new Page<>(page, pageSize, 0);
        }

        // 收集所有文章ID
        List<Long> postIds = posts.stream()
                .map(BlogPost::getId)
                .collect(Collectors.toList());

        // 批量查询标签信息
        Map<Long, List<PostDetailResponse.TagVO>> postTagsMap = getTagsMapByPostIds(postIds);

        // 批量查询话题名称
        Map<Long, String> topicNameMap = getTopicNamesMapByPostIds(postIds, posts);

        // 作者信息（当前用户）
        SysUser user = sysUserMapper.selectById(userId);

        // 转换为列表响应
        IPage<PostListResponse> result = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        result.setRecords(posts.stream()
                .map(post -> convertToListResponse(post, user, postTagsMap.get(post.getId()), topicNameMap.get(post.getTopicId())))
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<PostDetailResponse> getAdminPostList(AdminPostQueryRequest request) {
        Page<BlogPost> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<>();
        // 排除已删除的文章
        wrapper.ne(BlogPost::getIsDeleted, 1);

        // 关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w.like(BlogPost::getTitle, request.getKeyword().trim())
                    .or()
                    .like(BlogPost::getContent, request.getKeyword().trim()));
        }
        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(BlogPost::getStatus, request.getStatus());
        }
        // 用户ID筛选
        if (request.getUserId() != null) {
            wrapper.eq(BlogPost::getUserId, request.getUserId());
        }
        // 分类筛选
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            wrapper.eq(BlogPost::getCategory, request.getCategory().trim());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(BlogPost::getCreateTime);

        IPage<BlogPost> postPage = this.page(page, wrapper);

        List<BlogPost> posts = postPage.getRecords();
        if (posts.isEmpty()) {
            return new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        }

        // 收集所有需要的用户ID
        List<Long> userIds = posts.stream()
                .map(BlogPost::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 收集所有文章ID
        List<Long> postIds = posts.stream()
                .map(BlogPost::getId)
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        // 批量查询标签信息
        Map<Long, List<PostDetailResponse.TagVO>> postTagsMap = getTagsMapByPostIds(postIds);

        // 批量查询话题名称
        Map<Long, String> topicNameMap = getTopicNamesMapByPostIds(postIds, posts);

        // 转换为详情响应
        IPage<PostDetailResponse> result = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        result.setRecords(posts.stream()
                .map(post -> convertToDetailResponse(post, userMap.get(post.getUserId()), postTagsMap.get(post.getId()), topicNameMap.get(post.getTopicId())))
                .collect(Collectors.toList()));

        return result;
    }

    private PostDetailResponse convertToDetailResponse(BlogPost post, SysUser user, List<PostDetailResponse.TagVO> tags, String topicName) {
        PostDetailResponse response = new PostDetailResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setTitle(post.getTitle());
        response.setSummary(post.getSummary());
        response.setContent(post.getContent());
        response.setCategory(post.getCategory());
        response.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0L);
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setCollectCount(post.getCollectCount());
        response.setStatus(post.getStatus());
        response.setReviewerId(post.getReviewerId());
        response.setReviewTime(post.getReviewTime());
        response.setRejectReason(post.getRejectReason());
        response.setCreateTime(post.getCreateTime());
        response.setUpdateTime(post.getUpdateTime());
        response.setCoverImage(post.getCoverUrl());
        response.setTopicId(post.getTopicId());
        response.setTopicName(topicName);

        if (user != null) {
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
        }

        response.setTags(tags != null ? tags : Collections.emptyList());

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeletePost(Long postId, Long adminId) {
        BlogPost post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(404, "文章不存在");
        }
        if (post.getIsDeleted() != null && post.getIsDeleted() == 1) {
            throw new BusinessException(404, "文章不存在");
        }

        this.update().set("is_deleted", 1).eq("id", postId).update();

        // 删除标签关联
        LambdaQueryWrapper<BlogPostTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(BlogPostTag::getPostId, postId);
        blogPostTagMapper.delete(tagWrapper);

        // 逻辑删除评论
        blogCommentMapper.update(null, new LambdaUpdateWrapper<BlogComment>()
                .eq(BlogComment::getPostId, postId)
                .set(BlogComment::getIsDeleted, 1));

        // 逻辑删除点赞
        blogLikeMapper.update(null, new LambdaUpdateWrapper<BlogLike>()
                .eq(BlogLike::getPostId, postId)
                .set(BlogLike::getIsDeleted, 1));

        // 逻辑删除收藏
        blogCollectMapper.update(null, new LambdaUpdateWrapper<BlogCollect>()
                .eq(BlogCollect::getPostId, postId)
                .set(BlogCollect::getIsDeleted, 1));

        log.info("管理员删除文章: postId={}, adminId={}, title={}", postId, adminId, post.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<PostDetailResponse> getReviewList(String keyword, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Page<BlogPost> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<BlogPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogPost::getStatus, 0) // 待审核
                .ne(BlogPost::getIsDeleted, 1); // 排除已删除

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(BlogPost::getTitle, keyword.trim())
                    .or()
                    .like(BlogPost::getContent, keyword.trim()));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(BlogPost::getCreateTime);

        IPage<BlogPost> postPage = this.page(pageObj, wrapper);

        List<BlogPost> posts = postPage.getRecords();
        if (posts.isEmpty()) {
            return new Page<>(page, pageSize, 0);
        }

        // 收集所有需要的用户ID
        List<Long> userIds = posts.stream()
                .map(BlogPost::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 收集所有文章ID
        List<Long> postIds = posts.stream()
                .map(BlogPost::getId)
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        // 批量查询标签信息
        Map<Long, List<PostDetailResponse.TagVO>> postTagsMap = getTagsMapByPostIds(postIds);

        // 批量查询话题名称
        Map<Long, String> topicNameMap = getTopicNamesMapByPostIds(postIds, posts);

        // 转换为详情响应
        IPage<PostDetailResponse> result = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        result.setRecords(posts.stream()
                .map(post -> convertToDetailResponse(post, userMap.get(post.getUserId()), postTagsMap.get(post.getId()), topicNameMap.get(post.getTopicId())))
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePost(Long postId, Long reviewerId) {
        BlogPost post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(404, "文章不存在");
        }
        if (post.getIsDeleted() != null && post.getIsDeleted() == 1) {
            throw new BusinessException(404, "文章不存在");
        }
        if (post.getStatus() != 0) {
            throw new BusinessException(400, "文章不在待审核状态，无法审核");
        }

        post.setStatus(1);
        post.setReviewerId(reviewerId);
        post.setReviewTime(LocalDateTime.now());
        post.setRejectReason(null);
        this.updateById(post);

        // 文章审核通过后，立即更新热门趋势数据
        trendingService.updatePostTrending(postId);

        String approveTitle = "文章审核通过";
        String approveContent = "您的文章《" + post.getTitle() + "》已通过审核，现已正式发布";
        notificationService.sendNotification("AUDIT", approveTitle, approveContent, reviewerId, post.getUserId(), "POST", post.getId());

log.info("文章审核通过: postId={}, reviewerId={}, title={}", postId, reviewerId, post.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectPost(Long postId, Long reviewerId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(400, "驳回原因不能为空");
        }
        if (reason.length() > 500) {
            throw new BusinessException(400, "驳回原因不能超过500字符");
        }

        BlogPost post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(404, "文章不存在");
        }
        if (post.getIsDeleted() != null && post.getIsDeleted() == 1) {
            throw new BusinessException(404, "文章不存在");
        }
        if (post.getStatus() != 0) {
            throw new BusinessException(400, "文章不在待审核状态，无法驳回");
        }

        post.setStatus(2); // 已驳回
        post.setReviewerId(reviewerId);
        post.setReviewTime(LocalDateTime.now());
        post.setRejectReason(htmlSanitizer.sanitizePlainText(reason));
        this.updateById(post);

        String rejectTitle = "文章审核驳回";
        String rejectContent = "您的文章《" + post.getTitle() + "》未通过审核，驳回原因：" + reason;
        notificationService.sendNotification("AUDIT", rejectTitle, rejectContent, reviewerId, post.getUserId(), "POST", post.getId());

        log.info("文章审核驳回: postId={}, reviewerId={}, reason={}, title={}", postId, reviewerId, reason, post.getTitle());
    }
}
