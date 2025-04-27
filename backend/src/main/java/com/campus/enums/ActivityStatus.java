package com.campus.enums;

/**
 * 活动状态枚举
 */
public enum ActivityStatus {
    /**
     * 未开始 (或 待审核、草稿等，根据实际业务调整)
     */
    UPCOMING, // 或者 DRAFT, PENDING_APPROVAL 等

    /**
     * 进行中
     */
    ONGOING,

    /**
     * 已结束
     */
    ENDED,

    /**
     * 已取消
     */
    CANCELLED
} 