package com.campus.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 校园活动实体类
 */
@Data
public class Activity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 活动标题
     */
    private String title;
    
    /**
     * 活动内容
     */
    private String content;
    
    /**
     * 活动类型：1-娱乐比赛，2-公益服务，3-学术讲座，4-社团活动，5-其他
     */
    private Integer activityType;
    
    /**
     * 活动地点
     */
    private String location;
    
    /**
     * 开始时间
     */
    private Date startTime;
    
    /**
     * 结束时间
     */
    private Date endTime;
    
    /**
     * 组织者ID
     */
    private Long organizerId;
    
    /**
     * 组织者名称
     */
    private String organizerName;
    
    /**
     * 联系方式
     */
    private String contact;
    
    /**
     * 活动海报URL
     */
    private String posterUrl;
    
    /**
     * 报名人数上限
     */
    private Integer maxParticipants;
    
    /**
     * 当前报名人数
     */
    private Integer currentParticipants;
    
    /**
     * 活动状态：0-未开始，1-进行中，2-已结束
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}