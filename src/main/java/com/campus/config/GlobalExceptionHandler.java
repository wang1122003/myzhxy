package com.campus.config;

import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理400异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        BindException.class,
        MethodArgumentNotValidException.class,
        IllegalArgumentException.class
    })
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
        } else if (e instanceof IllegalArgumentException) {
            message = e.getMessage();
        }
        
        return Result.error(message);
    }
    
    /**
     * 处理404异常
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result handleNotFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        logger.error("资源不存在: {}, URL: {}", e.getMessage(), request.getRequestURL());
        return Result.error("请求的资源不存在");
    }
    
    /**
     * 处理405异常
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        logger.error("不支持的请求方法: {}, URL: {}", e.getMessage(), request.getRequestURL());
        return Result.error("不支持的请求方法: " + e.getMethod());
    }
    
    /**
     * 处理415异常
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        logger.error("不支持的媒体类型: {}, URL: {}", e.getMessage(), request.getRequestURL());
        return Result.error("不支持的媒体类型: " + e.getContentType());
    }
    
    /**
     * 处理文件上传大小限制异常
     */
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        logger.error("文件大小超出限制: {}, URL: {}", e.getMessage(), request.getRequestURL());
        return Result.error("上传的文件过大，请减小文件大小后重试");
    }
    
    /**
     * 处理IO异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public Result handleIOException(IOException e, HttpServletRequest request) {
        logger.error("IO异常: {}, URL: {}", e.getMessage(), request.getRequestURL(), e);
        return Result.error("文件读写错误: " + e.getMessage());
    }
    
    /**
     * 处理SQL异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SQLException.class)
    public Result handleSQLException(SQLException e, HttpServletRequest request) {
        logger.error("SQL异常: {}, URL: {}", e.getMessage(), request.getRequestURL(), e);
        return Result.error("数据库操作错误");
    }
    
    /**
     * 处理其他所有异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        logger.error("服务器错误: {}, URL: {}", e.getMessage(), request.getRequestURL(), e);
        return Result.error("服务器内部错误，请稍后再试");
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