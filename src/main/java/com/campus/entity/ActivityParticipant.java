package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动参与者实体类
 */
@Data
@TableName("activity_participant")
public class ActivityParticipant implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 活动ID
     */
    private Long activityId;
    
    /**
     * 参与者用户ID
     */
    private Long userId;
    
    /**
     * 报名时间
     */
    private Date registerTime;

    /**
     * 签到状态：0-未签到，1-已签到
     */
    private Integer checkInStatus;
    
    /**
     * 签到时间
     */
    private Date checkInTime;
    
    /**
     * 参与状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer status;
    
    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 