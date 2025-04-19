package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
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
    private Long studentId;
    
    /**
     * 学生姓名
     */
    @TableField(exist = false)
    private String studentName;
    
    /**
     * 学号
     */
    @TableField(exist = false)
    private String studentNo;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    @TableField(exist = false)
    private String courseName;
    
    /**
     * 教师ID
     */
    @TableField(exist = false)
    private Long teacherId;
    
    /**
     * 教师姓名
     */
    @TableField(exist = false)
    private String teacherName;
    
    /**
     * 学期ID
     */
    @TableField(exist = false)
    private Long termId;
    
    /**
     * 学期名称
     */
    @TableField(exist = false)
    private String termName;
    
    /**
     * 平时成绩
     */
    @TableField(exist = false)
    private Float regularScore;
    
    /**
     * 期中成绩
     */
    @TableField(exist = false)
    private Float midtermScore;
    
    /**
     * 期末成绩
     */
    @TableField(exist = false)
    private Float finalScore;
    
    /**
     * 总评成绩 (映射到数据库的 score 列)
     */
    @TableField("score") // 显式指定映射到数据库的 score 列
    private Float totalScore; 
    
    /**
     * 成绩等级：A、B、C、D、E、F
     */
    @TableField(exist = false)
    private String grade;
    
    /**
     * 备注
     */
    @TableField(exist = false)
    private String remark;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 关联课程
     */
    @TableField(exist = false)
    private Course course;
    
    /**
     * 关联学生
     */
    @TableField(exist = false)
    private Student student;
    
    /**
     * 关联教师
     */
    @TableField(exist = false)
    private Teacher teacher;
    
    /**
     * 关联学期
     */
    @TableField(exist = false)
    private Term term;
}