package com.campus.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动参与者实体类
 */
public class ActivityParticipant implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 关联活动ID
     */
    private Long activityId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 用户类型：0-学生，1-教师，2-管理员
     */
    private Integer userType;
    
    /**
     * 报名时间
     */
    private Date signupTime;
    
    /**
     * 签到时间
     */
    private Date checkInTime;
    
    /**
     * 参与状态：0-待确认，1-已确认，2-已取消
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    // 状态常量
    public static final int STATUS_PENDING = 0;    // 待参加
    public static final int STATUS_ATTENDED = 1;   // 已参加
    public static final int STATUS_CANCELLED = 2;  // 已取消

    // 用户类型常量
    public static final int USER_TYPE_STUDENT = 0;  // 学生
    public static final int USER_TYPE_TEACHER = 1;  // 教师
    public static final int USER_TYPE_ADMIN = 2;    // 管理员

    public ActivityParticipant() {
    }
    
    public ActivityParticipant(Long activityId, Long userId, String userName, Integer userType) {
        this.activityId = activityId;
        this.userId = userId;
        this.userName = userName;
        this.userType = userType;
        this.status = STATUS_PENDING;
        this.signupTime = new Date();
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Date getSignupTime() {
        return signupTime;
    }

    public void setSignupTime(Date signupTime) {
        this.signupTime = signupTime;
    }

    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    
    /**
     * 判断是否已签到
     * @return 是否已签到
     */
    public boolean isCheckedIn() {
        return this.checkInTime != null;
    }

    /**
     * 判断是否已取消
     * @return 是否已取消
     */
    public boolean isCancelled() {
        return STATUS_CANCELLED == this.status;
    }

    /**
     * 标记为已参加
     */
    public void markAsAttended() {
        this.status = STATUS_ATTENDED;
        this.checkInTime = new Date();
        this.updateTime = new Date();
    }

    /**
     * 标记为已取消
     */
    public void markAsCancelled() {
        this.status = STATUS_CANCELLED;
        this.updateTime = new Date();
    }

    @Override
    public String toString() {
        return "ActivityParticipant{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userType=" + userType +
                ", signupTime=" + signupTime +
                ", checkInTime=" + checkInTime +
                ", status=" + status +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
} 