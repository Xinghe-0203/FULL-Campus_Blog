package com.example.edu_project.common.exception;

import com.example.edu_project.common.result.Result;
import com.example.edu_project.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.io.IOException;

/**
 * 全局异常处理器
 * 统一处理所有异常，返回标准的JSON响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletResponse response) {
        log.warn("[EXCEPTION] Business: code={}, message={}", e.getCode(), e.getMessage());
        LogUtils.logSecurityEvent("BUSINESS_EXCEPTION", e.getMessage(), null);
        int status;
        switch (e.getCode()) {
            case 400: case 401: case 403: case 404: case 409: case 500:
                status = e.getCode();
                break;
            default:
                status = 400;
        }
        response.setStatus(status);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常 (JPA/Spring 验证异常)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("[EXCEPTION] ConstraintViolation: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = "请求参数 '" + e.getParameterName() + "' 缺失";
        log.warn("[EXCEPTION] MissingParameter: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理请求体解析失败异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "请求体解析失败";
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException) {
            message = "参数类型不匹配：" + ((InvalidFormatException) cause).getValue();
        } else if (cause instanceof MismatchedInputException) {
            message = "请求体格式错误";
        }
        log.warn("[EXCEPTION] MessageNotReadable: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理404资源不存在异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("[EXCEPTION] NotFound: {}", e.getRequestURL());
        return Result.error(404, "资源不存在");
    }

    /**
     * 处理405方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("[EXCEPTION] MethodNotAllowed: {} not supported", e.getMethod());
        return Result.error(405, "请求方法不支持");
    }

    /**
     * 处理406媒体类型不接受异常
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public Result<Void> handleMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
        log.warn("[EXCEPTION] MediaTypeNotAcceptable: {}", e.getMessage());
        return Result.error(406, "媒体类型不接受");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        log.warn("[EXCEPTION] Validation: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldError() != null
                ? e.getFieldError().getDefaultMessage()
                : "参数绑定失败";
        log.warn("[EXCEPTION] Bind: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理数据库重复键异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("[EXCEPTION] DuplicateKey: {}", e.getMessage());
        return Result.error(409, "数据已存在，操作冲突");
    }

    /**
     * 处理访问拒绝异常（权限不足）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("[EXCEPTION] AccessDenied: {}", e.getMessage());
        LogUtils.logSecurityEvent("ACCESS_DENIED", e.getMessage(), null);
        return Result.error(403, "权限不足，拒绝访问");
    }

    /**
     * 处理认证异常（未登录或认证失败）
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("[EXCEPTION] Authentication: {}", e.getMessage());
        LogUtils.logSecurityEvent("AUTH_FAILED", e.getMessage(), null);
        return Result.error(401, "认证失败，请先登录");
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("[EXCEPTION] UploadSizeExceeded: {}", e.getMessage());
        return Result.error(400, "上传文件大小超出限制");
    }

    /**
     * 处理参数类型转换异常（如 query string 中传了非数字给 int 参数）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        Object actualValue = e.getValue();
        log.warn("[EXCEPTION] TypeMismatch: param={}, value={}, requiredType={}", paramName, actualValue, e.getRequiredType());
        return Result.error(400, "参数 '" + paramName + "' 格式错误，期望类型: " + (e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知"));
    }

    /**
     * 处理IO异常（文件下载中断等）
     * 对ClientAbortException等连接中断异常直接忽略，不返回JSON
     */
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e, HttpServletRequest request, HttpServletResponse response) {
        if (e instanceof ClientAbortException) {
            log.warn("[EXCEPTION] ClientAbort: 客户端主动断开连接, URI={}", request.getRequestURI());
            return;
        }
        log.error("[EXCEPTION] IOException: ", e);
        try {
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"服务器内部错误\"}");
        } catch (IOException ignored) {
            // 如果此时还不能写回，直接忽略
        }
    }

    /**
     * 最后的兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        return Result.error("服务器内部错误");
    }
}
