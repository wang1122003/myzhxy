package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 用户类型枚举
 */
public enum UserType {
    ADMIN("Admin"), // 管理员
    TEACHER("Teacher"), // 教师
    STUDENT("Student"); // 学生

    @EnumValue // Instruct Mybatis-Plus to use this field for DB mapping
    private final String value;

    UserType(String value) {
        this.value = value;
    }

    // Optional: Add a method to get enum from string value if needed for API mapping
    public static UserType fromValue(String value) {
        for (UserType type : UserType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        // Handle unknown value, e.g., throw exception or return null
        throw new IllegalArgumentException("Unknown UserType value: " + value);
    }

    // getValue() is optional if using @EnumValue, but keep for general use
    public String getValue() {
        return value;
    }
} 