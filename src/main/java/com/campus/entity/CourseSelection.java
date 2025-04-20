package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 选课实体类
 */
@Data
@TableName("course_selection")
public class CourseSelection implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 选课ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    @TableField("student_id")
    private Long studentId;

    /**
     * 课程ID
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 学期ID
     */
    @TableField("term_id")
    private Long termId;

    /**
     * 选课时间
     */
    @TableField("selection_time")
    private Date selectionTime;

    /**
     * 状态：1-已选，0-已退选
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