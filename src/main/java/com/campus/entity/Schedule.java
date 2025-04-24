package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 课表实体类
 */
@Data
@TableName("schedule")
public class Schedule implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 课表ID (主键)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID (关联 course 表)
     */
    @TableField("course_id")
    private Long courseId;
    
    /**
     * 教师ID (关联 teacher/user 表)
     */
    @TableField("teacher_id")
    private Long teacherId;
    
    /**
     * 教室ID (关联 classroom 表)
     */
    @TableField("classroom_id")
    private Long classroomId;
    
    /**
     * 星期几 (例如: Monday, Tuesday... 或 1-7)
     */
    @TableField("day_of_week")
    private String dayOfWeek;
    
    /**
     * 上课开始时间 (时分秒，通常和 week_day 配合)
     */
    @TableField("start_time")
    private Date startTime;
    
    /**
     * 上课结束时间 (时分秒)
     */
    @TableField("end_time")
    private Date endTime;
    
    /**
     * 课程起始周次
     */
    @TableField("start_week")
    private Integer startWeek;
    
    /**
     * 课程结束周次
     */
    @TableField("end_week")
    private Integer endWeek;
    
    /**
     * 学期ID (关联 term 表)
     */
    @TableField("term_id")
    private Long termId;
    
    /**
     * 班级ID (关联 class 表)
     */
    @TableField("class_id")
    private Long classId;
    
    /**
     * 班级名称 (非数据库字段, 通过JOIN查询得到)
     */
    @TableField(exist = false)
    private String className;
    
    /**
     * 课程名称 (非数据库字段, 通过JOIN查询得到)
     */
    @TableField(exist = false)
    private String courseName;

    /**
     * 教师名称 (非数据库字段, 通过JOIN查询得到)
     */
    @TableField(exist = false)
    private String teacherName;

    /**
     * 学期名称 (非数据库字段, 通过JOIN查询得到)
     */
    @TableField(exist = false)
    private String termName;

    /**
     * 教室名称 (非数据库字段, 通过JOIN查询得到)
     */
    @TableField(exist = false)
    private String classroomName;

    /**
     * 课表状态 (例如: 1-正常, 0-已取消, 2-待定等)
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 记录更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 设置课程名称 (显式Setter，解决可能的MyBatis映射问题)
     *
     * @param courseName 课程名称
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * 设置教师名称 (显式Setter)
     *
     * @param teacherName 教师名称
     */
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    /**
     * 设置学期名称 (显式Setter)
     *
     * @param termName 学期名称
     */
    public void setTermName(String termName) {
        this.termName = termName;
    }

    /**
     * 设置班级名称 (显式Setter)
     *
     * @param className 班级名称
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * 设置教室名称 (显式Setter)
     *
     * @param classroomName 教室名称
     */
    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }
}