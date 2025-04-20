package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 教师实体类
 */
@Data
@TableName("teacher")
public class Teacher implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 教师ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 教师编号
     */
    @TableField("teacher_no")
    private String teacherNo;
    
    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期
     */
    @TableField("date_of_birth")
    private Date dateOfBirth;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 入职日期
     */
    @TableField("hire_date")
    private Date hireDate;

    /**
     * 院系ID
     */
    @TableField("department_id")
    private Long departmentId;
    
    /**
     * 职称
     */
    private String title;
    
    /**
     * 研究方向
     */
    @TableField("research_area")
    private String researchArea;
    
    /**
     * 状态
     */
    private String status;
    
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
    private String departmentName; // 院系名称
    @TableField(exist = false)
    private String username; // 用户名 (用于显示或关联查询)
}