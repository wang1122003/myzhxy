package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 行政班级实体类
 */
@Data
@TableName("clazz")
public class Clazz implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 班级ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 班级名称
     */
    @TableField("name")
    private String name;

    /**
     * 年级
     */
    @TableField("grade")
    private String grade;

    /**
     * 所属院系ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 辅导员/班主任教师ID
     */
    @TableField("instructor_id")
    private Long instructorId;

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
    private String departmentName;

    @TableField(exist = false)
    private String instructorName;
} 