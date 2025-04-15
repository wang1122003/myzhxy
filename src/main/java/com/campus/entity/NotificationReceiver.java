package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 通知接收者实体类
 */
@Data
@TableName("notification_receiver")
public class NotificationReceiver {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 通知ID
     */
    @TableField("notification_id")
    private Integer notificationId;
    
    /**
     * 接收者ID
     */
    @TableField("receiver_id")
    private Integer receiverId;
    
    /**
     * 接收者类型（如：学生、教师）
     */
    @TableField("receiver_type")
    private String receiverType;
    
    /**
     * 阅读状态（0-未读，1-已读）
     */
    @TableField("read_status")
    private Integer readStatus;
    
    /**
     * 阅读时间
     */
    @TableField("read_time")
    private Date readTime;
    
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
     * 通知信息
     */
    @TableField(exist = false)
    private Notification notification;
    
    /**
     * 接收者信息
     */
    @TableField(exist = false)
    private User receiver;
}