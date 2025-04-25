package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    ACTIVE("Active"),   // 活跃
    INACTIVE("Inactive"); // 非活跃/禁用

    @EnumValue // Instruct Mybatis-Plus to use this field for DB mapping
    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    // Optional: Add a method to get enum from string value if needed for API mapping
    public static UserStatus fromValue(String value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        // Handle unknown value
        throw new IllegalArgumentException("Unknown UserStatus value: " + value);
    }

    // getValue() is optional if using @EnumValue, but keep for general use
    public String getValue() {
        return value;
    }
} 