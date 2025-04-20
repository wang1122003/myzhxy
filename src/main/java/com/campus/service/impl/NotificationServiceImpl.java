package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.NotificationAttachmentDao;
import com.campus.dao.NotificationDao;
import com.campus.dao.NotificationReceiverDao;
import com.campus.dao.UserDao;
import com.campus.entity.Notification;
import com.campus.entity.NotificationReceiver;
import com.campus.exception.CustomException;
import com.campus.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告服务实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationDao, Notification> implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private NotificationReceiverDao notificationReceiverDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private NotificationAttachmentDao notificationAttachmentDao;

    @Override
    public Notification getNotificationWithAttachments(Long id) {
        // 这里可以根据需要实现查询通知详情，并关联查询附件信息
        // 暂未实现附件功能，直接返回基础信息
        return notificationDao.selectById(id);
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationDao.getAllNotifications(); // 使用与Mapper XML中ID匹配的方法 findAllNotifications -> getAllNotifications
    }

    @Override
    public List<Notification> getNotificationsByType(Integer noticeType) {
        return notificationDao.findByType(noticeType);
    }

    @Override
    public List<Notification> getNotificationsByStatus(Integer status) {
        return notificationDao.findByStatus(status);
    }

    @Override
    public List<Notification> getRecentNotifications(Integer limit) {
        return notificationDao.findRecent(limit);
    }

    @Override
    public List<Notification> getTopNotifications() {
        return notificationDao.findTop();
    }

    @Override
    public List<Notification> getNotificationsByPublisherId(Long publisherId) {
        return notificationDao.findByPublisherId(publisherId);
    }

    @Override
    @Transactional
    public boolean addNotification(Notification notification) {
        log.info("新增通知: {}", notification);
        notification.setCreateTime(new Date());
        notification.setUpdateTime(new Date());

        int inserted = notificationDao.insert(notification);
        return inserted > 0;
    }

    @Override
    @Transactional
    public boolean addNotification(Notification notification, List<Long> attachmentIds) {
        if (notification.getCreateTime() == null) {
            notification.setCreateTime(new Date());
        }
        notification.setUpdateTime(new Date());
        // 确保publisherId和senderId同步
        if (notification.getPublisherId() == null && notification.getSenderId() != null) {
            notification.setPublisherId(notification.getSenderId());
        } else if (notification.getSenderId() == null && notification.getPublisherId() != null) {
            notification.setSenderId(notification.getPublisherId());
        }
        boolean success = save(notification);
        return success;
    }

    @Override
    @Transactional
    public boolean updateNotification(Notification notification) {
        log.info("更新通知: {}", notification);
        Notification existingNotification = notificationDao.selectById(notification.getId());
        if (existingNotification == null) {
            throw new CustomException("通知不存在");
        }
        notification.setUpdateTime(new Date());

        int updated = notificationDao.updateById(notification);
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean updateNotification(Notification notification, List<Long> attachmentIds) {
        notification.setUpdateTime(new Date());
        // 确保publisherId和senderId同步
        if (notification.getPublisherId() == null && notification.getSenderId() != null) {
            notification.setPublisherId(notification.getSenderId());
        } else if (notification.getSenderId() == null && notification.getPublisherId() != null) {
            notification.setSenderId(notification.getPublisherId());
        }
        boolean success = updateById(notification);
        return success;
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long id) {
        log.info("删除通知 ID: {}", id);
        // 1. 删除通知本身
        boolean deleted = removeById(id);
        return deleted;
    }

    @Override
    @Transactional
    public boolean batchDeleteNotifications(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        // 1. 批量删除通知
        boolean deleted = removeByIds(ids);
        return deleted;
    }

    @Override
    public boolean updateNotificationStatus(Long id, Integer status) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setStatus(status);
        notification.setUpdateTime(new Date());
        return updateById(notification);
    }

    @Override
    public boolean incrementViewCount(Long id) {
        return notificationDao.incrementViewCount(id) > 0;
    }

    @Override
    public IPage<Notification> getNotificationPage(int pageNo, int pageSize, Notification notification) {
        Page<Notification> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Notification> wrapper = buildQueryWrapper(notification);
        wrapper.orderByDesc(Notification::getIsTop, Notification::getCreateTime);
        return notificationDao.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean sendNotificationToUsers(Notification notification, List<Long> userIds) {
        saveNotificationIfNotExists(notification);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        List<NotificationReceiver> receivers = userIds.stream()
                .map(userId -> createReceiver(notification.getId(), userId))
                .collect(Collectors.toList());
        return notificationReceiverDao.insertBatch(receivers) > 0;
    }

    @Override
    @Transactional
    public boolean sendNotificationToRoles(Notification notification, List<Long> roleIds) {
        saveNotificationIfNotExists(notification);
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        List<Long> userIds = userDao.findUserIdsByRoleIds(roleIds);
        return sendNotificationToUsers(notification, userIds);
    }

    @Override
    @Transactional
    public boolean sendNotificationToDepartments(Notification notification, List<Long> departmentIds) {
        saveNotificationIfNotExists(notification);
        if (departmentIds == null || departmentIds.isEmpty()) {
            return true;
        }
        // 实现根据部门查找用户ID
        List<Long> userIds = userDao.findUserIdsByDepartmentIds(departmentIds);
        return sendNotificationToUsers(notification, userIds);
    }

    @Override
    @Transactional
    public boolean sendNotificationToAll(Notification notification) {
        saveNotificationIfNotExists(notification);
        // 实现查询所有用户ID
        List<Long> allUserIds = userDao.findAllUserIds();
        return sendNotificationToUsers(notification, allUserIds);
    }

    @Override
    public int getUnreadNotificationCount(Long userId) {
        return notificationReceiverDao.count(new LambdaQueryWrapper<NotificationReceiver>()
                .eq(NotificationReceiver::getReceiverId, userId)
                .eq(NotificationReceiver::getIsRead, 0)
                .eq(NotificationReceiver::getStatus, 1));
    }

    @Override
    public IPage<Notification> getUnreadNotifications(int pageNo, int pageSize, Long userId) {
        Page<Notification> page = new Page<>(pageNo, pageSize);
        return notificationDao.findUnreadNotifications(page, userId);
    }

    @Override
    public IPage<Notification> getReadNotifications(int pageNo, int pageSize, Long userId) {
        Page<Notification> page = new Page<>(pageNo, pageSize);
        return notificationDao.findReadNotifications(page, userId);
    }

    @Override
    public boolean markNotificationAsRead(Long userId, Long notificationId) {
        LambdaQueryWrapper<NotificationReceiver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationReceiver::getReceiverId, userId)
                .eq(NotificationReceiver::getNotificationId, notificationId)
                .eq(NotificationReceiver::getIsRead, 0);
        return updateReceiverStatus(wrapper, 1);
    }

    @Override
    public boolean markAllNotificationsAsRead(Long userId) {
        LambdaQueryWrapper<NotificationReceiver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationReceiver::getReceiverId, userId)
                .eq(NotificationReceiver::getIsRead, 0);
        return updateReceiverStatus(wrapper, 1);
    }

    private NotificationReceiver createReceiver(Long notificationId, Long userId) {
        NotificationReceiver receiver = new NotificationReceiver();
        receiver.setNotificationId(notificationId);
        receiver.setReceiverId(userId);
        receiver.setIsRead(0);
        receiver.setStatus(1);
        receiver.setCreateTime(new Date());
        receiver.setUpdateTime(new Date());
        return receiver;
    }

    private void saveNotificationIfNotExists(Notification notification) {
        if (notification.getId() == null) {
            addNotification(notification);
        }
    }

    private boolean updateReceiverStatus(LambdaQueryWrapper<NotificationReceiver> wrapper, Integer isRead) {
        NotificationReceiver receiverUpdate = new NotificationReceiver();
        receiverUpdate.setIsRead(isRead);
        receiverUpdate.setReadTime(new Date());
        receiverUpdate.setUpdateTime(new Date());
        return notificationReceiverDao.update(receiverUpdate, wrapper) > 0;
    }

    private LambdaQueryWrapper<Notification> buildQueryWrapper(Notification notification) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        if (notification != null) {
            if (StringUtils.isNotBlank(notification.getTitle())) {
                wrapper.like(Notification::getTitle, notification.getTitle());
            }
            if (notification.getNoticeType() != null) {
                wrapper.eq(Notification::getNoticeType, notification.getNoticeType());
            }
            if (notification.getStatus() != null) {
                wrapper.eq(Notification::getStatus, notification.getStatus());
            }
            if (StringUtils.isNotBlank(notification.getPublisherName())) {
                wrapper.like(Notification::getPublisherName, notification.getPublisherName());
            }
            if (notification.getTargetType() != null) {
                wrapper.eq(Notification::getTargetType, notification.getTargetType());
            }
            if (notification.getTargetId() != null) {
                wrapper.eq(Notification::getTargetId, notification.getTargetId());
            }
            // 可以根据需要添加更多查询条件，比如时间范围等
        }
        return wrapper;
    }
} 