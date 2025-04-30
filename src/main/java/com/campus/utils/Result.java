package com.campus.utils;

import java.io.Serializable;

/**
 * 统一返回结果类
 *
 * @param <T> 数据类型
 */
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    public Result() {
    }

    public Result(boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public Result(boolean success, Integer code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果
     *
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<T>(true, 200, "操作成功");
    }

    /**
     * 成功返回结果
     *
     * @param message 提示信息
     * @return 成功结果
     */
    public static <T> Result<T> success(String message) {
        return new Result<T>(true, 200, message);
    }

    /**
     * 成功返回结果
     *
     * @param data 数据
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>(true, 200, "操作成功", data);
    }

    /**
     * 成功返回结果
     *
     * @param message 提示信息
     * @param data    数据
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<T>(true, 200, message, data);
    }

    /**
     * 失败返回结果
     *
     * @return 失败结果
     */
    public static <T> Result<T> error() {
        return new Result<T>(false, 500, "操作失败");
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     * @return 失败结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<T>(false, 500, message);
    }

    /**
     * 失败返回结果
     *
     * @param code    状态码
     * @param message 提示信息
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<T>(false, code, message);
    }

    /**
     * 参数验证失败返回结果
     *
     * @param message 提示信息
     * @return 参数验证失败结果
     */
    public static <T> Result<T> validateFailed(String message) {
        return new Result<T>(false, 400, message);
    }

    /**
     * 未登录返回结果
     *
     * @param message 提示信息
     * @return 未登录结果
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<T>(false, 401, message);
    }

    /**
     * 未授权返回结果
     *
     * @param message 提示信息
     * @return 未授权结果
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<T>(false, 403, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 设置状态码（链式调用）
     *
     * @param code 状态码
     * @return 当前 Result 实例
     */
    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }
} 