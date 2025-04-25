package com.campus.service;

import com.campus.entity.User;
import com.campus.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 * 提供统一的用户认证和授权功能
 */
@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

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
     * 从HttpServletRequest中获取用户信息
     *
     * @param request HTTP请求
     * @return 当前用户对象，如果未认证则返回null
     */
    public User getCurrentUserFromRequest(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            } else if (principal instanceof String) {
                // Fallback: 如果 Principal 是用户名字符串，尝试从数据库加载
                try {
                    return (User) userDetailsService.loadUserByUsername((String) principal);
                } catch (UsernameNotFoundException e) {
                }
            }
        }
        // Consider checking request attributes as a fallback if JWT filter puts info there
        // String username = (String) request.getAttribute("username");
        // Long userId = (Long) request.getAttribute("userId");
        // Integer userType = (Integer) request.getAttribute("userType");
        // if (username != null && userId != null && userType != null) {
        //     User user = new User();
        //     user.setId(userId); // Corrected: setId()
        //     user.setUsername(username); // Corrected: setUsername()
        //     user.setUserType(userType); // Corrected: setUserType()
        //     // Note: This user object might lack other essential details like password or authorities
        //     return user;
        // }

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