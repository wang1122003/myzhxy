package com.campus.service;

import com.campus.entity.User;
import com.campus.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 * 提供统一的用户认证和授权功能
 */
@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    // Token在Cookie中的名称
    private static final String TOKEN_COOKIE_NAME = "campus_token";
    // Cookie有效期(秒)，默认24小时
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60;

    /**
     * 从请求中获取Token
     * @param request HTTP请求
     * @return Token字符串，如果不存在则返回null
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        // 1. 先尝试从请求头获取
        String headerToken = request.getHeader("Authorization");
        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            return headerToken.substring(7);
        }
        
        // 2. 再尝试从Cookie获取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
    
    /**
     * 设置Token到Cookie
     * @param response HTTP响应
     * @param token Token字符串
     */
    public void setTokenToCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setHttpOnly(true); // 增强安全性，防止JS读取
        response.addCookie(cookie);
    }
    
    /**
     * 清除Token Cookie
     * @param response HTTP响应
     */
    public void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 立即过期
        response.addCookie(cookie);
    }
    
    /**
     * 验证当前用户是否已认证
     * @param request HTTP请求
     * @return 是否已认证
     */
    public boolean isAuthenticated(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return token != null && jwtUtil.validateToken(token);
    }
    
    /**
     * 获取当前认证用户
     * @param request HTTP请求
     * @return 用户对象，如果未认证则返回null
     */
    public User getCurrentUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token)) {
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getClaimsFromToken(token).get("name", String.class);
                Integer userType = jwtUtil.getClaimsFromToken(token).get("role", Integer.class);
                
                User user = new User();
                user.setId(userId);
                user.setUsername(username);
                user.setUserType(userType);
                return user;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 检查用户是否有指定角色
     * @param request HTTP请求
     * @param userType 用户类型(0:管理员, 1:学生, 2:教师)
     * @return 是否有指定角色
     */
    public boolean hasRole(HttpServletRequest request, Integer userType) {
        User currentUser = getCurrentUser(request);
        return currentUser != null && (currentUser.getUserType().equals(userType) || currentUser.getUserType() == 0); 
        // 管理员拥有所有权限
    }
    
    /**
     * 检查是否有管理员权限
     * @param request HTTP请求
     * @return 是否有管理员权限
     */
    public boolean isAdmin(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        return currentUser != null && currentUser.getUserType() == 0;
    }
    
    /**
     * 检查是否有教师权限
     * @param request HTTP请求
     * @return 是否有教师权限
     */
    public boolean isTeacher(HttpServletRequest request) {
        return hasRole(request, 2);
    }
    
    /**
     * 检查是否有学生权限
     * @param request HTTP请求
     * @return 是否有学生权限
     */
    public boolean isStudent(HttpServletRequest request) {
        return hasRole(request, 1);
    }
    
    /**
     * 生成认证Token
     * @param user 用户对象
     * @return JWT Token
     */
    public String generateToken(User user) {
        return jwtUtil.generateToken(user);
    }
} 