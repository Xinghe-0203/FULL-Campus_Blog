package com.example.edu_project.controller;

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
 * 璇濋鎺у埗鍣?
 */
@Slf4j
@Tag(name = "璇濋", description = "璇濋鐩稿叧鎺ュ彛")
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
     * 鍒涘缓璇濋锛堜粎绠＄悊鍛橈級
     */
    @Operation(summary = "鍒涘缓璇濋锛堜粎绠＄悊鍛橈級")
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<Long> createTopic(@Valid @RequestBody TopicCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(401, "璇峰厛鐧诲綍");
        }

        Long topicId = topicService.createTopic(request.getName(), request.getDescription());
        return Result.success(topicId);
    }

    /**
     * 鑾峰彇璇濋鍒楄〃锛堝垎椤碉級
     */
    @Operation(summary = "鑾峰彇璇濋鍒楄〃")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getTopicList(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        List<Map<String, Object>> result = topicService.getTopicList(page, pageSize).getRecords()
                .stream().map(this::toTopicMap).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 鑾峰彇鐑棬璇濋
     */
    @Operation(summary = "鑾峰彇鐑棬璇濋")
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotTopics(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        List<Map<String, Object>> result = topicService.getHotTopics(limit)
                .stream().map(this::toTopicMap).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 鑾峰彇璇濋璇︽儏
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
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        List<CirclePostVO> posts = circleService.getPostsByTopic(topicId, page, pageSize, userId);
        return Result.success(posts);
    }
}
