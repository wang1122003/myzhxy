package com.campus.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Notification Data Transfer Object
 */
@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private Integer type; // Or String depending on entity
    private Long publisherId;
    private String publisherName; // Optional, might be set in service
    private Date publishTime;
    private Boolean isTop; // 是否置顶 - DTO中用Boolean方便处理，实体中可能是Integer
    private Integer viewCount;
    private Date createTime;
    private Date updateTime;

    // List of attachment details
    // 与实体类保持完全一致
    private String status;
    private String targetType; 
    private List<AttachmentInfo> attachments;

    /**
     * Inner class to hold attachment information
     */
    @Data
    public static class AttachmentInfo {
        private String name; // Original filename
        private String url;  // Stored file URL
    }
}