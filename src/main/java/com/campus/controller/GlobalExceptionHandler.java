package com.campus.controller;

import com.campus.exception.AuthenticationException;
import com.campus.exception.PermissionDeniedException;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理器
 * 用于统一处理API请求中的异常，返回标准格式的错误信息
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理所有未捕获的异常
     * @param request 请求对象
     * @param e 异常
     * @return 标准错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result handleException(HttpServletRequest request, Exception e) {
        // 记录异常信息
        logger.error("请求处理异常: {}", request.getRequestURI(), e);
        
        // 返回友好的错误信息
        return Result.error("服务器内部错误，请稍后重试");
    }
    
    /**
     * 处理参数校验异常
     * @param request 请求对象
     * @param e 异常
     * @return 标准错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Result handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        // 记录异常信息
        logger.warn("参数校验失败: {}, 原因: {}", request.getRequestURI(), e.getMessage());
        
        // 返回具体的错误信息
        return Result.error(e.getMessage());
    }
    
    /**
     * 处理认证异常
     * @param request 请求对象
     * @param e 异常
     * @return 标准错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public Result handleAuthenticationException(HttpServletRequest request, AuthenticationException e) {
        // 记录异常信息
        logger.warn("认证失败: {}, 原因: {}", request.getRequestURI(), e.getMessage());
        
        // 返回认证错误信息
        return Result.error(e.getMessage());
    }
    
    /**
     * 处理权限异常
     * @param request 请求对象
     * @param e 异常
     * @return 标准错误响应
     */
    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Result handlePermissionDeniedException(HttpServletRequest request, PermissionDeniedException e) {
        // 记录异常信息
        logger.warn("权限不足: {}, 原因: {}", request.getRequestURI(), e.getMessage());
        
        // 返回权限错误信息
        return Result.error(e.getMessage());
    }
} 