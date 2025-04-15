package com.campus.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.campus.entity.User;

/**
 * JWT工具类
 * 用于生成和解析JWT令牌
 */
public class JwtUtil {
    
    // 密钥
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // 过期时间，24小时
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;
    
    /**
     * 生成JWT令牌
     * @param user 用户信息
     * @return JWT令牌
     */
    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("userType", user.getUserType());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }
    
    /**
     * 验证JWT令牌
     * @param token JWT令牌
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从JWT令牌中获取用户信息
     * @param token JWT令牌
     * @return 用户对象
     */
    public static User getUserFromToken(String token) {
        Claims claims = getClaims(token);
        if (claims == null) {
            return null;
        }
        
        User user = new User();
        user.setId(claims.get("userId", Long.class));
        user.setUsername(claims.get("username", String.class));
        user.setUserType(claims.get("userType", Integer.class));
        
        return user;
    }
    
    /**
     * 从JWT令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }
    
    /**
     * 从JWT令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public static String getUsername(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("username", String.class) : null;
    }
    
    /**
     * 从JWT令牌中获取用户类型
     * @param token JWT令牌
     * @return 用户类型
     */
    public static Integer getUserType(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("userType", Integer.class) : null;
    }
    
    /**
     * 从JWT令牌中获取Claims
     * @param token JWT令牌
     * @return Claims
     */
    private static Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
} 