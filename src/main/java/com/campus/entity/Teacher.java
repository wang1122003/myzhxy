package com.campus.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教师实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Teacher extends User {
    private static final long serialVersionUID = 1L;
    
    /**
     * 教师工号
     */
    private String teacherNo;
    
    /**
     * 职称ID
     */
    private Long titleId;
    
    /**
     * 职称名称
     */
    private String titleName;
    
    /**
     * 所属部门ID
     */
    private Long departmentId;
    
    /**
     * 所属部门名称
     */
    private String departmentName;
    
    /**
     * 办公室位置
     */
    private String officeLocation;
    
    /**
     * 办公电话
     */
    private String officePhone;
    
    /**
     * 入职时间
     */
    private String hireDate;
    
    /**
     * 教师简介
     */
    private String introduction;
}