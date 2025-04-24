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
import java.util.List;
import java.util.Map;

/**
 * 通知实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 通知类型字符串版本
     */
    @TableField("type")
    private String type;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 通知状态：0-草稿, 1-已发布, 2-已撤回 (修改为字符串类型以适应数据库或前端数据)
     */
    @TableField("status")
    private String status;

    /**
     * 是否置顶：0-否, 1-是
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 阅读次数
     */
    @TableField("view_count")
    private Integer viewCount;
    
    /**
     * 发送者ID 
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 发布者ID (同步sender_id字段)
     */
    @TableField("publisher_id")
    private Long publisherId;

    /**
     * 发布者姓名
     */
    @TableField("publisher_name")
    private String publisherName;

    /**
     * 目标类型（如：全体、学院、班级、个人）
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID
     */
    @TableField("target_ids")
    private String targetId;

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
     * 附件信息 (JSON 字符串)
     * 存储 List<AttachmentInfo> 的 JSON 格式，例如:
     * "[{\"name\": \"file1.pdf\", \"url\": \"/uploads/uuid1.pdf\"}, {\"name\": \"image.png\", \"url\": \"/uploads/uuid2.png\"}]"
     */
    @TableField("attachments_json")
    private String attachmentsJson;

    /**
     * 接收者类型：1-全体，2-指定用户，3-指定角色，4-指定部门
     */
    @TableField(exist = false)
    private Integer receiverType;

    /**
     * 接收者ID列表 (根据 receiverType 类型确定含义)
     */
    @TableField(exist = false)
    private List<Long> receiverIds;

    /**
     * 已读状态 (非数据库字段，根据当前用户查询)
     */
    @TableField(exist = false)
    private Boolean isRead;

    /**
     * 附件文件信息列表 (非数据库字段，从 attachments JSON 解析得到)
     */
    @TableField(exist = false)
    private List<Map<String, String>> attachmentFiles;

    /**
     * 接收者姓名列表 (非数据库字段，用于前端展示)
     */
    @TableField(exist = false)
    private List<String> receiverNames;

    /**
     * 已读人数 (非数据库字段)
     */
    @TableField(exist = false)
    private Integer readCount;

    /**
     * 消息类型名称 (非数据库字段)
     */
    @TableField(exist = false)
    private String noticeTypeName;

    /**
     * 发送者用户信息 (通过关联查询得到，非数据库字段)
     */
    @TableField(exist = false)
    private User sender;
}