package com.example.edu_project.controller.post;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.edu_project.annotation.RequiresAuth;
import com.example.edu_project.common.result.Result;
import com.example.edu_project.service.social.BlogLikeService;
import com.example.edu_project.utils.SecurityUtils;
import com.example.edu_project.vo.post.LikeItemVO;
import com.example.edu_project.vo.post.LikeResultVO;
import com.example.edu_project.vo.post.LikeStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点赞控制器
 */
@Tag(name = "点赞管理", description = "点赞相关接口")
@RestController
@RequestMapping("/like")
@Validated
@RequiredArgsConstructor
public class BlogLikeController {

    private final BlogLikeService blogLikeService;

    /**
     * 点赞/取消点赞
     */
    @RequiresAuth
    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{postId}")
    public Result<LikeResultVO> toggleLike(@PathVariable Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LikeResultVO result = blogLikeService.toggleLike(postId, userId);
        return Result.success(result);
    }

    /**
     * 检查是否已点赞
     */
    @Operation(summary = "检查是否已点赞")
    @GetMapping("/{postId}/status")
    public Result<LikeStatusVO> checkLikeStatus(@PathVariable Long postId) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        LikeStatusVO status = blogLikeService.checkLikeStatus(postId, userId);
        return Result.success(status);
    }

    /**
     * 获取我的点赞列表
     */
    @RequiresAuth
    @Operation(summary = "获取我的点赞列表")
    @GetMapping("/my")
    public Result<IPage<LikeItemVO>> getMyLikes(
            @RequestParam(defaultValue = "1", name = "pageNum") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        IPage<LikeItemVO> result = blogLikeService.getMyLikes(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 批量检查点赞状态
     */
    @RequiresAuth
    @Operation(summary = "批量检查点赞状态")
    @PostMapping("/check/batch")
    public Result<List<Boolean>> checkLikeStatusBatch(@RequestBody @NotEmpty @Size(max = 50) List<Long> postIds) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Boolean> results = blogLikeService.checkLikeStatusBatch(postIds, userId);
        return Result.success(results);
    }
}
