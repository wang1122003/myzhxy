package com.campus.config;

import com.campus.entity.User;
import com.campus.exception.AuthenticationException;
import com.campus.exception.PermissionDeniedException;
import com.campus.utils.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Web安全配置
 * 用于配置API接口的权限验证
 */
@Configuration
public class WebSecurityConfig {

    // 公开API，不需要认证
    private static final List<String> PUBLIC_APIS = Arrays.asList(
            "/api/users/login",
            "/api/common/"
    );
    
    // 管理员API，需要管理员权限
    private static final List<String> ADMIN_APIS = Arrays.asList(
            "/api/admin/",
            "/api/classroom/"
    );
    
    // 教师API，需要教师权限
    private static final List<String> TEACHER_APIS = Arrays.asList(
            "/api/teacher/"
    );
    
    // 学生API，需要学生权限
    private static final List<String> STUDENT_APIS = Arrays.asList(
            "/api/student/"
    );

    /**
     * 权限拦截器
     */
    @Bean
    public Filter authorizationFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                
                try {
                    // 获取请求路径
                    String path = request.getRequestURI();
                    
                    // 检查是否是公开API
                    if (isPublicApi(path)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 获取token
                    String token = getTokenFromRequest(request);
                    if (token == null) {
                        throw new AuthenticationException("未提供认证令牌");
                    }
                    
                    // 验证token
                    if (!JwtUtil.validateToken(token)) {
                        throw new AuthenticationException("认证令牌无效");
                    }
                    
                    // 获取用户信息
                    User user = JwtUtil.getUserFromToken(token);
                    if (user == null) {
                        throw new AuthenticationException("认证令牌无效");
                    }
                    
                    // 检查请求权限
                    if (isAdminApi(path) && user.getUserType() != 0) {
                        throw new PermissionDeniedException("需要管理员权限");
                    }
                    
                    if (isTeacherApi(path) && user.getUserType() != 1 && user.getUserType() != 0) {
                        throw new PermissionDeniedException("需要教师权限");
                    }
                    
                    if (isStudentApi(path) && user.getUserType() != 2 && user.getUserType() != 0) {
                        throw new PermissionDeniedException("需要学生权限");
                    }
                    
                    // 通过权限验证，继续处理请求
                    filterChain.doFilter(request, response);
                } catch (AuthenticationException | PermissionDeniedException e) {
                    // 让全局异常处理器处理这些异常
                    throw e;
                }
            }
        };
    }
    
    /**
     * 从请求中获取令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 检查是否是公开API
     */
    private boolean isPublicApi(String path) {
        return PUBLIC_APIS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查是否是管理员API
     */
    private boolean isAdminApi(String path) {
        return ADMIN_APIS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查是否是教师API
     */
    private boolean isTeacherApi(String path) {
        return TEACHER_APIS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查是否是学生API
     */
    private boolean isStudentApi(String path) {
        return STUDENT_APIS.stream().anyMatch(path::startsWith);
    }
} 