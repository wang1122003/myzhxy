package com.campus.utils;

import com.campus.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT工具类，用于生成和验证JWT令牌
 */
@Component
public class JwtUtil {

    // 密钥
    @Value("${jwt.secret:campus-management-system-secret-key-for-jwt-authentication}")
    private String secret;

    // 过期时间(秒)
    @Value("${jwt.expiration:86400}")
    private long expiration;

    // 安全密钥缓存
    private SecretKey secretKey;

    /**
     * 生成安全的随机密钥（用于配置文件中的密钥设置）
     *
     * @return 生成的Base64编码密钥字符串
     */
    public static String generateRandomSecretKey() {
        byte[] keyBytes = new byte[64]; // 512位
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    /**
     * 生成JWT令牌
     *
     * @param user 用户对象
     * @return JWT令牌
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("name", user.getUsername())
                .claim("role", user.getUserType())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从JWT令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("name", String.class));
    }

    /**
     * 验证JWT令牌是否有效 (基本验证)
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token); // 增加过期检查
        } catch (Exception e) {
            // 可以根据具体异常类型打印更详细的日志
            // e.g., MalformedJwtException, ExpiredJwtException, UnsupportedJwtException, IllegalArgumentException
            return false;
        }
    }

    /**
     * 验证JWT令牌是否对特定用户有效
     *
     * @param token       JWT令牌
     * @param userDetails 用户信息
     * @return 是否有效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 从JWT令牌中获取Claims
     *
     * @param token JWT令牌
     * @return Claims对象
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从JWT令牌中获取特定Claim
     *
     * @param token          JWT令牌
     * @param claimsResolver Function to extract the claim
     * @param <T>            Claim 类型
     * @return Claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expirationDate = getClaimFromToken(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    /**
     * 获取安全密钥
     * 确保密钥长度满足HS512算法的要求（至少512位/64字节）
     *
     * @return SecretKey对象
     */
    private SecretKey getSecretKey() {
        // 如果已经创建过密钥，直接返回缓存的实例
        if (secretKey != null) {
            return secretKey;
        }

        // 检查配置的密钥是否足够长
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // 如果配置的密钥不够长(小于64字节)，则生成更长的密钥
        if (keyBytes.length < 64) {
            // 创建一个至少512位（64字节）的密钥
            byte[] newKeyBytes = new byte[64];

            // 将现有密钥复制到新数组
            System.arraycopy(keyBytes, 0, newKeyBytes, 0, Math.min(keyBytes.length, 64));

            // 使用SecureRandom填充剩余空间以确保密钥强度
            new SecureRandom().nextBytes(newKeyBytes);

            // 创建并缓存密钥
            secretKey = Keys.hmacShaKeyFor(newKeyBytes);
        } else {
            // 如果配置的密钥足够长，直接使用
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        }

        return secretKey;
    }

    /**
     * 设置密钥（可选，用于配置）
     */
    public void setSecret(String secret) {
        if (secret != null && !secret.isEmpty()) {
            this.secret = secret;
            // 清除缓存的密钥，以便下次调用getSecretKey时重新生成
            this.secretKey = null;
        }
    }

    /**
     * 设置过期时间（可选，用于配置）
     */
    public void setExpiration(long expiration) {
        if (expiration > 0) {
            this.expiration = expiration;
        }
    }
} 