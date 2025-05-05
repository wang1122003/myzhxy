package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 学期实体类
 */
@Data
@TableName("term")
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Term implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 学期ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学期代码，如：2022-2023-1
     */
    @TableField("code")
    private String code;

    /**
     * 学期名称，如：2022-2023学年第一学期
     */
    @TableField("name")
    private String name;

    /**
     * 开始日期
     */
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /**
     * 是否当前学期：0-否，1-是
     */
    @TableField("is_current")
    private Integer isCurrent;

    /**
     * 学年，如：2022-2023
     */
    @TableField("academic_year")
    private String academicYear;

    /**
     * 学期序号：1-第一学期，2-第二学期，3-第三学期（短学期/暑期）
     */
    @TableField("term_number")
    private Integer termNumber;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("creation_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creationTime;

    /**
     * 更新时间
     */
    @TableField("last_update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdateTime;

    /**
     * 描述
     */
    @TableField("description")
    private String description;
} 