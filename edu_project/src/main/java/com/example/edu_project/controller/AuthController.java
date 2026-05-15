package com.example.edu_project.controller;

import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.common.result.Result;
import com.example.edu_project.dto.RegisterVerifyRequest;
import com.example.edu_project.dto.SendRegisterCodeRequest;
import com.example.edu_project.service.EmailService;
import com.example.edu_project.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 注册认证控制器
 */
@Slf4j
@Tag(name = "注册认证", description = "用户注册和邮箱验证相关接口")
@RestController
@RequestMapping("/auth/register")
@Validated
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SysUserService sysUserService;

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
            throw new BusinessException(400, "验证码验证失败");
        }
        sysUserService.registerWithVerifiedEmail(request);
        log.info("用户注册成功: username={}, email={}", request.getUsername(), request.getEmail());
        return Result.success("注册成功", null);
    }
}