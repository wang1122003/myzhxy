package com.campus.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 * 简化版本，不做真正的JWT验证，仅为了让系统能够启动
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 简化版JWT验证，始终返回true让请求通过
        // 实际应该从请求头中提取token并验证
        return true;
    }
} 