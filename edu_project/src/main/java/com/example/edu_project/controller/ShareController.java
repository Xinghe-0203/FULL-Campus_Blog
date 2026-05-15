package com.example.edu_project.controller;

import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.common.result.Result;
import com.example.edu_project.service.ShareService;
import com.example.edu_project.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 分享控制器
 */
@Tag(name = "分享管理", description = "分享相关接口")
@RestController
@RequestMapping("/share")
@Validated
public class ShareController {

    @Autowired
    private ShareService shareService;

    /**
     * 记录分享
     */
    @Operation(summary = "记录分享")
    @PostMapping("/{postId}")
    public Result<Void> recordShare(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "web") String platform) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        shareService.recordShare(postId, userId, platform);
        return Result.success(null);
    }

    /**
     * 获取分享数
     */
    @Operation(summary = "获取分享数")
    @GetMapping("/count/{postId}")
    public Result<Integer> getShareCount(@PathVariable Long postId) {
        if (postId == null) {
            throw new BusinessException(400, "文章ID不能为空");
        }
        int count = shareService.getShareCount(postId);
        return Result.success(count);
    }
}