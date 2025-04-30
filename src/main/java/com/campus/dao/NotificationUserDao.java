package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Notification;
import com.campus.entity.NotificationUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NotificationUser 数据访问层
 */
@Repository
public interface NotificationUserDao extends BaseMapper<NotificationUser> {

    /**
     * 批量插入用户通知关系
     *
     * @param notificationUserList 关系列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<NotificationUser> notificationUserList);

    /**
     * 根据用户ID和通知ID查找记录
     *
     * @param userId         用户ID
     * @param notificationId 通知ID
     * @return 记录
     */
    @Select("SELECT * FROM notification_user WHERE user_id = #{userId} AND notification_id = #{notificationId}")
    NotificationUser findByUserIdAndNotificationId(@Param("userId") Long userId, @Param("notificationId") Long notificationId);

    /**
     * 获取用户的通知列表（支持已读/未读过滤）
     *
     * @param userId     用户ID
     * @param readStatus 0 未读, 1 已读 (null 表示所有)
     * @return 通知列表
     */
    List<Notification> findNotificationsByUserId(@Param("userId") Long userId, @Param("readStatus") Integer readStatus);

    /**
     * 批量插入用户通知关联记录
     *
     * @param notificationUserList 用户通知关联列表
     * @return 影响的行数
     */
    int batchInsert(@Param("list") List<NotificationUser> notificationUserList);
} 