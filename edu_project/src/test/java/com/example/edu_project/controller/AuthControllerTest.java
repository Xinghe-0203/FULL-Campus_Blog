package com.example.edu_project.controller;

import com.example.edu_project.common.enums.BaseErrorCode;
import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.dto.RegisterVerifyRequest;
import com.example.edu_project.dto.SendRegisterCodeRequest;
import com.example.edu_project.service.EmailService;
import com.example.edu_project.service.SysUserService;
import com.example.edu_project.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private SysUserService sysUserService;

    @MockBean
    private JwtUtils jwtUtils;

    private SendRegisterCodeRequest sendCodeRequest;
    private RegisterVerifyRequest verifyRequest;

    @BeforeEach
    void setUp() {
        sendCodeRequest = new SendRegisterCodeRequest();
        sendCodeRequest.setEmail("test@example.com");
        sendCodeRequest.setUsername("testuser");

        verifyRequest = new RegisterVerifyRequest();
        verifyRequest.setEmail("test@example.com");
        verifyRequest.setCode("123456");
        verifyRequest.setUsername("testuser");
    }

    @Test
    @DisplayName("sendRegisterCode_Success_Returns200")
    void sendRegisterCode_Success_Returns200() throws Exception {
        // Given
        doNothing().when(emailService)
                .sendRegisterVerificationCode(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/auth/register/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("验证码已发送"));
    }

    @Test
    @DisplayName("sendRegisterCode_InvalidEmail_Returns400")
    void sendRegisterCode_InvalidEmail_Returns400() throws Exception {
        // Given
        when(emailService)
                .sendRegisterVerificationCode(anyString(), anyString()))
                .thenThrow(new BusinessException(400, "邮箱格式不正确"));

        // When & Then
        mockMvc.perform(post("/auth/register/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid-email\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("sendRegisterCode_TooManyRequests_Returns429")
    void sendRegisterCode_TooManyRequests_Returns429() throws Exception {
        // Given
        when(emailService)
                .sendRegisterVerificationCode(anyString(), anyString()))
                .thenThrow(new BusinessException(429, "发送过于频繁，请稍后再试"));

        // When & Then
        mockMvc.perform(post("/auth/register/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(429));
    }

    @Test
    @DisplayName("verifyAndRegister_Success_Returns200")
    void verifyAndRegister_Success_Returns200() throws Exception {
        // Given
        when(emailService.verifyCode(anyString(), anyString(), any(EmailService.VerificationType.class)))
                .thenReturn(true);
        doNothing().when(sysUserService).registerWithVerifiedEmail(any(RegisterVerifyRequest.class));

        // When & Then
        mockMvc.perform(post("/auth/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"code\":\"123456\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    @Test
    @DisplayName("verifyAndRegister_InvalidCode_ReturnsError")
    void verifyAndRegister_InvalidCode_ReturnsError() throws Exception {
        // Given
        when(emailService.verifyCode(anyString(), anyString(), any(EmailService.VerificationType.class)))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"code\":\"wrongcode\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("verifyAndRegister_ExpiredCode_ReturnsError")
    void verifyAndRegister_ExpiredCode_ReturnsError() throws Exception {
        // Given
        when(emailService.verifyCode(anyString(), anyString(), any(EmailService.VerificationType.class)))
                .thenThrow(new BusinessException(BaseErrorCode.VERIFICATION_CODE_ERROR));

        // When & Then
        mockMvc.perform(post("/auth/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"code\":\"expired\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("verifyAndRegister_EmptyEmail_Returns400")
    void verifyAndRegister_EmptyEmail_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"code\":\"123456\",\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("verifyAndRegister_EmptyCode_Returns400")
    void verifyAndRegister_EmptyCode_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"code\":\"\",\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("verifyAndRegister_EmailAlreadyExists_ReturnsError")
    void verifyAndRegister_EmailAlreadyExists_ReturnsError() throws Exception {
        // Given
        when(emailService.verifyCode(anyString(), anyString(), any(EmailService.VerificationType.class)))
                .thenReturn(true);
        doThrow(new BusinessException(400, "该邮箱已被注册"))
                .when(sysUserService).registerWithVerifiedEmail(any(RegisterVerifyRequest.class));

        // When & Then
        mockMvc.perform(post("/auth/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"code\":\"123456\",\"username\":\"testuser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}