package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
     * 课表ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 教室ID
     */
    private Long classroomId;
    
    /**
     * 教室名称
     */
    private String classroomName;
    
    /**
     * 上课日期（星期几：1-7表示周一至周日）
     */
    private Integer weekDay;
    
    /**
     * 开始时间（第几节课）
     */
    private Integer startTime;
    
    /**
     * 结束时间（第几节课）
     */
    private Integer endTime;
    
    /**
     * 开始周次
     */
    private Integer startWeek;
    
    /**
     * 结束周次
     */
    private Integer endWeek;
    
    /**
     * 学期ID
     */
    private Long termId;
    
    /**
     * 学期名称
     */
    private String termName;
    
    /**
     * 班级ID
     */
    private Long classId;
    
    /**
     * 班级名称
     */
    private String className;
    
    /**
     * 状态（0：正常，1：已取消，2：已调整）
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