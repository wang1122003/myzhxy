package com.campus.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义认证入口点
 * 处理未认证用户访问受保护资源的情况，返回JSON错误信息
 */
@Component("restAuthenticationEntryPoint") // 将其声明为 Spring Bean
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> data = new HashMap<>();
        data.put("code", HttpServletResponse.SC_UNAUTHORIZED);
        data.put("message", "用户未登录或认证已过期，请重新登录");
        // 可以根据需要添加更多错误详情，例如 authException.getMessage()
        // data.put("details", authException.getMessage()); 

        response.getWriter().write(objectMapper.writeValueAsString(data));
    }
} 