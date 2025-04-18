package com.campus.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果 DTO
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    /**
     * 当前页码
     */
    private long current;

    /**
     * 每页数量
     */
    private long size;

    public PageResult(long total, List<T> records, long current, long size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
    }

    // 可选：添加一个简单的构造函数或静态工厂方法
    public static <T> PageResult<T> success(long total, List<T> records, long current, long size) {
        return new PageResult<>(total, records, current, size);
    }
} 