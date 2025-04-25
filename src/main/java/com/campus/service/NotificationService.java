package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Notification;

import java.util.List;

/**
 * 通知公告服务接口
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 根据ID获取通知详情，包含附件信息
     * @param id 通知ID
     * @return 通知对象
     */
    Notification getNotificationWithAttachments(Long id);
    
    /**
     * 获取所有通知列表
     * @return 通知列表
     */
    List<Notification> getAllNotifications();
    
    /**
     * 根据通知类型获取通知列表
     * @param type 通知类型 (String)
     * @return 通知列表
     */
    List<Notification> getNotificationsByType(String type);

    /**
     * 根据状态获取通知列表
     *
     * @param status 状态 (String)
     * @return 通知列表
     */
    List<Notification> getNotificationsByStatus(String status);

    /**
     * 获取最近N条通知
     *
     * @param limit 数量
     * @return 通知列表
     */
    List<Notification> getRecentNotifications(Integer limit);

    /**
     * 获取置顶通知列表
     *
     * @return 通知列表
     */
    List<Notification> getTopNotifications();

    /**
     * 根据发布者ID获取通知列表
     * @param publisherId 发布者ID
     * @return 通知列表
     */
    List<Notification> getNotificationsByPublisherId(Long publisherId);

    /**
     * 添加通知（包含附件处理）
     *
     * @param notification  通知对象
     * @param attachmentIds 附件ID列表
     * @return 是否成功
     */
    boolean addNotification(Notification notification, List<Long> attachmentIds);

    /**
     * 添加通知（不含附件处理）
     *
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean addNotification(Notification notification);
    
    /**
     * 更新通知（包含附件处理）
     * @param notification 通知对象
     * @param attachmentIds 附件ID列表
     * @return 是否成功
     */
    boolean updateNotification(Notification notification, List<Long> attachmentIds);

    /**
     * 更新通知（不含附件处理）
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
     * 批量删除通知
     *
     * @param ids 通知ID列表
     * @return 是否成功
     */
    boolean batchDeleteNotifications(List<Long> ids);

    /**
     * 更新通知状态
     * @param id 通知ID
     * @param status 新状态 (String)
     * @return 是否成功
     */
    boolean updateNotificationStatus(Long id, String status);

    /**
     * 增加阅读次数
     * @param id 通知ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);

    /**
     * 分页查询通知
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param notification 通知条件
     * @return 分页通知
     */
    IPage<Notification> getNotificationPage(int pageNo, int pageSize, Notification notification);

    /**
     * 分页查询通知（支持关键词和类型过滤）
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @param type     通知类型 (String)
     * @param keyword  标题关键词
     * @param status   通知状态 (String)
     * @return 分页通知
     */
    IPage<Notification> getNotificationPage(int pageNo, int pageSize, String type, String keyword, String status);

    /**
     * 发送通知给指定用户
     *
     * @param notification 通知对象
     * @param userIds      用户ID列表
     * @return 是否成功
     */
    boolean sendNotificationToUsers(Notification notification, List<Long> userIds);

    /**
     * 发送通知给指定用户类型
     *
     * @param notification 通知对象
     * @param userType     用户类型 (Admin, Teacher, Student)
     * @return 是否成功
     */
    boolean sendNotificationByUserType(Notification notification, String userType);

    /**
     * 发送通知给全体用户
     *
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean sendNotificationToAll(Notification notification);
} 