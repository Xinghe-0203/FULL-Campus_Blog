package com.example.edu_project.aspect;

import com.example.edu_project.annotation.RequiresAdmin;
import com.example.edu_project.annotation.RequiresAuth;
import com.example.edu_project.common.exception.BusinessException;
import com.example.edu_project.utils.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthAspect {

    /**
     * 处理 @RequiresAuth 注解 - 检查用户是否已认证
     */
    @Around("@annotation(requiresAuth)")
    public Object checkAuth(ProceedingJoinPoint joinPoint, RequiresAuth requiresAuth) throws Throwable {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return joinPoint.proceed();
    }

    /**
     * 处理 @RequiresAdmin 注解 - 检查是否为管理员
     */
    @Around("@annotation(requiresAdmin)")
    public Object checkAdmin(ProceedingJoinPoint joinPoint, RequiresAdmin requiresAdmin) throws Throwable {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!SecurityUtils.isCurrentUserAdmin()) {
            throw new BusinessException(403, "仅管理员可执行此操作");
        }
        return joinPoint.proceed();
    }
}
