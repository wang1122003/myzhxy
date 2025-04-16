package com.campus.utils;

import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.service.FileService;
import com.campus.utils.ExceptionUtils.AuthenticationException;
import com.campus.utils.ExceptionUtils.PermissionDeniedException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 系统安全和初始化工具类
 * 提供系统安全配置和初始化功能
 */
public class SecurityUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
    
    /**
     * 应用初始化组件
     * 在应用启动时执行必要的初始化操作
     */
    @Component
    public static class AppInitializer implements ApplicationListener<ContextRefreshedEvent> {
        
        private static final Logger logger = LoggerFactory.getLogger(AppInitializer.class);
        
        @Autowired
        private FileService fileService;
        
        @Value("${file.upload.path:uploads}")
        private String uploadPath;
        
        // 添加静态标志防止重复初始化
        private static boolean initialized = false;
        
        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            // 使用双重检查锁定模式确保只初始化一次
            if (!initialized) {
                synchronized (AppInitializer.class) {
                    if (!initialized) {
                        // 初始化文件上传目录
                        logger.info("初始化文件上传目录: {}", uploadPath);
                        fileService.init();
                        
                        // 其他系统初始化操作可以在这里添加
                        
                        logger.info("系统初始化完成");
                        initialized = true;
                    }
                }
            }
        }
    }
    
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
                        
                        // 验证认证状态
                        if (!authService.isAuthenticated(request)) {
                            throw new AuthenticationException("认证失败，请重新登录");
                        }
                        
                        // 获取当前用户
                        User currentUser = authService.getCurrentUser(request);
                        if (currentUser == null) {
                            throw new AuthenticationException("无法获取用户信息");
                        }
                        
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