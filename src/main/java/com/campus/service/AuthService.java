package com.campus.service;

import com.campus.entity.User;
import com.campus.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private static final Logger authLogger = LoggerFactory.getLogger(AuthService.class); // 添加日志记录器
    // Token在Cookie中的名称
    private static final String TOKEN_COOKIE_NAME = "campus_token";
    // Cookie有效期(秒)，默认24小时
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService userDetailsService;

    /**
     * 从请求中获取Token
     *
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
     *
     * @param response HTTP响应
     * @param token    Token字符串
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
     *
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
        authLogger.debug("Attempting to get current user from SecurityContextHolder");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            authLogger.debug("Authentication found. Principal type: {}", (principal != null ? principal.getClass().getName() : "null"));

            if (principal instanceof User) {
                authLogger.debug("Principal is instance of com.campus.entity.User. Returning directly.");
                return (User) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails springUserDetails) {
                authLogger.debug("Principal is instance of UserDetails. Attempting to load User entity from UserDetailsService.");
                try {
                    User userEntity = (User) userDetailsService.loadUserByUsername(springUserDetails.getUsername());
                    authLogger.debug("Successfully loaded User entity from UserDetailsService for username: {}", springUserDetails.getUsername());
                    return userEntity;
                } catch (UsernameNotFoundException | ClassCastException e) {
                    authLogger.error("Failed to load or cast User entity from UserDetailsService for UserDetails principal: {}", e.getMessage());
                    return null;
                }
            } else if (principal instanceof String) {
                authLogger.debug("Principal is instance of String. Attempting to load User entity from UserDetailsService.");
                try {
                    User userEntity = (User) userDetailsService.loadUserByUsername((String) principal);
                    authLogger.debug("Successfully loaded User entity from UserDetailsService for String principal: {}", principal);
                    return userEntity;
                } catch (UsernameNotFoundException | ClassCastException e) {
                    authLogger.error("Failed to load or cast User entity from UserDetailsService for String principal: {}", e.getMessage());
                    return null;
                }
            } else {
                authLogger.warn("Unknown Principal type encountered: {}. Cannot extract User.", (principal != null ? principal.getClass().getName() : "null"));
            }
        } else {
            if (authentication == null) {
                authLogger.debug("No authentication found in SecurityContextHolder.");
            } else if (!authentication.isAuthenticated()) {
                authLogger.debug("Authentication found but is not authenticated: {}", authentication.getName());
            } else {
                authLogger.debug("Authentication is AnonymousAuthenticationToken.");
            }
        }
        authLogger.debug("Returning null from getCurrentUserFromRequest.");
        return null;
    }

    /**
     * 获取当前通过SecurityContext认证的用户
     *
     * @return 当前用户对象 (User)，如果未认证或认证信息不是User类型则返回null
     */
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
                // Handle case where principal is Spring Security User object
                // Need to load our User entity based on the username
                try {
                    // Assuming UserDetailsService returns our User entity
                    return (User) userDetailsService.loadUserByUsername(springUser.getUsername());
                } catch (UsernameNotFoundException e) {
                    return null;
                }
            } else if (principal instanceof String) {
                // Fallback: If Principal is username string
                try {
                    return (User) userDetailsService.loadUserByUsername((String) principal);
                } catch (UsernameNotFoundException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 生成认证Token
     *
     * @param user 用户对象
     * @return JWT Token
     */
    public String generateToken(User user) {
        return jwtUtil.generateToken(user);
    }
} 