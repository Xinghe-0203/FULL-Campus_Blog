package com.example.edu_project.controller.auth;

import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.common.result.Result;
import com.example.edu_project.dto.auth.ResetPasswordRequest;
import com.example.edu_project.dto.auth.SendCodeRequest;
import com.example.edu_project.service.auth.EmailService;
import com.example.edu_project.service.auth.SysUserService;
import com.example.edu_project.utils.StringMaskUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 密码找回控制器
 */
@Slf4j
@Tag(name = "密码找回", description = "密码找回相关接口")
@RestController
@RequestMapping("/auth/password")
@Validated
@RequiredArgsConstructor
public class PasswordController {

    private final EmailService emailService;

    private final SysUserService sysUserService;

    /**
     * 发送验证码到邮箱
     */
    @Operation(summary = "发送验证码", description = "发送密码找回验证码到指定邮箱")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        // 检查邮箱是否已注册（未注册的邮箱不能重置密码）
        if (!emailService.isEmailRegistered(request.getEmail())) {
            throw new BusinessException(400, "该邮箱未注册");
        }
        emailService.sendVerificationCode(request.getEmail(), EmailService.VerificationType.PASSWORD_RESET);
        return Result.success("验证码已发送", null);
    }

    /**
     * 验证验证码并重置密码
     */
    @Operation(summary = "重置密码", description = "验证验证码后重置密码")
    @PutMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // 验证验证码
        if (!emailService.verifyCode(request.getEmail(), request.getCode(), EmailService.VerificationType.PASSWORD_RESET)) {
            throw new BusinessException(400, "验证码验证失败");
        }

        // 重置密码
        sysUserService.resetPassword(request.getEmail(), request.getNewPassword());

        log.info("密码重置成功: {}", StringMaskUtils.maskEmail(request.getEmail()));
        return Result.success("密码重置成功", null);
    }
}