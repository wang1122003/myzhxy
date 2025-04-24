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
     * @param noticeType 通知类型
     * @return 通知列表
     */
    List<Notification> getNotificationsByType(Integer noticeType);

    /**
     * 根据状态获取通知列表
     *
     * @param status 状态
     * @return 通知列表
     */
    List<Notification> getNotificationsByStatus(Integer status);

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
     * @param status 新状态 (String, e.g., "Active", "Recalled")
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
     * 分页获取通知列表
     *
     * @param pageNo       页码
     * @param pageSize     每页数量
     * @param notification 查询条件
     * @return 分页结果
     */
    IPage<Notification> getNotificationPage(int pageNo, int pageSize, Notification notification);

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
     * 发送通知给指定部门
     * @param notification 通知对象
     * @param departmentIds 部门ID列表
     * @return 是否成功
     */
    boolean sendNotificationToDepartments(Notification notification, List<Long> departmentIds);

    /**
     * 发送通知给全体用户
     *
     * @param notification 通知对象
     * @return 是否成功
     */
    boolean sendNotificationToAll(Notification notification);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadNotificationCount(Long userId);

    /**
     * 获取用户未读通知列表（分页）
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @param userId   用户ID
     * @return 分页结果
     */
    IPage<Notification> getUnreadNotifications(int pageNo, int pageSize, Long userId);

    /**
     * 获取用户已读通知列表（分页）
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @param userId   用户ID
     * @return 分页结果
     */
    IPage<Notification> getReadNotifications(int pageNo, int pageSize, Long userId);

    /**
     * 标记通知为已读
     * @param userId 用户ID
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean markNotificationAsRead(Long userId, Long notificationId);

    /**
     * 标记用户所有通知为已读
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllNotificationsAsRead(Long userId);
} 