package com.example.edu_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求 DTO
 */
@Data
@Schema(description = "用户登录请求")
public class UserLoginRequest {

    @Schema(description = "用户名（与email二选一）")
    private String username;

    @Schema(description = "邮箱（与username二选一）")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度必须在8-50个字符之间")
    @Schema(description = "密码", requiredMode = RequiredMode.REQUIRED)
    private String password;
}
