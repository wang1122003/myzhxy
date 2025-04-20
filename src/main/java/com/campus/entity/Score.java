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
 * 成绩实体类
 */
@Data
@TableName("score")
public class Score implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 成绩ID
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
     * 学期
     */
    private String term;
    
    /**
     * 平时成绩
     */
    @TableField("regular_score")
    private BigDecimal regularScore;
    
    /**
     * 期中成绩
     */
    @TableField("midterm_score")
    private BigDecimal midtermScore;
    
    /**
     * 期末成绩
     */
    @TableField("final_score")
    private BigDecimal finalScore;
    
    /**
     * 总成绩
     */
    @TableField("total_score")
    private BigDecimal totalScore;
    
    /**
     * 绩点
     */
    private BigDecimal gpa;
    
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

    // 非数据库字段
    @TableField(exist = false)
    private String courseName; // 课程名称
    @TableField(exist = false)
    private String studentName; // 学生名称
}