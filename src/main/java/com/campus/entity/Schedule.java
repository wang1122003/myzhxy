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
     * 学期信息 (数据库字段)
     */
    @TableField("term_info")
    private String termInfo;

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
     * 教室名称 (非数据库字段, 通过JOIN查询得到)
     */
    @TableField(exist = false)
    private String classroomName;

    /**
     * 状态 (e.g., "Active", "Inactive")
     */
    @TableField("status")
    private String status;

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
     * 设置教室名称 (显式Setter)
     *
     * @param classroomName 教室名称
     */
    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}