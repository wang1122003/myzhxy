package com.campus.service.impl;

import com.campus.dao.NotificationDao;
import com.campus.dao.NotificationReceiverDao;
import com.campus.entity.Notification;
import com.campus.entity.NotificationReceiver;
import com.campus.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 通知服务实现类
 * 用于管理系统消息、通知等功能
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationDao notificationDao;
    
    @Autowired
    private NotificationReceiverDao notificationReceiverDao;
    
    @Override
    public Notification getNotificationById(Long id) {
        return notificationDao.getNotificationById(id.intValue());
    }
    
    @Override
    public List<Notification> getAllNotifications() {
        return notificationDao.getAllNotifications();
    }
    
    @Override
    public List<Notification> getNotificationsByUserId(Long userId) {
        // 获取用户接收到的所有通知
        List<NotificationReceiver> receivers = notificationReceiverDao.getUserNotifications(
                userId.intValue(), "USER");
        
        List<Notification> notifications = new ArrayList<>();
        for (NotificationReceiver receiver : receivers) {
            Notification notification = notificationDao.getNotificationById(receiver.getNotificationId());
            if (notification != null) {
                notifications.add(notification);
            }
        }
        
        return notifications;
    }
    
    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        // 获取用户未读的通知
        List<NotificationReceiver> receivers = notificationReceiverDao.getUnreadNotifications(
                userId.intValue(), "USER");
        
        List<Notification> notifications = new ArrayList<>();
        for (NotificationReceiver receiver : receivers) {
            Notification notification = notificationDao.getNotificationById(receiver.getNotificationId());
            if (notification != null) {
                notifications.add(notification);
            }
        }
        
        return notifications;
    }
    
    @Override
    @Transactional
    public boolean createNotification(Notification notification) {
        // 设置创建时间
        if (notification.getCreateTime() == null) {
            notification.setCreateTime(new Date());
        }
        
        return notificationDao.insert(notification) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateNotification(Notification notification) {
        return notificationDao.updateById(notification) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteNotification(Long id) {
        // 获取相关的接收记录
        List<NotificationReceiver> receivers = notificationReceiverDao.getByNotificationId(id.intValue());
        
        // 删除接收记录
        for (NotificationReceiver receiver : receivers) {
            notificationReceiverDao.deleteNotificationReceiver(receiver.getId());
        }
        
        // 删除通知
        return notificationDao.deleteById(id.intValue()) > 0;
    }
    
    @Override
    @Transactional
    public boolean markAsRead(Long id, Long userId) {
        // 根据通知ID和用户ID查找接收记录
        List<NotificationReceiver> receivers = notificationReceiverDao.getUserNotifications(
                userId.intValue(), "USER");
        
        for (NotificationReceiver receiver : receivers) {
            if (receiver.getNotificationId().equals(id.intValue())) {
                return notificationReceiverDao.markAsRead(receiver.getId()) > 0;
            }
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public boolean markAllAsRead(Long userId) {
        return notificationReceiverDao.markAllAsRead(userId.intValue(), "USER") > 0;
    }
    
    @Override
    @Transactional
    public boolean sendNotificationToUser(String title, String content, Long userId) {
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreateTime(new Date());
        notification.setType("USER"); // 个人通知
        notification.setPriority(1);  // 普通优先级
        notification.setStatus(1);    // 已发送状态
        notification.setSendTime(new Date());
        
        if (notificationDao.insert(notification) > 0) {
            // 创建通知接收者
            NotificationReceiver receiver = new NotificationReceiver();
            receiver.setNotificationId(notification.getId());
            receiver.setReceiverId(userId.intValue());
            receiver.setReceiverType("USER");
            receiver.setReadStatus(0); // 0-未读
            receiver.setCreateTime(new Date());
            
            return notificationReceiverDao.insert(receiver) > 0;
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public boolean sendNotificationToUsers(String title, String content, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreateTime(new Date());
        notification.setType("GROUP"); // 群组通知
        notification.setPriority(1);   // 普通优先级
        notification.setStatus(1);     // 已发送状态
        notification.setSendTime(new Date());
        
        if (notificationDao.insert(notification) > 0) {
            // 创建通知接收者
            boolean allSuccess = true;
            for (Long userId : userIds) {
                NotificationReceiver receiver = new NotificationReceiver();
                receiver.setNotificationId(notification.getId());
                receiver.setReceiverId(userId.intValue());
                receiver.setReceiverType("USER");
                receiver.setReadStatus(0); // 0-未读
                receiver.setCreateTime(new Date());
                
                if (notificationReceiverDao.insert(receiver) <= 0) {
                    allSuccess = false;
                }
            }
            
            return allSuccess;
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public boolean sendNotificationToAllUsers(String title, String content) {
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreateTime(new Date());
        notification.setType("ALL"); // 全体通知
        notification.setPriority(2);  // 较高优先级
        notification.setStatus(1);    // 已发送状态
        notification.setSendTime(new Date());
        notification.setTargetType("ALL");
        
        return notificationDao.insert(notification) > 0;
    }
    
    @Override
    @Transactional
    public boolean sendSystemNotification(String title, String content) {
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreateTime(new Date());
        notification.setType("SYSTEM"); // 系统通知
        notification.setPriority(3);    // 高优先级
        notification.setStatus(1);      // 已发送状态
        notification.setSendTime(new Date());
        notification.setTargetType("SYSTEM");
        
        return notificationDao.insert(notification) > 0;
    }
} 