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

        filterLogger.info("JwtAuthenticationFilter: 正在处理请求 {}", request.getRequestURI());

        final String authorizationHeader = request.getHeader("Authorization");
        filterLogger.debug("Authorization 头: {}", authorizationHeader);

        String username = null;
        String jwt = null;

        // 从Authorization头中提取JWT令牌
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            filterLogger.debug("提取到的 JWT: {}", jwt);
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                filterLogger.debug("从 JWT 提取的用户名: {}", username);
            } catch (Exception e) {
                filterLogger.error("无法从JWT获取用户名: {}", e.getMessage());
            }
        } else {
            filterLogger.debug("Authorization 头中未找到 Bearer token。");
        }

        // 验证令牌并设置认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            filterLogger.debug("找到用户名且当前无认证信息，正在为 {} 加载 UserDetails", username);
            try {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                filterLogger.debug("成功为 {} 加载 UserDetails", username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                filterLogger.debug("用户 {} 的 JWT 验证成功", username);

                // 获取完整的 User 对象
                User fullUser = userService.findByUsername(username); // 假设 userService 有 findByUsername 方法

                if (fullUser != null) {
                    // 使用完整的 User 对象作为 principal
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            fullUser, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置当前认证信息到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    filterLogger.info("认证成功，已为用户 {} 设置 SecurityContextHolder (使用完整 User 对象)", username);
                } else {
                     filterLogger.error("无法为用户名 {} 获取完整的 User 对象，认证失败", username);
                     // 可以选择清除 SecurityContext 或其他处理
                     SecurityContextHolder.clearContext();
                }
            } else {
                filterLogger.warn("用户 {} 的 JWT 验证失败，Token: {}", username, jwt);
            }
            } catch (Exception e) {
                filterLogger.error("为用户名 {} 加载 UserDetails 或验证 Token 时出错: {}", username, e.getMessage());
            }
        } else {
            if (username == null) {
                filterLogger.debug("用户名为空，跳过认证设置。");
            }
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterLogger.debug("SecurityContextHolder 已包含认证信息: {}", SecurityContextHolder.getContext().getAuthentication().getName());
            }
        }

        filterLogger.debug("将请求传递到过滤器链下游...");
        chain.doFilter(request, response);
    }
} 