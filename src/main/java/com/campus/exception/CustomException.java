package com.campus.exception;

/**
 * 自定义异常类，用于业务异常处理
 */
public class CustomException extends RuntimeException {

    private Integer code;

    /**
     * 默认构造函数
     */
    public CustomException() {
        super();
    }

    /**
     * 带消息的构造函数
     *
     * @param message 异常消息
     */
    public CustomException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 带消息和错误码的构造函数
     *
     * @param message 异常消息
     * @param code    错误码
     */
    public CustomException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    /**
     * 带原因的构造函数
     *
     * @param message 异常消息
     * @param cause   原因
     */
    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    /**
     * 完整构造函数
     *
     * @param message 异常消息
     * @param cause   原因
     * @param code    错误码
     */
    public CustomException(String message, Throwable cause, Integer code) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置错误码
     *
     * @param code 错误码
     */
    public void setCode(Integer code) {
        this.code = code;
    }
} 