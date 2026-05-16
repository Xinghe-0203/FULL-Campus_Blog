package com.example.edu_project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.common.result.Result;
import com.example.edu_project.dto.TopicCreateRequest;

import com.example.edu_project.entity.Topic;
import com.example.edu_project.service.CircleService;
import com.example.edu_project.service.TopicService;
import com.example.edu_project.utils.SecurityUtils;
import com.example.edu_project.vo.CirclePostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 话题控制器
 */
@Slf4j
@Tag(name = "话题", description = "话题相关接口")
@RestController
@RequestMapping("/topic")
@Validated
public class TopicController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private CircleService circleService;

    private Map<String, Object> toTopicMap(Topic topic) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", topic.getId());
        map.put("name", topic.getName());
        map.put("description", topic.getDescription());
        map.put("postCount", topic.getPostCount());
        map.put("status", topic.getStatus());
        map.put("createTime", topic.getCreateTime());
        return map;
    }

    /**
     * 创建话题（仅管理员）
     */
    @Operation(summary = "创建话题（仅管理员）")
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<Long> createTopic(@Valid @RequestBody TopicCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Long topicId = topicService.createTopic(request.getName(), request.getDescription());
        return Result.success(topicId);
    }

    /**
     * 获取话题列表（分页）
     */
    @Operation(summary = "获取话题列表")
    @GetMapping("/list")
    public Result<IPage<Map<String, Object>>> getTopicList(
            @RequestParam(defaultValue = "1", name = "pageNum") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        IPage<Topic> topicPage = topicService.getTopicList(page, pageSize);
        IPage<Map<String, Object>> result = topicPage.convert(this::toTopicMap);
        return Result.success(result);
    }

    /**
     * 获取热门话题
     */
    @Operation(summary = "获取热门话题")
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotTopics(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        List<Map<String, Object>> result = topicService.getHotTopics(limit)
                .stream().map(this::toTopicMap).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 获取话题详情
     */
    @Operation(summary = "获取话题详情")
    @GetMapping("/{topicId}")
    public Result<Map<String, Object>> getTopicById(@PathVariable Long topicId) {
        Topic topic = topicService.getTopicById(topicId);
        if (topic == null) {
            throw new BusinessException(404, "话题不存在");
        }
        return Result.success(toTopicMap(topic));
    }

    /**
     * 获取话题下的动态列表
     */
    @Operation(summary = "获取话题下的动态列表")
    @GetMapping("/{topicId}/posts")
    public Result<List<CirclePostVO>> getTopicPosts(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "1", name = "pageNum") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        List<CirclePostVO> posts = circleService.getPostsByTopic(topicId, page, pageSize, userId);
        return Result.success(posts);
    }
}