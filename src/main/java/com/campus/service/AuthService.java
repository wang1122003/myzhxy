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
     * 获取当前认证用户 (从请求中解析Token)
     * 注意：此方法仍被 JwtAuthenticationFilter 使用，但后续可优化为直接从 SecurityContext 获取
     * @param request HTTP请求
     * @return 用户对象，如果未认证或Token无效则返回null
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
                // 可以记录日志
                // logger.error("从Token解析用户信息时出错: {}", e.getMessage());
                return null;
            }
        }
        return null;
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