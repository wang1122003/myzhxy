package com.campus.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Student extends User {
    private static final long serialVersionUID = 1L;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 班级ID
     */
    private Long classId;
    
    /**
     * 班级名称
     */
    private String className;
    
    /**
     * 专业ID
     */
    private Long majorId;
    
    /**
     * 专业名称
     */
    private String majorName;
    
    /**
     * 学院ID
     */
    private Long collegeId;
    
    /**
     * 学院名称
     */
    private String collegeName;
    
    /**
     * 入学年份
     */
    private Integer enrollYear;
    
    /**
     * 学生状态：1-在读，2-休学，3-毕业，4-退学
     */
    private Integer studentStatus;
}