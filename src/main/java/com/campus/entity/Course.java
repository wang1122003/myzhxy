package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
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
    private Float credit;
    
    /**
     * 课程类型：1-必修课，2-选修课
     */
    @TableField("course_type")
    private Integer courseType;
    
    /**
     * 课程所属学院ID
     */
    @TableField(exist = false) // 非数据库字段
    private Long collegeId;
    
    /**
     * 课程所属学院名称
     */
    @TableField(exist = false) // 非数据库字段
    private String collegeName;
    
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
     * 课时数(非数据库字段)
     */
    @TableField(exist = false)
    private Integer hours;

    /**
     * 课程状态(非数据库字段)
     */
    @TableField(exist = false)
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

    /**
     * 课程代码
     */
    @TableField("course_code")
    private String courseCode;

    // 由于 Lombok 问题，显式添加可能缺失的 getter 方法
    public String getCourseCode() {
        return this.courseCode;
    }

    // 如果需要，稍后添加显式 setter 方法
    // public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
}