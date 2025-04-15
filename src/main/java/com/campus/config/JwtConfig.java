package com.campus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;

/**
 * JWT配置类
 * 用于设置JWT相关的配置参数
 */
@Configuration
public class JwtConfig {
    
    /**
     * 密钥
     */
    private static final String SECRET_KEY = "campusSecretKey123456789987654321";
    
    /**
     * token有效期（毫秒）- 默认7天
     */
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000;
    
    /**
     * token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * 存放token的请求头
     */
    private static final String HEADER_STRING = "Authorization";
    
    /**
     * 获取密钥
     */
    public static String getSecretKey() {
        return SECRET_KEY;
    }
    
    /**
     * 获取过期时间
     */
    public static long getExpiration() {
        return EXPIRATION;
    }
    
    /**
     * 获取token前缀
     */
    public static String getTokenPrefix() {
        return TOKEN_PREFIX;
    }
    
    /**
     * 获取请求头名称
     */
    public static String getHeaderString() {
        return HEADER_STRING;
    }
} 