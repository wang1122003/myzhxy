package com.campus.utils;

import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * 异常工具类
 * 包含系统使用的自定义异常
 */
public class ExceptionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);
    
    /**
     * 认证异常
     * 用于表示用户认证失败
     */
    public static class AuthenticationException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 构造函数
         */
        public AuthenticationException() {
            super("认证失败，请重新登录");
        }
        
        /**
         * 构造函数
         * @param message 错误信息
         */
        public AuthenticationException(String message) {
            super(message);
        }
        
        /**
         * 构造函数
         * @param message 错误信息
         * @param cause 异常原因
         */
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * 权限拒绝异常
     * 用于表示用户没有权限访问某个资源
     */
    public static class PermissionDeniedException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 构造函数
         */
        public PermissionDeniedException() {
            super("没有权限访问此资源");
        }
        
        /**
         * 构造函数
         * @param message 错误信息
         */
        public PermissionDeniedException(String message) {
            super(message);
        }
        
        /**
         * 构造函数
         * @param message 错误信息
         * @param cause 异常原因
         */
        public PermissionDeniedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * 全局异常处理器
     * 处理一般技术异常
     */
    @RestControllerAdvice
    @Order(2)
    public static class GlobalExceptionHandler {
        
        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
        
        /**
         * 处理400异常
         */
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            BindException.class,
            MethodArgumentNotValidException.class
        })
        @ResponseBody
        public Result handleBadRequestException(Exception e, HttpServletRequest request) {
            logger.error("请求错误: {}, URL: {}", e.getMessage(), request.getRequestURL());
            
            String message = "请求参数错误";
            
            // 参数校验失败，获取具体的错误信息
            if (e instanceof BindException) {
                BindingResult bindingResult = ((BindException) e).getBindingResult();
                message = getBindingResultErrorMessage(bindingResult);
            } else if (e instanceof MethodArgumentNotValidException) {
                BindingResult bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
                message = getBindingResultErrorMessage(bindingResult);
            }
            
            return Result.error(message);
        }
        
        /**
         * 处理404异常
         */
        @ResponseStatus(HttpStatus.NOT_FOUND)
        @ExceptionHandler(NoHandlerFoundException.class)
        @ResponseBody
        public Result handleNotFoundException(NoHandlerFoundException e, HttpServletRequest request) {
            logger.error("资源不存在: {}, URL: {}", e.getMessage(), request.getRequestURL());
            request.setAttribute("javax.servlet.error.status_code", HttpStatus.NOT_FOUND.value());
            return Result.error("请求的资源不存在");
        }
        
        /**
         * 处理405异常
         */
        @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        @ResponseBody
        public Result handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
            logger.error("不支持的请求方法: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error("不支持的请求方法: " + e.getMethod());
        }
        
        /**
         * 处理415异常
         */
        @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        @ResponseBody
        public Result handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
            logger.error("不支持的媒体类型: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error("不支持的媒体类型: " + e.getContentType());
        }
        
        /**
         * 处理文件上传大小限制异常
         */
        @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        @ResponseBody
        public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
            logger.error("文件大小超出限制: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error("上传的文件过大，请减小文件大小后重试");
        }
        
        /**
         * 处理IO异常
         */
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler(IOException.class)
        @ResponseBody
        public Result handleIOException(IOException e, HttpServletRequest request) {
            logger.error("IO异常: {}, URL: {}", e.getMessage(), request.getRequestURL(), e);
            return Result.error("文件读写错误: " + e.getMessage());
        }
        
        /**
         * 处理SQL异常
         */
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler(SQLException.class)
        @ResponseBody
        public Result handleSQLException(SQLException e, HttpServletRequest request) {
            logger.error("SQL异常: {}, URL: {}", e.getMessage(), request.getRequestURL(), e);
            return Result.error("数据库操作错误");
        }
        
        /**
         * 获取参数绑定错误信息
         */
        private String getBindingResultErrorMessage(BindingResult bindingResult) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            if (!fieldErrors.isEmpty()) {
                FieldError fieldError = fieldErrors.get(0);
                return fieldError.getDefaultMessage();
            }
            return "参数校验失败";
        }
    }
    
    /**
     * 业务异常处理器
     * 用于统一处理API请求中的业务异常，返回标准格式的错误信息
     */
    @RestControllerAdvice
    @Order(1)
    public static class BusinessExceptionHandler {
        
        private static final Logger logger = LoggerFactory.getLogger(BusinessExceptionHandler.class);
        
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
            
            // 设置响应的内容类型和编码
            request.setAttribute("javax.servlet.error.status_code", HttpStatus.UNAUTHORIZED.value());
            
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
            
            // 设置响应的内容类型和编码
            request.setAttribute("javax.servlet.error.status_code", HttpStatus.FORBIDDEN.value());
            
            // 返回权限错误信息
            return Result.error(e.getMessage());
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
            
            // 设置响应的内容类型和编码
            request.setAttribute("javax.servlet.error.status_code", HttpStatus.BAD_REQUEST.value());
            
            // 返回具体的错误信息
            return Result.error(e.getMessage());
        }
    }
}