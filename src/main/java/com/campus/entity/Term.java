package com.campus.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学期实体类
 */
@Data
public class Term implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 学期ID
     */
    private Long id;
    
    /**
     * 学期名称
     */
    private String name;
    
    /**
     * 学期代码
     */
    private String code;
    
    /**
     * 开始日期
     */
    private Date startDate;
    
    /**
     * 结束日期
     */
    private Date endDate;
    
    /**
     * 当前学期：0-否，1-是
     */
    private Integer current;
    
    /**
     * 学期状态：0-未开始，1-进行中，2-已结束
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