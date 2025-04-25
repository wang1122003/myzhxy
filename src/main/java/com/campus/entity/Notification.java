package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    // 统一状态枚举值
    @TableField("status")
    private String status; // DRAFT/PUBLISHED/RECALLED

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
     * 目标类型（如：全体、学院、班级、个人）
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID列表，JSON格式
     */
    @TableField("target_ids")
    private String targetIds;

    /**
     * 发送时间
     */
    @TableField("send_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 通知接收者JSON数据，替代原来的NotificationReceiver实体
     * 格式: [{receiverId: 1, isRead: 0, readTime: null}, ...]
     */
    @TableField("receivers_json")
    private String receiversJson;

    /**
     * 附件JSON数据，替代原来的NotificationAttachment实体
     * 格式: [{name: "file1.pdf", url: "/uploads/file1.pdf"}, ...]
     */
    @TableField("attachments_json")
    private String attachmentsJson;

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

    /**
     * 当前用户是否已读该通知 (用于Service层填充: getNotificationsForUser)
     */
    @TableField(exist = false)
    private boolean readForCurrentUser;

    // --- 手动添加 Getters/Setters (尝试解决 Lombok 问题) ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(String targetIds) {
        this.targetIds = targetIds;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getReceiversJson() {
        return receiversJson;
    }

    public void setReceiversJson(String receiversJson) {
        this.receiversJson = receiversJson;
    }

    public String getAttachmentsJson() {
        return attachmentsJson;
    }

    public void setAttachmentsJson(String attachmentsJson) {
        this.attachmentsJson = attachmentsJson;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public List<Map<String, String>> getAttachmentFiles() {
        return attachmentFiles;
    }

    public void setAttachmentFiles(List<Map<String, String>> attachmentFiles) {
        this.attachmentFiles = attachmentFiles;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }

    public String getNoticeTypeName() {
        return noticeTypeName;
    }

    public void setNoticeTypeName(String noticeTypeName) {
        this.noticeTypeName = noticeTypeName;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public boolean isReadForCurrentUser() {
        return readForCurrentUser;
    }

    public void setReadForCurrentUser(boolean readForCurrentUser) {
        this.readForCurrentUser = readForCurrentUser;
    }
}