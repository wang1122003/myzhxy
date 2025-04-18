package com.campus.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Notice Data Transfer Object
 */
@Data
public class NoticeDTO {
    private Long id;
    private String title;
    private String content;
    private Integer type; // Or String depending on entity
    private Long publisherId;
    private String publisherName; // Optional, might be set in service
    private Date publishTime;
    private Integer status; // 0: draft, 1: published, 2: withdrawn
    private Boolean isTop; // 是否置顶 - DTO中用Boolean方便处理，实体中可能是Integer
    private Integer viewCount;
    private Date createTime;
    private Date updateTime;

    // List of attachment details
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