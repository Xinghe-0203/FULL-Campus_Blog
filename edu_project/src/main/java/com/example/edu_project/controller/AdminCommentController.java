package com.example.edu_project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.edu_project.common.result.Result;
import com.example.edu_project.dto.PageRequest;
import com.example.edu_project.service.BlogCommentService;
import com.example.edu_project.utils.SecurityUtils;
import com.example.edu_project.vo.CommentWithPostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员评论管理控制器
 */
@Slf4j
@Tag(name = "管理员-评论管理", description = "管理员评论管理接口")
@RestController
@RequestMapping("/admin/comment")
@Validated
public class AdminCommentController {

    @Autowired
    private BlogCommentService blogCommentService;

    @Operation(summary = "获取评论列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('admin')")
    public Result<IPage<CommentWithPostVO>> getCommentList(@Validated PageRequest request) {
        IPage<CommentWithPostVO> result = blogCommentService.getAllComments(request.getPage(), request.getPageSize());
        return Result.success(result);
    }

    /**
     * 管理员删除评论（级联删除子评论）
     */
    @Operation(summary = "管理员删除评论")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long adminId = SecurityUtils.getCurrentUserId();
        blogCommentService.adminDeleteComment(id, adminId);
        return Result.success(null);
    }
}
