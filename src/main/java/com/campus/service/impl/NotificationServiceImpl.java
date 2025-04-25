package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.NotificationDao;
import com.campus.dao.UserDao;
import com.campus.entity.Notification;
import com.campus.entity.User;
import com.campus.entity.NotificationUser;
import com.campus.enums.UserType;
import com.campus.exception.CustomException;
import com.campus.exception.ResourceNotFoundException;
import com.campus.service.NotificationService;
import com.campus.service.UserService;
import com.campus.dao.NotificationUserDao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知公告服务实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationDao, Notification> implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private NotificationUserDao notificationUserDao;

    @Override
    public Notification getNotificationWithAttachments(Long id) {
        Notification notification = getById(id);
        if (notification == null) {
            return null;
        }

        try {
            this.incrementViewCount(id);
        } catch (Exception e) {
            log.error("尝试增加通知浏览次数失败 (non-blocking), id: {}", id, e);
        }

        if (StringUtils.hasText(notification.getAttachmentsJson())) {
            try {
                TypeReference<List<Map<String, String>>> typeRef = new TypeReference<>() {
                };
                List<Map<String, String>> attachments = objectMapper.readValue(notification.getAttachmentsJson(), typeRef);
                notification.setAttachmentFiles(attachments);
            } catch (IOException e) {
                log.error("解析通知附件JSON失败, id: {}", id, e);
                notification.setAttachmentFiles(Collections.emptyList());
            }
        }
        return notification;
    }

    @Override
    public List<Notification> getAllNotifications() {
        return list(Wrappers.<Notification>lambdaQuery().orderByDesc(Notification::getCreateTime));
    }

    @Override
    public List<Notification> getNotificationsByType(String type) {
        if (!StringUtils.hasText(type)) {
            return Collections.emptyList();
        }
        return list(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getType, type)
                .orderByDesc(Notification::getCreateTime));
    }

    @Override
    public List<Notification> getNotificationsByStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return Collections.emptyList();
        }
        return list(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getStatus, status)
                .orderByDesc(Notification::getCreateTime));
    }

    @Override
    public List<Notification> getRecentNotifications(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5;
        }
        Page<Notification> page = new Page<>(1, limit);
        IPage<Notification> resultPage = page(page, Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getStatus, "1")
                .orderByDesc(Notification::getIsTop, Notification::getCreateTime));
        return resultPage.getRecords();
    }

    @Override
    public List<Notification> getTopNotifications() {
        return list(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getStatus, "1")
                .eq(Notification::getIsTop, 1)
                .orderByDesc(Notification::getCreateTime));
    }

    @Override
    public List<Notification> getNotificationsByPublisherId(Long publisherId) {
        if (publisherId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getPublisherId, publisherId)
                .orderByDesc(Notification::getCreateTime));
    }

    private void prepareNotificationForSave(Notification notification, List<Long> userIds, String targetType) {
        if (notification == null) throw new IllegalArgumentException("通知对象不能为空");

        notification.setTargetType(targetType);
        notification.setCreateTime(notification.getCreateTime() == null ? new Date() : notification.getCreateTime());
        notification.setUpdateTime(new Date());
        notification.setStatus(notification.getStatus() == null ? "1" : notification.getStatus());
        notification.setIsTop(notification.getIsTop() == null ? 0 : notification.getIsTop());
        notification.setViewCount(notification.getViewCount() == null ? 0 : notification.getViewCount());

        if (!CollectionUtils.isEmpty(userIds)) {
            List<Map<String, Object>> receivers = userIds.stream()
                    .distinct()
                    .map(userId -> {
                        Map<String, Object> receiverMap = new HashMap<>();
                        receiverMap.put("receiverId", userId);
                        receiverMap.put("isRead", 0);
                        receiverMap.put("readTime", null);
                        return receiverMap;
                    })
                    .collect(Collectors.toList());
            try {
                notification.setReceiversJson(objectMapper.writeValueAsString(receivers));
            } catch (IOException e) {
                log.error("序列化通知接收者JSON失败 for targetType: {}", targetType, e);
                throw new CustomException("发送通知失败 (接收者JSON序列化错误)");
            }
        } else {
            notification.setReceiversJson("[]");
        }

        if (notification.getAttachmentFiles() != null && !notification.getAttachmentFiles().isEmpty()) {
            try {
                notification.setAttachmentsJson(objectMapper.writeValueAsString(notification.getAttachmentFiles()));
            } catch (IOException e) {
                log.error("序列化通知附件JSON失败 for targetType: {}", targetType, e);
                notification.setAttachmentsJson("[]");
            }
        } else if (!StringUtils.hasText(notification.getAttachmentsJson())) {
            notification.setAttachmentsJson("[]");
        }
    }

    @Override
    @Transactional
    public void sendNotificationToUsers(Notification notification, List<Long> userIds) {
        if (notification == null || userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("通知和用户ID列表不能为空");
        }

        // 1. Save the notification itself (ensure it has an ID)
        notification.setStatus("PUBLISHED"); // Set status to published
        notification.setSendTime(new Date()); // Set send time
        notification.setTargetType("USERS");
        // Convert userIds list to JSON string for target_ids field
        try {
            notification.setTargetIds(objectMapper.writeValueAsString(userIds));
        } catch (IOException e) {
            log.error("Failed to serialize targetIds for notification: {}", e.getMessage());
            throw new CustomException("序列化目标用户ID失败");
        }

        // Save or Update the main notification record
        this.saveOrUpdate(notification);
        Long notificationId = notification.getId();
        if (notificationId == null) {
            throw new CustomException("保存通知记录失败，无法获取通知ID");
        }

        // 2. Create records in the notification_user junction table
        List<NotificationUser> relations = userIds.stream()
                .map(userId -> new NotificationUser(notificationId, userId, false, null))
                .collect(Collectors.toList());

        if (!relations.isEmpty()) {
            // Use the injected NotificationUserDao for batch insert
            int insertedCount = notificationUserDao.batchInsert(relations);
            if (insertedCount != relations.size()) {
                log.warn("发送通知给用户：预期插入 {} 条关联记录，实际插入 {} 条", relations.size(), insertedCount);
                // Decide on error handling: throw exception or just log?
                // throw new CustomException("部分通知未能成功发送给指定用户");
            }
        }
    }

    @Override
    @Transactional
    public boolean sendNotificationByUserType(Notification notification, String userType) {
        UserType typeEnum;
        try {
            typeEnum = UserType.valueOf(userType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("无效的用户类型: {}", userType);
            throw new CustomException("无效的用户类型: " + userType);
        }

        List<Object> userIdObjects = userDao.selectObjs(Wrappers.<User>lambdaQuery()
                .eq(User::getUserType, typeEnum)
                .select(User::getId));

        List<Long> userIds = userIdObjects.stream()
                .filter(Objects::nonNull)
                .map(id -> ((Number) id).longValue())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userIds)) {
            log.warn("尝试按类型 '{}' 发送通知，但未找到该类型的用户。", userType);
            return false;
        }

        prepareNotificationForSave(notification, userIds, userType);

        if (notification.getId() != null) {
            log.warn("sendNotificationByUserType 被调用，但传入的通知对象已包含ID，将尝试更新而非创建。 ID: {}", notification.getId());
            return this.updateById(notification);
        }
        return this.save(notification);
    }

    @Override
    @Transactional
    public boolean sendNotificationToAll(Notification notification) {
        List<Object> allUserIdObjects = userDao.selectObjs(Wrappers.<User>lambdaQuery().select(User::getId));

        List<Long> allUserIds = allUserIdObjects.stream()
                .filter(Objects::nonNull)
                .map(id -> ((Number) id).longValue())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(allUserIds)) {
            log.warn("尝试发送全体通知，但系统中没有用户。");
            return false;
        }

        prepareNotificationForSave(notification, allUserIds, "全体");

        if (notification.getId() != null) {
            log.warn("sendNotificationToAll 被调用，但传入的通知对象已包含ID，将尝试更新而非创建。 ID: {}", notification.getId());
            return this.updateById(notification);
        }
        return this.save(notification);
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long id) {
        if (id == null) return false;
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteNotifications(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return removeByIds(ids);
    }

    @Override
    @Transactional
    public boolean updateNotificationStatus(Long id, String status) {
        if (id == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("通知ID和状态不能为空");
        }
        return update(Wrappers.<Notification>lambdaUpdate()
                .eq(Notification::getId, id)
                .set(Notification::getStatus, status)
                .set(Notification::getUpdateTime, new Date()));
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        if (id == null) return false;
        return update(Wrappers.<Notification>lambdaUpdate()
                .eq(Notification::getId, id)
                .setSql("view_count = view_count + 1"));
    }

    @Override
    public IPage<Notification> getNotificationsForUser(Long userId, int pageNo, int pageSize, String status) {
        Page<NotificationUser> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<NotificationUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotificationUser::getUserId, userId);

        if ("unread".equalsIgnoreCase(status)) {
            queryWrapper.eq(NotificationUser::isRead, false);
        } else if ("read".equalsIgnoreCase(status)) {
            queryWrapper.eq(NotificationUser::isRead, true);
        }
        // "all" status doesn't need a filter

        queryWrapper.orderByDesc(NotificationUser::getCreateTime); // Order by relation creation time?

        IPage<NotificationUser> userPage = notificationUserDao.selectPage(page, queryWrapper);

        // Extract notification IDs
        // List<Long> notificationIds = userPage.getRecords().stream()
        //         .map(NotificationUser::getNotificationId)
        //         .collect(Collectors.toList()); // Unused

        // Fetch Notification details (assuming NotificationDao exists or baseMapper works)
        List<Notification> notifications = userPage.getRecords().stream()
                .map(nu -> notificationDao.selectById(nu.getNotificationId())) // Fetch details
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Create a new IPage with Notification objects
        IPage<Notification> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        resultPage.setRecords(notifications);

        return resultPage;
    }

    @Override
    @Transactional
    public boolean markNotificationAsRead(Long notificationId, Long userId) {
        // Check if the relation exists
        NotificationUser relation = notificationUserDao.selectOne(Wrappers.<NotificationUser>lambdaQuery()
                .eq(NotificationUser::getNotificationId, notificationId)
                .eq(NotificationUser::getUserId, userId));

        if (relation == null) {
            // Relation doesn't exist, maybe the user wasn't a target?
            // Or maybe it's a general notification they haven't interacted with?
            // Option 1: Create the relation row now as read
            // Option 2: Throw error / return false
            log.warn("尝试标记未找到的通知关联记录为已读: notificationId={}, userId={}", notificationId, userId);
            // Let's create it now if the notification exists
            if (this.getById(notificationId) != null) {
                NotificationUser newRelation = new NotificationUser(notificationId, userId, true, new Date());
                return notificationUserDao.insert(newRelation) > 0;
            } else {
                throw new ResourceNotFoundException("通知 (ID: " + notificationId + ") 不存在");
            }
            // return false;
        }

        // If already read, return true (or false if you want to indicate no change)
        if (relation.getIsRead()) {
            return true; // Already marked as read
        }

        // Update the existing relation
        relation.setIsRead(true);
        relation.setReadTime(new Date());
        return notificationUserDao.updateById(relation) > 0;
    }

    @Override
    public IPage<Notification> getNotificationPage(int pageNo, int pageSize, Notification notification) {
        Page<Notification> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Notification> queryWrapper = buildQueryWrapper(notification);
        queryWrapper.orderByDesc(Notification::getCreateTime);
        return page(page, queryWrapper);
    }

    @Override
    public IPage<Notification> getNotificationPage(int pageNo, int pageSize, String type, String keyword, String status) {
        Page<Notification> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.<Notification>lambdaQuery();

        queryWrapper.eq(StringUtils.hasText(type), Notification::getType, type);
        queryWrapper.eq(StringUtils.hasText(status), Notification::getStatus, status);
        queryWrapper.like(StringUtils.hasText(keyword), Notification::getTitle, keyword);

        queryWrapper.orderByDesc(Notification::getIsTop, Notification::getCreateTime);

        return page(page, queryWrapper);
    }

    private LambdaQueryWrapper<Notification> buildQueryWrapper(Notification notification) {
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.<Notification>lambdaQuery();
        if (notification != null) {
            queryWrapper.eq(notification.getId() != null, Notification::getId, notification.getId());
            queryWrapper.like(StringUtils.hasText(notification.getTitle()), Notification::getTitle, notification.getTitle());
            queryWrapper.eq(StringUtils.hasText(notification.getType()), Notification::getType, notification.getType());
            queryWrapper.eq(notification.getPriority() != null, Notification::getPriority, notification.getPriority());
            queryWrapper.eq(StringUtils.hasText(notification.getStatus()), Notification::getStatus, notification.getStatus());
            queryWrapper.eq(notification.getIsTop() != null, Notification::getIsTop, notification.getIsTop());
            queryWrapper.eq(notification.getSenderId() != null, Notification::getSenderId, notification.getSenderId());
            queryWrapper.eq(notification.getPublisherId() != null, Notification::getPublisherId, notification.getPublisherId());
            queryWrapper.eq(StringUtils.hasText(notification.getTargetType()), Notification::getTargetType, notification.getTargetType());
        }
        return queryWrapper;
    }

    @Override
    public List<Map<String, Object>> getNoticeTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("value", "1", "label", "系统通知"));
        types.add(Map.of("value", "2", "label", "教学通知"));
        types.add(Map.of("value", "3", "label", "活动公告"));
        types.add(Map.of("value", "4", "label", "其他"));
        return types;
    }
}