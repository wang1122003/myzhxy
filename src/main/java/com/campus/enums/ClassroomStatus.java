package com.campus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 教室状态枚举
 * DB Schema: 0-禁用，1-正常，2-维护中
 */
public enum ClassroomStatus {
    DISABLED(0, "禁用"),
    NORMAL(1, "正常"),
    MAINTENANCE(2, "维护中");

    @EnumValue // Use the integer code for DB mapping
    private final int code;
    private final String description;

    ClassroomStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // Optional: Find by code
    public static ClassroomStatus fromCode(Integer code) {
        if (code == null) return null;
        for (ClassroomStatus status : ClassroomStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null; // Or throw exception
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
} 