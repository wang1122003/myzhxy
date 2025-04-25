package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.NotificationDao;
import com.campus.dao.NotificationReceiverDao;
import com.campus.dao.UserDao;
import com.campus.entity.Notification;
import com.campus.entity.NotificationReceiver;
import com.campus.entity.User;
import com.campus.enums.UserType;
import com.campus.exception.CustomException;
import com.campus.service.NotificationService;
import com.campus.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知公告服务实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationDao, Notification> implements NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private UserDao userDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Notification getNotificationWithAttachments(Long id) {
        Notification notification = notificationDao.selectById(id);

        // TODO: [通知附件] Check if Notification entity actually has get/setAttachmentsJson and setAttachmentFiles methods
        /*
        if (notification != null && org.springframework.util.StringUtils.hasText(notification.getAttachmentsJson())) {
            try {
                List<Map<String, String>> attachments = objectMapper.readValue(
                    notification.getAttachmentsJson(),
                    new TypeReference<List<Map<String, String>>>() {}
                );
                notification.setAttachmentFiles(attachments);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        */
        return notification;
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationDao.getAllNotifications();
    }

    @Override
    public List<Notification> getNotificationsByType(String type) {
        return notificationDao.findByType(type);
    }

    @Override
    public List<Notification> getNotificationsByStatus(String status) {
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
        // TODO: [通知时间戳] Check if Notification entity actually has setCreateTime/setUpdateTime methods
        // notification.setCreateTime(new Date());
        // notification.setUpdateTime(new Date());
        int inserted = notificationDao.insert(notification);
        return inserted > 0;
    }

    @Override
    @Transactional
    public boolean addNotification(Notification notification, List<Long> attachmentIds) {
        // TODO: [通知时间戳] Check if Notification entity actually has getCreateTime/setUpdateTime methods
        // if (notification.getCreateTime() == null) {
        //     notification.setCreateTime(new Date());
        // }
        // notification.setUpdateTime(new Date());

        // TODO: [通知发送者/发布者] Check if Notification entity actually has get/set PublisherId/SenderId methods
        // if (notification.getPublisherId() == null && notification.getSenderId() != null) {
        //     notification.setPublisherId(notification.getSenderId());
        // } else if (notification.getSenderId() == null && notification.getPublisherId() != null) {
        //     notification.setSenderId(notification.getPublisherId());
        // }
        
        boolean success = save(notification);
        // TODO: Handle attachment association if needed
        return success;
    }

    @Override
    @Transactional
    public boolean updateNotification(Notification notification) {
        Notification existingNotification = notificationDao.selectById(notification.getId());
        if (existingNotification == null) {
            throw new RuntimeException("通知不存在");
        }
        // TODO: [通知时间戳] Check if Notification entity actually has setUpdateTime
        // notification.setUpdateTime(new Date());
        int updated = notificationDao.updateById(notification);
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean updateNotification(Notification notification, List<Long> attachmentIds) {
        // TODO: [通知时间戳] Check if Notification entity actually has setUpdateTime
        // notification.setUpdateTime(new Date());

        // TODO: [通知发送者/发布者] Check if Notification entity actually has get/set PublisherId/SenderId methods
        // if (notification.getPublisherId() == null && notification.getSenderId() != null) {
        //     notification.setPublisherId(notification.getSenderId());
        // } else if (notification.getSenderId() == null && notification.getPublisherId() != null) {
        //     notification.setSenderId(notification.getPublisherId());
        // }
        
        boolean success = updateById(notification);
        // TODO: Handle attachment updates if needed
        return success;
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long id) {
        boolean deleted = removeById(id);
        return deleted;
    }

    @Override
    @Transactional
    public boolean batchDeleteNotifications(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        boolean deleted = removeByIds(ids);
        return deleted;
    }

    @Override
    public boolean updateNotificationStatus(Long id, String status) {
        Notification notification = new Notification();
        // TODO: [通知ID/状态/时间戳] Check if Notification entity actually has setId/setStatus/setUpdateTime methods
        // notification.setId(id);
        // notification.setStatus(status);
        // notification.setUpdateTime(new Date());
        // For now, assume we cannot update selectively via entity setters
        // Need custom DAO method or direct UpdateWrapper
        // return updateById(notification); 
        LambdaUpdateWrapper<Notification> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Notification::getId, id).set(Notification::getStatus, status);
        return update(updateWrapper);
    }

    @Override
    public boolean incrementViewCount(Long id) {
        return notificationDao.incrementViewCount(id) > 0;
    }

    @Override
    public IPage<Notification> getNotificationPage(int pageNo, int pageSize, Notification notification) {
        Page<Notification> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Notification> wrapper = buildQueryWrapper(notification);
        // TODO: [通知置顶/时间戳] Check if Notification entity has getIsTop/getCreateTime methods for sorting
        // wrapper.orderByDesc(Notification::getIsTop, Notification::getCreateTime);
        return notificationDao.selectPage(page, wrapper);
    }

    @Override
    public IPage<Notification> getNotificationPage(int pageNo, int pageSize, String type, String keyword, String status) {
        Page<Notification> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();

        // TODO: [通知类型/标题/内容/状态] Check if Notification entity has getType/getTitle/getContent/getStatus methods
        /*
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq(Notification::getType, type);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Notification::getTitle, keyword)
                   .or(w -> w.like(Notification::getContent, keyword));
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(Notification::getStatus, status);
        }
        */
        // TODO: [通知置顶/时间戳] Check sorting fields exist
        // wrapper.orderByDesc(Notification::getIsTop, Notification::getCreateTime);

        return notificationDao.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean sendNotificationToUsers(Notification notification, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }

        saveNotificationIfNotExists(notification);

        try {
            List<Map<String, Object>> receivers = new ArrayList<>();
            // TODO: [通知接收者] Check if Notification entity has get/setReceiversJson methods
            /*
            if (org.springframework.util.StringUtils.hasText(notification.getReceiversJson())) {
                receivers = objectMapper.readValue(
                    notification.getReceiversJson(),
                    new TypeReference<List<Map<String, Object>>>() {}
                );
            }
            */

            for (Long userId : userIds) {
                boolean exists = receivers.stream()
                        .anyMatch(r -> userId.equals(Long.valueOf(r.get("receiverId").toString())));
                if (!exists) {
                    // TODO: [通知ID] Check if Notification entity has getId method
                    // Map<String, Object> receiver = createReceiverMap(notification.getId(), userId);
                    // receivers.add(receiver);
                }
            }

            // TODO: [通知接收者] Check setReceiversJson exists
            // notification.setReceiversJson(objectMapper.writeValueAsString(receivers));

            return updateById(notification);
        } catch (Exception e) { // Catch broader Exception for now
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean sendNotificationByUserType(Notification notification, String userType) {
        saveNotificationIfNotExists(notification);
        if (StringUtils.isBlank(userType)) {
            return false;
        }

        // 查询该类型的用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserType, userType);
        List<User> users = userDao.selectList(queryWrapper);
        
        if (users == null || users.isEmpty()) {
            return true; // 无需发送通知，没有符合条件的用户
        }

        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
            
        return sendNotificationToUsers(notification, userIds);
    }

    @Override
    @Transactional
    public boolean sendNotificationToAll(Notification notification) {
        // 获取所有用户ID
        List<User> users = userDao.selectList(null);
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        return sendNotificationToUsers(notification, userIds);
    }

    /**
     * 创建接收者信息Map
     */
    private Map<String, Object> createReceiverMap(Long notificationId, Long userId) {
        Map<String, Object> receiver = new HashMap<>();
        receiver.put("receiverId", userId);
        receiver.put("notificationId", notificationId);
        receiver.put("isRead", 0);
        receiver.put("readTime", null);
        receiver.put("createTime", new Date());
        return receiver;
    }

    /**
     * 保存通知（如果不存在）
     */
    private void saveNotificationIfNotExists(Notification notification) {
        // TODO: [通知ID/时间戳] Check getId, setCreateTime, setUpdateTime exist
        /*
        if (notification.getId() == null) {
             notification.setCreateTime(new Date());
             notification.setUpdateTime(new Date());
             save(notification);
        }
        */
    }

    /**
     * 构建查询Wrapper
     */
    private LambdaQueryWrapper<Notification> buildQueryWrapper(Notification notification) {
        LambdaQueryWrapper<Notification> wrapper = Wrappers.lambdaQuery();
        // TODO: [通知实体字段] Check all referenced fields (getTitle, getType, getStatus, etc.) exist
        /*
        if (notification != null) {
            if (StringUtils.isNotBlank(notification.getTitle())) {
                wrapper.like(Notification::getTitle, notification.getTitle());
            }
            if (StringUtils.isNotBlank(notification.getType())) {
                wrapper.eq(Notification::getType, notification.getType());
            }
            if (notification.getStatus() != null) { // Assuming status is Integer or Enum
                wrapper.eq(Notification::getStatus, notification.getStatus());
            }
            // Add other fields as needed
        }
        */
        return wrapper;
    }
}