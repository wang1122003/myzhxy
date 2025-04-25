package com.campus.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 通用工具类
 * 提供系统常用的辅助方法
 */
public class CommonUtils {

    // 手机号正则表达式
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    
    /**
     * 生成MD5加密字符串
     *
     * @param str 原始字符串
     * @return MD5加密后的字符串
     */
    public static String md5(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成UUID
     *
     * @return UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    /**
     * 格式化日期
     *
     * @param date 日期
     * @param pattern 格式
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
    
    /**
     * 生成随机数
     *
     * @param min 最小值
     * @param max 最大值
     * @return 随机数
     */
    public static int random(int min, int max) {
        if (min >= max) {
            return min;
        }
        return new Random().nextInt(max - min) + min;
    }
    
    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        if (length <= 0) {
            return "";
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * 验证手机号
     *
     * @param mobile 手机号
     * @return 是否有效
     */
    public static boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return false;
        }
        return MOBILE_PATTERN.matcher(mobile).matches();
    }
    
    /**
     * 验证邮箱
     *
     * @param email 邮箱
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 安全的字符串截断
     *
     * @param str 原始字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    public static String safeTruncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        
        if (str.length() <= maxLength) {
            return str;
        }
        
        return str.substring(0, maxLength) + "...";
    }
} 