package com.campus.utils;

import com.campus.entity.User;
import com.campus.utils.ExceptionUtils.AuthenticationException;
import com.campus.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 * 用于验证API请求的JWT令牌和设置用户信息
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 检查请求是否需要认证
        String path = request.getRequestURI();
        // 公开路径不进行认证
        if (path.startsWith("/api/users/login") || path.startsWith("/api/common/")) {
            return true;
        }
        
        // 验证是否已认证
        if (!authService.isAuthenticated(request)) {
            throw new AuthenticationException("认证失败，请重新登录");
        }
        
        // 获取当前用户
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            throw new AuthenticationException("无法获取用户信息");
        }
        
        // 将用户信息添加到请求属性中，方便后续使用
        request.setAttribute("userId", currentUser.getId());
        request.setAttribute("username", currentUser.getUsername());
        request.setAttribute("userType", currentUser.getUserType());
        
        return true;
    }
} 