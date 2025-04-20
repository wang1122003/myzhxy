package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动报名记录实体类
 */
@Data
@TableName("activity_registration")
public class ActivityRegistration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报名记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 报名用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 报名时间
     */
    @TableField("registration_time")
    private Date registrationTime;

    /**
     * 报名状态 (例如: 1-已报名, 0-已取消) - 可选
     */
    @TableField("status")
    private Integer status;

    // 可根据需要添加其他字段，如联系方式、备注等

} 