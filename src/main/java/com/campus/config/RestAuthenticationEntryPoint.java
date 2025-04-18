package com.campus.config;

// import com.campus.utils.JsonUtils; // 移除 JsonUtils 导入

import com.campus.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 自定义认证入口点
 * 处理认证失败（未登录或Token无效）的情况，返回JSON格式的错误信息
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired // 注入 ObjectMapper Bean
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 使用 Result<?> 或 Result<Object>
        Result<?> result = Result.error("认证失败，请先登录").code(HttpStatus.UNAUTHORIZED.value());

        // 使用 ObjectMapper 将 Result 对象转换为 JSON 字符串并写入响应
        objectMapper.writeValue(response.getWriter(), result);
    }
} 