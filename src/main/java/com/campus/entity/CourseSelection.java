package com.campus.entity;

import java.util.Date;

/**
 * 选课记录模型
 */
public class CourseSelection {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Date selectionTime;
    private Integer status; // 0-待审核, 1-已选, 2-已退选
    private String remark;
    
    // 扩展属性，非数据库字段
    private String studentName;
    private String courseName;
    private String courseNo;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public Date getSelectionTime() {
        return selectionTime;
    }
    
    public void setSelectionTime(Date selectionTime) {
        this.selectionTime = selectionTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getCourseNo() {
        return courseNo;
    }
    
    public void setCourseNo(String courseNo) {
        this.courseNo = courseNo;
    }
} 