package com.campus.utils;

import com.campus.exception.CustomException;
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
import java.sql.SQLException;
import java.util.List;

/**
 * 全局异常处理工具类
 * 使用 @RestControllerAdvice 统一处理 Controller 层抛出的异常
 */
public class ExceptionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

    /**
     * 全局异常处理器 - 处理技术异常
     * 优先级较低 (Order=2)
     */
    @RestControllerAdvice
    @Order(2) // Lower priority than BusinessExceptionHandler
    public static class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * 处理400异常 (Bad Request)
         */
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            BindException.class,
                MethodArgumentNotValidException.class,
                IllegalArgumentException.class // Also handle IllegalArgumentException here for consistency
        })
        @ResponseBody
        public Result handleBadRequestException(Exception e, HttpServletRequest request) {
            logger.warn("Bad Request: {}, URL: {}", e.getMessage(), request.getRequestURL()); // Use warn for client errors

            String message = "请求参数错误";

            // 参数校验失败，获取具体的错误信息
            if (e instanceof BindException) {
                BindingResult bindingResult = ((BindException) e).getBindingResult();
                message = getBindingResultErrorMessage(bindingResult);
            } else if (e instanceof MethodArgumentNotValidException) {
                BindingResult bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
                message = getBindingResultErrorMessage(bindingResult);
            } else if (e instanceof IllegalArgumentException) {
                message = e.getMessage(); // Use the message from IllegalArgumentException
            }

            return Result.error(message);
        }

        /**
         * 处理404异常 (Not Found)
         */
        @ResponseStatus(HttpStatus.NOT_FOUND)
        @ExceptionHandler(NoHandlerFoundException.class)
        @ResponseBody
        public Result handleNotFoundException(NoHandlerFoundException e, HttpServletRequest request) {
            logger.warn("Resource Not Found: {}, URL: {}", e.getMessage(), request.getRequestURL());
            request.setAttribute("javax.servlet.error.status_code", HttpStatus.NOT_FOUND.value());
            return Result.error("请求的资源不存在");
        }

        /**
         * 处理405异常 (Method Not Allowed)
         */
        @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        @ResponseBody
        public Result handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
            logger.warn("Method Not Allowed: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error("不支持的请求方法: " + e.getMethod());
        }

        /**
         * 处理415异常 (Unsupported Media Type)
         */
        @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        @ResponseBody
        public Result handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
            logger.warn("Unsupported Media Type: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error("不支持的媒体类型: " + e.getContentType());
        }

        /**
         * 处理文件上传大小限制异常 (Payload Too Large)
         */
        @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        @ResponseBody
        public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
            logger.warn("File Size Limit Exceeded: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error("上传的文件过大，请减小文件大小后重试");
        }

        /**
         * 处理IO异常 (Internal Server Error)
         */
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler(IOException.class)
        @ResponseBody
        public Result handleIOException(IOException e, HttpServletRequest request) {
            logger.error("IO Exception: {}, URL: {}", e.getMessage(), request.getRequestURL(), e); // Log stack trace
            return Result.error("文件读写错误"); // Avoid exposing detailed error message
        }

        /**
         * 处理SQL异常 (Internal Server Error)
         */
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler(SQLException.class)
        @ResponseBody
        public Result handleSQLException(SQLException e, HttpServletRequest request) {
            logger.error("SQL Exception: {}, URL: {}", e.getMessage(), request.getRequestURL(), e); // Log stack trace
            return Result.error("数据库操作错误"); // Avoid exposing detailed error message
        }

        /**
         * 获取参数绑定错误信息
         */
        private String getBindingResultErrorMessage(BindingResult bindingResult) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            if (!fieldErrors.isEmpty()) {
                FieldError fieldError = fieldErrors.get(0); // Return the first error message
                return fieldError.getDefaultMessage();
            }
            return "参数校验失败";
        }
    }

    /**
     * 业务异常处理器 - 处理业务逻辑异常和最终未捕获异常
     * 优先级较高 (Order=1)
     */
    @RestControllerAdvice
    @Order(1) // Higher priority
    public static class BusinessExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(BusinessExceptionHandler.class);

        /**
         * 处理自定义业务异常 (CustomException from exception package)
         */
        @ExceptionHandler(CustomException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST) // Typically Bad Request for general business errors
        @ResponseBody
        public Result handleCustomException(HttpServletRequest request, CustomException e) {
            logger.warn("Business Exception: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error(e.getMessage());
        }

        /* // 由 RestAuthenticationEntryPoint 处理
        @ExceptionHandler(AuthenticationException.class) // Use the imported class
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        @ResponseBody
        public Result handleAuthenticationException(HttpServletRequest request, AuthenticationException e) {
            logger.warn("Authentication Failed: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error(e.getMessage()).code(HttpStatus.UNAUTHORIZED.value()); // Optionally set code
        }
        */

        /* // 由 RestAccessDeniedHandler 处理
        @ExceptionHandler(PermissionDeniedException.class) // Use the imported class
        @ResponseStatus(HttpStatus.FORBIDDEN)
        @ResponseBody
        public Result handlePermissionDeniedException(HttpServletRequest request, PermissionDeniedException e) {
            logger.warn("Permission Denied: {}, URL: {}", e.getMessage(), request.getRequestURL());
            return Result.error(e.getMessage()).code(HttpStatus.FORBIDDEN.value()); // Optionally set code
        }
        */

        /**
         * 处理所有未捕获的异常 (兜底)
         * @param request 请求对象
         * @param e 异常
         * @return 标准错误响应
         */
        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ResponseBody
        public Result handleException(HttpServletRequest request, Exception e) {
            // Log detailed error for unexpected exceptions
            logger.error("Unhandled Exception: {}, URL: {}", e.getMessage(), request.getRequestURL(), e);
            // Return a generic error message to the client
            return Result.error("系统内部错误，请联系管理员");
        }
    }
}