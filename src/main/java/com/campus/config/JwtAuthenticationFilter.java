package com.campus.config;

import com.campus.entity.User;
// import com.campus.exception.UnauthorizedException;
// import com.campus.service.JwtService;
// import com.campus.utils.AuthUtils;
import com.campus.utils.JwtUtil;
// import com.campus.service.JwtService; // Removed unused import
import com.campus.service.UserService; // Keep if needed, or remove if also unused
import com.campus.service.AuthService;
// Removed unused import
// import com.campus.service.UserService;
import com.campus.utils.JwtUtil; // Keep existing JwtUtil import
// Removed unresolved import
// import com.campus.utils.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 拦截请求，验证JWT Token，并将认证信息设置到 Spring Security 上下文中
 */
@Component // 注册为 Spring Bean，方便注入
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService; // 注入 UserDetailsService

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = getTokenFromRequest(request); // 从请求中提取Token

        try {
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.getUsernameFromToken(jwt); // 从Token获取用户名

                // 检查 SecurityContext 中是否已有认证信息 (避免重复认证)
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 使用 UserDetailsService 加载用户信息
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    // 验证Token对于该用户是否有效 (虽然validateToken已做部分检查，这里可以再确认下)
                    if (jwtUtil.validateToken(jwt, userDetails)) { // 假设 JwtUtil 有此方法
                        // 创建 Authentication 对象 (使用 UserDetails 及其权限)
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        // 设置认证请求的详情
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 将 Authentication 对象设置到 SecurityContextHolder
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 尝试将 User 实体存入请求属性 (如果 UserDetails 是 User 的实例)
                        if (userDetails instanceof User userEntity) {
                            request.setAttribute("username", userEntity.getUsername());
                            request.setAttribute("userId", userEntity.getId());
                            request.setAttribute("userType", userEntity.getUserType());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // 异常处理
        }

        // 继续执行过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头或Cookie中提取JWT Token
     * (这个方法从 AuthService 移过来或重写，因为 AuthService 不再被注入)
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从请求头获取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}