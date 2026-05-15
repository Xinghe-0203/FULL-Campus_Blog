package com.example.edu_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册验证请求 DTO
 */
@Data
@Schema(description = "注册验证请求")
public class RegisterVerifyRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Schema(description = "用户名", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度必须在8-50个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "密码必须包含大小写字母和数字")
    @Schema(description = "密码", required = true)
    private String password;

    @Size(max = 30, message = "昵称长度不能超过30个字符")
    @Schema(description = "昵称")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "邮箱", required = true)
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", required = true)
    private String code;
}
