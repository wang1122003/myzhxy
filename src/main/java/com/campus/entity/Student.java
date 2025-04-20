package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生实体类
 */
@Data
@TableName("student")
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 学生ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 学号
     */
    @TableField("student_no")
    private String studentNo;
    
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
     * 入学日期
     */
    @TableField("enrollment_date")
    private Date enrollmentDate;

    /**
     * 毕业日期
     */
    @TableField("graduation_date")
    private Date graduationDate;

    /**
     * 院系ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 专业
     */
    private String major;

    /**
     * 所属班级ID
     */
    @TableField("class_id")
    private Long classId;
    
    /**
     * 学籍状态
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