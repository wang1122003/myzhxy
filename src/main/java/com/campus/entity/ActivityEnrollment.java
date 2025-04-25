package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 活动报名记录实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity_enrollment")
public class ActivityEnrollment {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 报名时间
     */
    @TableField("enrollment_time")
    private Date enrollmentTime;

    // 可以添加其他字段，例如签到状态等
    // @TableField("check_in_status")
    // private Integer checkInStatus;
} 