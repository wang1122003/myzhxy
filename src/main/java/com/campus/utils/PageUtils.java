package com.campus.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 * 替代 PageResult DTO 类
 */
public class PageUtils {

    /**
     * 将 MyBatis-Plus 的分页结果转换为 Map
     *
     * @param page MyBatis-Plus 分页结果
     * @return Map 格式的分页结果
     */
    public static <T> Map<String, Object> toMap(IPage<T> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        result.put("pages", page.getPages());
        result.put("records", page.getRecords());
        return result;
    }

    /**
     * 将 MyBatis-Plus 的分页结果转换为 Map，并对记录进行转换
     *
     * @param page      MyBatis-Plus 分页结果
     * @param converter 记录转换函数
     * @return Map 格式的分页结果
     */
    public static <T, R> Map<String, Object> toMap(IPage<T> page, Function<T, R> converter) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        result.put("pages", page.getPages());

        List<R> records = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());

        result.put("records", records);
        return result;
    }

    /**
     * 创建一个空的分页结果
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return Map 格式的空分页结果
     */
    public static Map<String, Object> emptyPage(long current, long size) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0L);
        result.put("current", current);
        result.put("size", size);
        result.put("pages", 0L);
        result.put("records", List.of());
        return result;
    }

    /**
     * 手动构建分页结果
     *
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页大小
     * @param records 记录列表
     * @return Map 格式的分页结果
     */
    public static <T> Map<String, Object> buildPage(long total, long current, long size, List<T> records) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("current", current);
        result.put("size", size);
        result.put("pages", Math.max(1, (int) Math.ceil((double) total / size)));
        result.put("records", records);
        return result;
    }

    /**
     * 分页结果类，提供兼容旧代码的PageResult
     */
    public static class PageResult<T> {
        private long total;
        private long current;
        private long size;
        private long pages;
        private List<T> records;

        public PageResult() {
        }

        public PageResult(long total, long current, long size, List<T> records) {
            this.total = total;
            this.current = current;
            this.size = size;
            this.pages = Math.max(1, (int) Math.ceil((double) total / size));
            this.records = records;
        }

        public PageResult(IPage<T> page) {
            this.total = page.getTotal();
            this.current = page.getCurrent();
            this.size = page.getSize();
            this.pages = page.getPages();
            this.records = page.getRecords();
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getCurrent() {
            return current;
        }

        public void setCurrent(long current) {
            this.current = current;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getPages() {
            return pages;
        }

        public void setPages(long pages) {
            this.pages = pages;
        }

        public List<T> getRecords() {
            return records;
        }

        public void setRecords(List<T> records) {
            this.records = records;
        }
    }
}