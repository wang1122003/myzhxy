package com.campus.exception;

/**
 * 自定义通用业务异常类
 */
public class CustomException extends RuntimeException {

    // private Integer code; // Temporarily remove code field
    private static final long serialVersionUID = 1L; // Add serialVersionUID

    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /* Temporarily remove constructors with code
    public CustomException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public CustomException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
    */
} 