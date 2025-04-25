package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 选课信息实体类
 */
@Data
@TableName("course_selection")
@EqualsAndHashCode(callSuper = false)
public class CourseSelection implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 选课ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生用户ID
     */
    @TableField("user_id")
    private Long userId;

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
     * 选课时间
     */
    @TableField("selection_time")
    private Date selectionTime;

    /**
     * 选课状态（String, e.g., "Selected", "Dropped"）
     */
    @TableField("status")
    private String status;

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
    @TableField("final_exam_score")
    private BigDecimal finalExamScore;

    /**
     * 总分值
     */
    @TableField("score_value")
    private BigDecimal scoreValue;

    /**
     * 等级
     */
    @TableField("grade")
    private String grade;

    /**
     * 绩点
     */
    @TableField("gpa")
    private BigDecimal gpa;

    /**
     * 评语
     */
    @TableField("comment")
    private String comment;

    /**
     * 评分日期
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

    // 非数据库字段，用于关联查询显示
    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String termName;

    public String getStatus() {
        return status;
    }
} 