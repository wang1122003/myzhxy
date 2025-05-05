package com.campus.utils;

import com.campus.entity.User;
import com.campus.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器，处理请求中的JWT令牌
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger filterLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // 注入 UserService
    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        // 不记录OPTIONS请求和静态资源请求的日志，减少噪音
        if (!request.getMethod().equals("OPTIONS") && !requestURI.contains(".")) {
            filterLogger.info("JwtAuthenticationFilter: 正在处理请求 {}", requestURI);
        }

        final String authorizationHeader = request.getHeader("Authorization");

        // 仅在DEBUG级别记录敏感信息，避免在生产日志中记录token
        if (filterLogger.isDebugEnabled()) {
            filterLogger.debug("Authorization 头: {}", authorizationHeader);
        }

        String username = null;
        String jwt = null;

        // 从Authorization头中提取JWT令牌
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                if (filterLogger.isDebugEnabled()) {
                    filterLogger.debug("从JWT提取的用户名: {}", username);
                }
            } catch (Exception e) {
                filterLogger.error("无法从JWT获取用户名: {}，Token: {}", e.getMessage(), jwt.substring(0, Math.min(10, jwt.length())) + "...");
            }
        } else {
            // 对某些公共路径不记录日志，减少噪音
            if (!isPublicPath(requestURI) && filterLogger.isDebugEnabled()) {
                filterLogger.debug("请求 {} 未包含有效的Authorization头", requestURI);
            }
        }

        // 验证令牌并设置认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (filterLogger.isDebugEnabled()) {
                filterLogger.debug("找到用户名且当前无认证信息，正在为 {} 加载 UserDetails", username);
            }
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (userDetails != null) {
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        filterLogger.debug("用户 {} 的JWT验证成功", username);

                        // 获取完整的User对象
                        User fullUser = userService.findByUsername(username);

                        if (fullUser != null) {
                            // 使用完整的User对象作为principal
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    fullUser, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // 设置当前认证信息到安全上下文
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            if (!isPublicPath(requestURI)) {
                                filterLogger.info("认证成功，已为用户 {} 设置安全上下文", username);
                            }
                        } else {
                            filterLogger.error("无法为用户名 {} 获取完整的User对象，认证失败", username);
                            SecurityContextHolder.clearContext();
                        }
                    } else {
                        filterLogger.warn("用户 {} 的JWT验证失败: 令牌无效或已过期", username);
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    filterLogger.warn("无法为用户名 {} 加载UserDetails，用户可能不存在", username);
                }
            } catch (Exception e) {
                filterLogger.error("认证过程中发生错误: {}", e.getMessage(), e);
                SecurityContextHolder.clearContext();
            }
        }

        // 继续过滤器链
        chain.doFilter(request, response);
    }

    /**
     * 判断是否为公共路径（不需要认证的路径）
     *
     * @param path 请求路径
     * @return 是否为公共路径
     */
    private boolean isPublicPath(String path) {
        // 静态资源
        if (path.contains(".")) {
            return true;
        }

        // 常见的公共API路径
        String[] publicPaths = {
                "/api/users/login",
                "/api/users/register",
                "/api/common/",
                "/api/notifications/recent",
                "/api/posts"
        };

        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        return false;
    }
} 