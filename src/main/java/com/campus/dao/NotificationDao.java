package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.Notification;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知公告数据访问接口
 */
@Repository
public interface NotificationDao extends BaseMapper<Notification> {
    
    /**
     * 根据ID查询通知详情（可能包含附件等关联信息）
     * @param id 通知ID
     * @return 通知对象
     */
    Notification findByIdWithDetails(@Param("id") Long id);

    /**
     * 查询所有通知列表（可带排序）
     * @return 通知列表
     */
    List<Notification> getAllNotifications();
    
    /**
     * 根据类型查询通知列表
     * @param noticeType 通知类型
     * @return 通知列表
     */
    List<Notification> findByType(@Param("noticeType") Integer noticeType);
    
    /**
     * 根据状态查询通知列表
     * @param status 状态
     * @return 通知列表
     */
    List<Notification> findByStatus(@Param("status") Integer status);
    
    /**
     * 查询最新的N条通知
     * @param limit 数量限制
     * @return 通知列表
     */
    List<Notification> findRecent(@Param("limit") Integer limit);
    
    /**
     * 查询置顶通知
     * @return 通知列表
     */
    List<Notification> findTop();
    
    /**
     * 根据发布者ID查询通知列表
     * @param publisherId 发布者ID
     * @return 通知列表
     */
    List<Notification> findByPublisherId(@Param("publisherId") Long publisherId);

    /**
     * 批量更新通知状态
     *
     * @param ids    通知ID列表
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 增加阅读次数
     *
     * @param id 通知ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 分页查询通知列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件包装器
     * @return 分页结果
     */
    IPage<Notification> findPage(Page<Notification> page, @Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<Notification> queryWrapper);

    /**
     * 查询用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int countUnread(@Param("userId") Long userId);

    /**
     * 查询用户已读通知列表（分页）
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Notification> findReadNotifications(Page<Notification> page, @Param("userId") Long userId);

    /**
     * 查询用户未读通知列表（分页）
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Notification> findUnreadNotifications(Page<Notification> page, @Param("userId") Long userId);
}