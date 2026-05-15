package com.example.edu_project.controller;

import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.common.result.Result;
import com.example.edu_project.dto.TagCreateRequest;
import com.example.edu_project.entity.BlogTag;
import com.example.edu_project.service.BlogTagService;
import com.example.edu_project.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签管理控制器
 */
@Tag(name = "标签管理", description = "标签相关接口")
@RestController
@RequestMapping("/tag")
@Validated
public class BlogTagController {

    @Autowired
    private BlogTagService blogTagService;

    private Map<String, Object> toTagMap(BlogTag tag) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tag.getId());
        map.put("name", tag.getName());
        return map;
    }

    /**
     * 获取所有标签列表
     */
    @Operation(summary = "获取所有标签")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listAllTags() {
        List<BlogTag> tags = blogTagService.listAllTags();
        List<Map<String, Object>> result = tags.stream().map(this::toTagMap).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 根据ID获取标签
     */
    @Operation(summary = "根据ID获取标签")
    @GetMapping("/{tagId}")
    public Result<Map<String, Object>> getTagById(@PathVariable Long tagId) {
        BlogTag tag = blogTagService.getById(tagId);
        if (tag == null) {
            throw new BusinessException(404, "标签不存在");
        }
        return Result.success(toTagMap(tag));
    }

    /**
     * 创建标签
     */
    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Map<String, Object>> createTag(@Valid @RequestBody TagCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        BlogTag tag = blogTagService.createTag(request.getName());
        return Result.success(toTagMap(tag));
    }

    /**
     * 删除标签（仅管理员可操作）
     */
    @Operation(summary = "删除标签")
    @DeleteMapping("/{tagId}")
    public Result<Void> deleteTag(@PathVariable Long tagId) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!SecurityUtils.isCurrentUserAdmin()) {
            throw new BusinessException(403, "仅管理员可执行此操作");
        }
        blogTagService.deleteTag(tagId);
        return Result.success(null);
    }
}