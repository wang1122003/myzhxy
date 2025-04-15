package com.campus.service;

import java.util.List;

import com.campus.entity.Notification;

/**
 * 通知服务接口
 * 用于管理系统消息、通知等功能
 */
public interface NotificationService {
    
    /**
     * 获取通知详情
     * @param id 通知ID
     * @return 通知对象
     */
    Notification getNotificationById(Long id);
    
    /**
     * 获取所有通知
     * @return 通知列表
     */
    List<Notification> getAllNotifications();
    
    /**
     * 获取用户通知
     * @param userId 用户ID
     * @return 通知列表
     */
    List<Notification> getNotificationsByUserId(Long userId);
    
    /**
     * 获取未读通知
     * @param userId 用户ID
     * @return 通知列表
     */
    List<Notification> getUnreadNotifications(Long userId);
    
    /**
     * 创建通知
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean createNotification(Notification notification);
    
    /**
     * 更新通知
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean updateNotification(Notification notification);
    
    /**
     * 删除通知
     * @param id 通知ID
     * @return 是否成功
     */
    boolean deleteNotification(Long id);
    
    /**
     * 标记通知为已读
     * @param id 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long id, Long userId);
    
    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 发送通知给指定用户
     * @param title 通知标题
     * @param content 通知内容
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean sendNotificationToUser(String title, String content, Long userId);
    
    /**
     * 发送通知给多个用户
     * @param title 通知标题
     * @param content 通知内容
     * @param userIds 用户ID列表
     * @return 是否成功
     */
    boolean sendNotificationToUsers(String title, String content, List<Long> userIds);
    
    /**
     * 发送通知给所有用户
     * @param title 通知标题
     * @param content 通知内容
     * @return 是否成功
     */
    boolean sendNotificationToAllUsers(String title, String content);
    
    /**
     * 发送系统通知
     * @param title 通知标题
     * @param content 通知内容
     * @return 是否成功
     */
    boolean sendSystemNotification(String title, String content);
} 