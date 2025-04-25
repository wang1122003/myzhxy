package com.campus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学期枚举
 * 包含常用的学期代码和名称
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT) // For potential direct serialization
public enum Term {
    // --- 定义学期 --- 
    // 命名规则: YEAR_YEAR_SEMESTER (例如 S2024_2025_1 表示 2024-2025学年第一学期)
    // 增加一个 isCurrent 标志来标识当前学期 (需要手动更新)

    S2023_2024_1("2023-2024-1", "2023-2024学年第一学期", false),
    S2023_2024_2("2023-2024-2", "2023-2024学年第二学期", false),
    S2024_2025_1("2024-2025-1", "2024-2025学年第一学期", true), // 假设这是当前学期
    S2024_2025_2("2024-2025-2", "2024-2025学年第二学期", false);
    // --- 可根据需要添加更多学期 --- 

    private final String code; // 学期代码，例如 "2024-2025-1"
    private final String displayName; // 学期显示名称
    private final boolean isCurrent; // 是否为当前学期

    Term(String code, String displayName, boolean isCurrent) {
        this.code = code;
        this.displayName = displayName;
        this.isCurrent = isCurrent;
    }

    /**
     * 获取所有学期（按代码降序排序）
     *
     * @return 学期列表
     */
    public static List<Term> getAllTermsSorted() {
        return Arrays.stream(Term.values())
                .sorted(Comparator.comparing(Term::getCode).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取当前学期
     *
     * @return 当前学期枚举实例，如果未定义则返回null
     */
    public static Term getCurrentTerm() {
        return Arrays.stream(Term.values())
                .filter(Term::isCurrent)
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据学期代码查找学期枚举
     *
     * @param code 学期代码
     * @return 对应的学期枚举，如果找不到则返回null
     */
    public static Term fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(Term.values())
                .filter(term -> term.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    // --- 静态方法 --- 

    @JsonValue // Jackson会使用这个方法获取用于序列化的值
    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    // 可选: 返回 Map 结构方便前端使用
    public Map<String, Object> toMap() {
        return Map.of(
                "code", this.code,
                "displayName", this.displayName,
                "isCurrent", this.isCurrent
        );
    }
} 