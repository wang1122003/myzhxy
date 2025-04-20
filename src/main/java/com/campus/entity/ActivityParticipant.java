package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动参与者关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity_participant") // 假设数据库表名为 activity_participant
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
    @TableField("activity_id")
    private Long activityId;

    /**
     * 参与用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 加入时间
     */
    @TableField("join_time")
    private Date joinTime;

    /**
     * 状态 (可选, 例如 1-已报名, 0-已取消)
     * 如果需要记录退出状态，可以添加此字段
     */
    // @TableField("status")
    // private Integer status;

    // 构造函数方便创建实例
    public ActivityParticipant(Long activityId, Long userId, Date joinTime) {
        this.activityId = activityId;
        this.userId = userId;
        this.joinTime = joinTime;
    }
} 