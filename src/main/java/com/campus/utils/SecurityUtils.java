package com.campus.utils;

import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.utils.ExceptionUtils.AuthenticationException;
import com.campus.utils.ExceptionUtils.PermissionDeniedException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 系统安全工具类
 * 提供系统安全配置和权限验证功能
 */
public class SecurityUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
    
    /**
     * Web安全配置
     * 用于配置API接口的权限验证
     */
    @Configuration
    public static class WebSecurityConfig {
    
        @Autowired
        private AuthService authService;
    
        // 公开API，不需要认证
        private static final List<String> PUBLIC_APIS = Arrays.asList(
                "/campus/api/users/login",
                "/campus/api/users/register",
                "/campus/api/users/check-session",
                "/campus/api/users/create-admin",
                "/campus/api/common/notice-types",
                "/campus/api/common/terms",
                "/campus/api/common/time-slots",
                "/campus/api/common/weekdays",
                "/campus/api/file/upload/",
                "/campus/api/file/download/",
                "/campus/api/notices/recent",
                "/campus/api/notices/",
                "/campus/static/",
                "/favicon.ico"
        );
        
        // 管理员API，需要管理员权限
        private static final List<String> ADMIN_APIS = Arrays.asList(
                "/campus/api/admin/",
                "/campus/api/classroom/"
        );
        
        // 教师API，需要教师权限
        private static final List<String> TEACHER_APIS = List.of(
                "/campus/api/teacher/"
        );
        
        // 学生API，需要学生权限
        private static final List<String> STUDENT_APIS = List.of(
                "/campus/api/student/"
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

                    // 放行 OPTIONS 请求 (用于 CORS 预检)
                    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    try {
                        // 获取请求路径
                        String path = request.getRequestURI();
                        
                        // 检查是否是公开API
                        if (isPublicApi(path)) {
                            filterChain.doFilter(request, response);
                            return;
                        }
                        
                        // 验证认证状态
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
                        
                        // 检查请求权限
                        if (isAdminApi(path) && !authService.isAdmin(request)) {
                            throw new PermissionDeniedException("需要管理员权限");
                        }
                        
                        if (isTeacherApi(path) && !authService.isTeacher(request) && !authService.isAdmin(request)) {
                            throw new PermissionDeniedException("需要教师权限");
                        }
                        
                        if (isStudentApi(path) && !authService.isStudent(request) && !authService.isAdmin(request)) {
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
} 