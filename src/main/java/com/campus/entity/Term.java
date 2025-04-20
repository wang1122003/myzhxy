package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学期实体类
 */
@Data
@TableName("term")
public class Term implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 学期ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 学期名称
     */
    @TableField("term_name")
    private String termName;
    
    /**
     * 学期代码
     */
    @TableField("code")
    private String code;
    
    /**
     * 开始日期
     */
    @TableField("start_date")
    private Date startDate;
    
    /**
     * 结束日期
     */
    @TableField("end_date")
    private Date endDate;
    
    /**
     * 当前学期：0-否，1-是
     */
    @TableField("current")
    private Integer current;
    
    /**
     * 学期状态：0-未开始，1-进行中，2-已结束
     */
    @TableField("status")
    private Integer status;
    
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
} 