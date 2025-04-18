package com.campus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 提供全局CORS配置和其他Web相关配置
 */
@Configuration
// 不再需要实现 WebMvcConfigurer 来配置CORS，但可能需要它用于其他配置，暂时保留
public class WebConfig implements WebMvcConfigurer {
    
    /* // 移除 addCorsMappings 方法
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
    */
    
    /* // CORS 配置已移至 SecurityConfig
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 使用allowedOriginPatterns替代allowedOrigins
        config.addAllowedOriginPattern("*"); 
        // 或者明确指定前端域名
        // config.addAllowedOrigin("http://localhost:8088");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有方法
        config.addAllowedMethod("*");
        // 暴露Authorization头
        config.addExposedHeader("Authorization");
        // 允许凭证
        config.setAllowCredentials(true);
        // 预检请求的有效期，单位秒
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用此配置
        source.registerCorsConfiguration("/**", config);
        // 也可以为特定路径指定不同的配置，但这里我们已经为 /** 配置了
        // source.registerCorsConfiguration("/campus/api/**", config);
        
        return new CorsFilter(source);
    }
    */

    // 移除 ObjectMapper Bean 定义
    /*
    @Bean
    public ObjectMapper objectMapper() {
        // 这里可以添加自定义配置，例如日期格式、忽略未知属性等
        return new ObjectMapper(); 
    }
    */
} 