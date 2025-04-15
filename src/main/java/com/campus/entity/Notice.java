package com.campus.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 通知公告实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
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
     */
    private Integer noticeType;
    
    /**
     * 通知状态：0-草稿, 1-已发布, 2-已撤回
     */
    private Integer status;
    
    /**
     * 是否置顶：0-否, 1-是
     */
    private Integer isTop;
    
    /**
     * 阅读次数
     */
    private Integer viewCount;
    
    /**
     * 发布者ID
     */
    private Long publisherId;
    
    /**
     * 发布者姓名
     */
    private String publisherName;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 