package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知公告与附件关联表
 */
@Data
@TableName("notification_attachment")
public class NotificationAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知ID
     */
    private Long notificationId;

    /**
     * 附件ID (假设附件信息存储在单独的 attachment 表中)
     */
    private Long attachmentId;

    /**
     * 创建时间
     */
    private Date createTime;

    // 可以根据需要添加其他关联信息，例如附件 URL (如果直接存储URL)
    // private String attachmentUrl;
} 