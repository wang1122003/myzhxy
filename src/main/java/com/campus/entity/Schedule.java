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
     * 课表ID (主键, 自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID (外键, 关联 course 表)
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 教师ID (外键, 关联 user 表)
     */
    @TableField("teacher_id")
    private Long teacherId;

    /**
     * 教室ID (外键, 关联 classroom 表)
     */
    @TableField("classroom_id")
    private Long classroomId;

    /**
     * 星期几 (存储为数字 1-7, 1代表星期一)
     */
    @TableField("day_of_week")
    private Integer dayOfWeek;

    /**
     * 上课开始时间 (数据库 TIME 类型)
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 上课结束时间 (数据库 TIME 类型)
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
     * 学期信息 (例如 "2023-2024-1")
     */
    @TableField("term_info")
    private String termInfo;

    /**
     * 课程名称 (非数据库持久化字段, 通过 JOIN 查询填充)
     */
    @TableField(exist = false)
    private String courseName;

    /**
     * 教师名称 (非数据库持久化字段, 通过 JOIN 查询填充)
     */
    @TableField(exist = false)
    private String teacherName;

    /**
     * 教室名称 (非数据库持久化字段, 通过 JOIN 查询填充)
     */
    @TableField(exist = false)
    private String classroomName;

    /**
     * 教室所在建筑 (非数据库持久化字段, 通过 JOIN 查询填充)
     */
    @TableField(exist = false)
    private String building;

    /**
     * 状态 (例如, "1" 代表有效, "0" 代表无效或取消)
     */
    @TableField("status")
    private String status;

    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 记录最后更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    // --- Getters 和 Setters 由 @Data (Lombok) 自动生成 --- 
    // 如果 JOIN 映射有问题，可以取消注释或添加显式的 Setter 方法
    /*
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }
    */
}