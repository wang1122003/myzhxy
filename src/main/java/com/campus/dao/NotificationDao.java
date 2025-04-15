package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Notification;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 通知数据访问接口
 */
@Repository
public interface NotificationDao extends BaseMapper<Notification> {
    
    /**
     * 获取所有通知
     * @return 通知列表
     */
    List<Notification> getAllNotifications();
    
    /**
     * 根据ID获取通知
     * @param id 通知ID
     * @return 通知对象
     */
    Notification getNotificationById(Integer id);
    
    /**
     * 根据类型获取通知
     * @param type 通知类型
     * @return 通知列表
     */
    List<Notification> getNotificationsByType(String type);
    
    /**
     * 根据优先级获取通知
     * @param priority 优先级
     * @return 通知列表
     */
    List<Notification> getNotificationsByPriority(Integer priority);
    
    /**
     * 根据发送者ID获取通知
     * @param senderId 发送者ID
     * @return 通知列表
     */
    List<Notification> getNotificationsBySenderId(Integer senderId);
    
    /**
     * 根据目标类型和ID获取通知
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 通知列表
     */
    List<Notification> getNotificationsByTarget(@Param("targetType") String targetType, @Param("targetId") Integer targetId);
    
    /**
     * 获取有效通知
     * @return 通知列表
     */
    List<Notification> getActiveNotifications();
}