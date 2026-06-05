package com.example.edu_project.controller.auth;

import com.example.edu_project.common.enums.BaseErrorCode;
import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.common.result.Result;
import com.example.edu_project.dto.auth.RegisterVerifyRequest;
import com.example.edu_project.dto.auth.SendRegisterCodeRequest;
import com.example.edu_project.service.auth.EmailService;
import com.example.edu_project.service.auth.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册认证控制器
 */
@Slf4j
@Tag(name = "注册认证", description = "用户注册和邮箱验证相关接口")
@RestController
@RequestMapping("/auth/register")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final EmailService emailService;

    private final SysUserService sysUserService;

    @Operation(summary = "发送注册验证码", description = "向指定邮箱发送注册验证码，验证邮箱真实性")
    @PostMapping("/send-code")
    public Result<Void> sendRegisterCode(@Valid @RequestBody SendRegisterCodeRequest request) {
        emailService.sendRegisterVerificationCode(request.getEmail(), request.getUsername());
        return Result.success("验证码已发送", null);
    }

    @Operation(summary = "验证注册验证码", description = "验证邮箱验证码并完成注册")
    @PostMapping("/verify")
    public Result<Void> verifyAndRegister(@Valid @RequestBody RegisterVerifyRequest request) {
        if (!emailService.verifyCode(request.getEmail(), request.getCode(), EmailService.VerificationType.REGISTER)) {
            throw new BusinessException(BaseErrorCode.VERIFICATION_CODE_ERROR);
        }
        sysUserService.registerWithVerifiedEmail(request);
        log.info("用户注册成功: username={}, email={}", request.getUsername(), request.getEmail());
        return Result.success("注册成功", null);
    }
}