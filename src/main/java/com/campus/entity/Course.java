package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程实体类
 */
@Data
@TableName("course")
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 课程ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程编号
     */
    @TableField("code")
    private String courseNo;
    
    /**
     * 课程名称
     */
    @TableField("name")
    private String courseName;
    
    /**
     * 课程学分
     */
    @TableField("credit")
    private BigDecimal credit;
    
    /**
     * 课程类型：1-必修课，2-选修课，3-通识等
     */
    @TableField("course_type")
    private Integer courseType;
    
    /**
     * 课程简介
     */
    @TableField("description")
    private String introduction;
    
    /**
     * 课程授课教师ID
     */
    @TableField("teacher_id")
    private Long teacherId;

    /**
     * 授课教师名称(非数据库字段)
     */
    @TableField(exist = false)
    private String teacherName;

    /**
     * 课程状态 (String, e.g., "Active", "Inactive")
     */
    @TableField("status")
    private String status;
    
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
     * 学期信息
     */
    @TableField("term_info")
    private String termInfo;
}