package com.campus.utils;

/**
 * 系统状态枚举
 */
public enum StatusEnum {
    USER_ACTIVE(1, "正常"),
    USER_INACTIVE(0, "禁用"),
    TEACHER_ACTIVE(1, "在职"),
    TEACHER_INACTIVE(0, "离职"),
    STUDENT_ACTIVE(1, "在读"),
    STUDENT_INACTIVE(0, "休学"),
    COURSE_OPEN(1, "开课"),
    COURSE_CLOSED(0, "结课");

    private final int code;
    private final String description;

    StatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}