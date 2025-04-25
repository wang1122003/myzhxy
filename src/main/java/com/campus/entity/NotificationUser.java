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
 * 用户通知关联表实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_user")
public class NotificationUser {

    /**
     * 主键ID (可以自增，也可以不需要，取决于是否需要独立操作此关联记录)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知ID
     */
    @TableField("notification_id")
    private Long notificationId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 是否已读 (0: 未读, 1: 已读)
     */
    @TableField("is_read")
    private Boolean isRead;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private Date readTime;

    /**
     * 记录创建时间 (关联创建时间)
     */
    @TableField("create_time")
    private Date createTime;

    // 构造函数，用于简化 ServiceImpl 中的创建
    public NotificationUser(Long notificationId, Long userId, Boolean isRead, Date readTime) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.isRead = isRead;
        this.readTime = readTime;
        this.createTime = new Date(); // Set creation time on instantiation
    }

    // 手动添加 getter/setter 尝试解决编译问题
    public Long getUserId() {
        return userId;
    }

    public Boolean isRead() {
        return isRead;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public void setReadTime(Date time) {
        readTime = time;
    }
} 