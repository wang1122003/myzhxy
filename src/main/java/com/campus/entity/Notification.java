package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 通知实体类
 */
@Data
@TableName("notification")
public class Notification {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 通知类型
     */
    private String type;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;
    
    /**
     * 目标类型（如：全体、学院、班级、个人）
     */
    @TableField("target_type")
    private String targetType;
    
    /**
     * 目标ID
     */
    @TableField("target_id")
    private Long targetId;
    
    /**
     * 状态（0-草稿，1-已发送，2-已撤回）
     */
    private Integer status;
    
    /**
     * 发送时间
     */
    @TableField("send_time")
    private Date sendTime;
    
    /**
     * 过期时间
     */
    @TableField("expire_time")
    private Date expireTime;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
    
    /**
     * 发送者信息
     */
    @TableField(exist = false)
    private User sender;
}