package com.campus.config;

import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 认证过滤器
 * 拦截请求，验证JWT Token，并将认证信息设置到 Spring Security 上下文中
 */
@Component // 注册为 Spring Bean，方便注入
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService; // 暂时使用 AuthService 获取 Token 和用户信息

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = authService.getTokenFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                // 从 Token 中获取用户信息 (这里暂时依赖 AuthService 的实现)
                // 更好的做法是直接从 jwtUtil 解析必要信息，或者创建一个 UserDetailsService 来加载用户信息
                User userDetails = authService.getCurrentUser(request);

                if (userDetails != null) {
                    // 创建权限列表 (Spring Security 需要 GrantedAuthority)
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    // 假设 userType 0=ADMIN, 1=STUDENT, 2=TEACHER
                    String role = switch (userDetails.getUserType()) {
                        case 0 -> "ROLE_ADMIN";
                        case 1 -> "ROLE_STUDENT";
                        case 2 -> "ROLE_TEACHER";
                        default -> null;
                    };
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority(role));
                    }

                    // 创建 Authentication 对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    // 设置认证请求的详情
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 将 Authentication 对象设置到 SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("用户 '{}' (ID: {}) 已通过JWT认证，角色: {}", userDetails.getUsername(), userDetails.getId(), role);
                } else {
                    logger.warn("无法从有效的JWT中获取用户信息, Token: {}", jwt);
                }
            } else {
                // 如果没有 token 或 token 无效，则不设置 SecurityContext，后续的 Filter 会处理（例如抛出未授权异常）
                if (StringUtils.hasText(jwt)) {
                    logger.debug("JWT Token 无效或已过期: {}", jwt);
                }
            }
        } catch (Exception ex) {
            logger.error("JWT认证过滤器处理失败: {}", ex.getMessage());
            // 不要在过滤器中抛出异常，让后续处理或全局异常处理器捕获
        }

        // 继续执行过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
} 