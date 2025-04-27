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
     * 选课ID
     */
    @TableField("selection_id")
    private Long selectionId;

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
     * 学期信息
     */
    @TableField("term_info")
    private String termInfo;

    /**
     * 总成绩
     */
    @TableField("total_score")
    private BigDecimal totalScore;

    /**
     * 等级成绩 (A, B+, C等)
     */
    @TableField("grade")
    private String grade;

    /**
     * 绩点
     */
    private BigDecimal gpa;

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
     * 成绩类型
     */
    @TableField("score_type")
    private String scoreType;

    /**
     * 评语
     */
    @TableField(exist = false)
    private String comment;

    /**
     * 评定日期
     */
    @TableField("evaluation_date")
    private Date evaluationDate;

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

    /**
     * 学生对象（非数据库字段）
     */
    @TableField(exist = false)
    private User student;

    /**
     * 课程对象（非数据库字段）
     */
    @TableField(exist = false)
    private Course course;
}