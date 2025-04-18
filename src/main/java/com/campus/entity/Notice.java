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
 * 通知公告实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
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
     * 通知类型：1-系统通知, 2-教学通知, 3-活动通知, 4-其他
     * 可以同时作为字符串类型存储
     */
    @TableField("notice_type")
    private Integer noticeType;

    /**
     * 通知类型字符串版本，兼容Notification表的type字段
     */
    @TableField("type")
    private String type;

    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 通知状态：0-草稿, 1-已发布, 2-已撤回
     */
    private Integer status;
    
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
     * 发布者ID (同时兼容publisherId和senderId)
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
    @TableField("target_id")
    private Long targetId;

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
     * 发送者信息
     */
    @TableField(exist = false)
    private User sender;
} 