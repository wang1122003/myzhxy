package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private String courseNo;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 课程学分
     */
    private Float credit;
    
    /**
     * 课程类型：1-必修课，2-选修课
     */
    private Integer courseType;
    
    /**
     * 课程所属学院ID
     */
    private Long collegeId;
    
    /**
     * 课程所属学院名称
     */
    private String collegeName;
    
    /**
     * 课程简介
     */
    private String introduction;
    
    /**
     * 课程状态：0-未开课，1-已开课
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