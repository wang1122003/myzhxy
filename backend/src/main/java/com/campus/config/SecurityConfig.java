package com.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置类 (已完全禁用认证和拦截)
 */
@Configuration
@EnableWebSecurity // 启用Spring Security的Web安全支持
public class SecurityConfig {

    // 注释掉 Autowired 字段
    /*
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // 注入自定义的 JWT 过滤器
    
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint; // 注入自定义认证入口点
    
    @Autowired
    private RestAccessDeniedHandler restAccessDeniedHandler; // 注入自定义访问拒绝处理器
    */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 启用并配置 CORS
                .cors(Customizer.withDefaults()) // 使用名为 corsConfigurationSource 的 Bean
                // 禁用 CSRF
                .csrf(csrf -> csrf.disable())
                // 配置 Session 管理策略为无状态 (STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置授权规则 (完全允许所有请求)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 允许所有请求
                );
            /* // 注释掉异常处理配置
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(restAuthenticationEntryPoint) // 配置认证入口点
                .accessDeniedHandler(restAccessDeniedHandler) // 配置访问拒绝处理器
            );
            */
        // .httpBasic(withDefaults()); // 如果需要 HTTP Basic 认证，可以开启

        // 注释掉添加自定义 Filter
        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置 CORS 策略
     *
     * @return CorsConfigurationSource
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的前端来源 (重要！根据你的前端地址修改)
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8088", "http://localhost:7778", "http://localhost:7777")); // 添加前端开发服务器地址
        // 允许所有请求方法
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许所有请求头
        configuration.setAllowedHeaders(List.of("*"));
        // 允许携带凭证 (如 Cookies)
        configuration.setAllowCredentials(true);
        // 暴露哪些响应头给前端 JS (例如用于获取 Token)
        configuration.setExposedHeaders(List.of("Authorization"));
        // 预检请求的有效期
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用这个配置
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 其他 Bean 定义，如 AuthenticationManager 等 (后续添加)

}