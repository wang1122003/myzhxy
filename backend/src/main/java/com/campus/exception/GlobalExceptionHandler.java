package com.campus.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 替代自定义异常类，统一处理各种异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理数据库相关异常
     */
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public ResponseEntity<Map<String, Object>> handleSQLException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "数据库操作异常");
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.BAD_REQUEST.value());

        Map<String, String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        response.put("message", "参数校验失败");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.BAD_REQUEST.value());

        Map<String, String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        response.put("message", "参数绑定失败");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.BAD_REQUEST.value());

        Map<String, String> errors = e.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        response.put("message", "约束违反");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理认证相关异常
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(SecurityException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.UNAUTHORIZED.value());
        response.put("message", "认证失败");
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", "服务器内部错误");
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}