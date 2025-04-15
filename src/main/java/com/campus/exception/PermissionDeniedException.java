package com.campus.exception;

/**
 * 权限拒绝异常
 * 用于表示用户没有权限访问某个资源
 */
public class PermissionDeniedException extends RuntimeException {
    
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