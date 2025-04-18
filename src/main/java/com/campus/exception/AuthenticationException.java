package com.campus.exception;

/**
 * 认证异常
 * 用于表示用户认证失败
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     */
    public AuthenticationException() {
        super("认证失败，请重新登录");
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause   异常原因
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
} 