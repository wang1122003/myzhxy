package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.NotificationReceiver;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知接收者数据访问接口
 */
@Repository
public interface NotificationReceiverDao extends BaseMapper<NotificationReceiver> {
    
    /**
     * 获取用户的所有通知
     * @param receiverId 接收者ID
     * @param receiverType 接收者类型
     * @return 通知接收记录列表
     */
    List<NotificationReceiver> getUserNotifications(@Param("receiverId") Integer receiverId, 
                                                  @Param("receiverType") String receiverType);
    
    /**
     * 获取用户未读通知
     * @param receiverId 接收者ID
     * @param receiverType 接收者类型
     * @return 未读通知列表
     */
    List<NotificationReceiver> getUnreadNotifications(@Param("receiverId") Integer receiverId, 
                                                    @Param("receiverType") String receiverType);
    
    /**
     * 获取用户未读通知（不带接收者类型）
     * @param receiverId 接收者ID
     * @return 未读通知列表
     */
    List<NotificationReceiver> getUnreadNotificationsWithoutType(Integer receiverId);
    
    /**
     * 标记通知为已读
     * @param id 通知接收记录ID
     * @return 影响行数
     */
    int markAsRead(Integer id);
    
    /**
     * 批量标记通知为已读
     * @param receiverId 接收者ID
     * @param receiverType 接收者类型
     * @return 影响行数
     */
    int markAllAsRead(@Param("receiverId") Integer receiverId, 
                     @Param("receiverType") String receiverType);
    
    /**
     * 根据通知ID获取接收记录
     * @param notificationId 通知ID
     * @return 接收记录列表
     */
    List<NotificationReceiver> getByNotificationId(Integer notificationId);
    
    /**
     * 删除通知接收记录
     * @param id 接收记录ID
     * @return 影响行数
     */
    int deleteNotificationReceiver(Integer id);
}