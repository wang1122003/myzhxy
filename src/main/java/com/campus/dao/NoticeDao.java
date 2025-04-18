package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Notice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 通知公告数据访问接口 (合并了NotificationDao的功能)
 */
@Repository
public interface NoticeDao extends BaseMapper<Notice> {
    
    /**
     * 根据ID查询通知
     * @param id 通知ID
     * @return 通知对象
     */
    Notice selectNoticeById(Long id);
    
    /**
     * 获取所有通知
     * @return 通知列表
     */
    List<Notice> selectAllNotices();
    
    /**
     * 根据类型查询通知
     * @param noticeType 通知类型
     * @return 通知列表
     */
    List<Notice> selectNoticesByType(Integer noticeType);
    
    /**
     * 根据状态查询通知
     * @param status 通知状态
     * @return 通知列表
     */
    List<Notice> selectNoticesByStatus(Integer status);
    
    /**
     * 获取最近的通知
     * @param limit 数量限制
     * @return 通知列表
     */
    List<Notice> selectRecentNotices(@Param("limit") Integer limit);
    
    /**
     * 获取置顶通知
     * @return 通知列表
     */
    List<Notice> selectTopNotices();
    
    /**
     * 根据发布者ID查询通知
     * @param publisherId 发布者ID
     * @return 通知列表
     */
    List<Notice> selectNoticesByPublisherId(Long publisherId);
    
    /**
     * 插入通知
     * @param notice 通知对象
     * @return 影响行数
     */
    int insertNotice(Notice notice);
    
    /**
     * 更新通知
     * @param notice 通知对象
     * @return 影响行数
     */
    int updateNotice(Notice notice);
    
    /**
     * 更新通知状态
     * @param id 通知ID
     * @param status 通知状态
     * @return 影响行数
     */
    int updateNoticeStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 更新阅读次数
     * @param id 通知ID
     * @return 影响行数
     */
    int incrementViewCount(Long id);
    
    /**
     * 删除通知
     * @param id 通知ID
     * @return 影响行数
     */
    int deleteNotice(Long id);
    
    /**
     * 批量删除通知
     * @param ids ID数组
     * @return 影响行数
     */
    int batchDeleteNotices(@Param("ids") Long[] ids);

    /**
     * 查询通知总数
     *
     * @return 总数
     */
    int countNotices();

    /**
     * 分页查询通知
     *
     * @param offset   起始偏移量
     * @param pageSize 每页数量
     * @return 通知列表
     */
    List<Notice> selectNoticesPaged(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 根据关键词搜索通知 (标题或内容)
     *
     * @param keyword 关键词
     * @return 通知列表
     */
    List<Notice> searchNoticesByKeyword(@Param("keyword") String keyword);

    /**
     * 按字符串类型获取通知
     *
     * @param type 通知类型字符串
     * @return 通知列表
     */
    List<Notice> getNotificationsByType(String type);

    /**
     * 按优先级获取通知
     *
     * @param priority 优先级
     * @return 通知列表
     */
    List<Notice> getNotificationsByPriority(Integer priority);

    /**
     * 按目标类型和ID获取通知
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 通知列表
     */
    List<Notice> getNotificationsByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 获取有效通知（未过期且状态为已发布）
     *
     * @return 通知列表
     */
    List<Notice> getActiveNotifications();

    /**
     * 搜索通知（标题、内容、类型、发送者）
     *
     * @param keyword 关键词
     * @return 通知列表
     */
    List<Notice> searchNotifications(@Param("keyword") String keyword);

    /**
     * 添加通知（兼容旧接口）
     *
     * @param notice 通知对象
     * @return 影响行数
     */
    int addNotification(Notice notice);

    /**
     * 更新通知（兼容旧接口）
     *
     * @param notice 通知对象
     * @return 影响行数
     */
    int updateNotification(Notice notice);

    /**
     * 删除通知（兼容旧接口）
     *
     * @param id 通知ID
     * @return 影响行数
     */
    int deleteNotification(Integer id);

    /**
     * 批量更新通知状态
     *
     * @param ids    ID集合
     * @param status 状态
     * @return 影响行数
     */
    int batchUpdateNotificationStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 获取通知统计信息
     *
     * @return 统计信息Map
     */
    Map<String, Object> getNotificationStats();

    /**
     * 获取通知类型统计信息
     *
     * @return 统计信息列表
     */
    List<Map<String, Object>> getTypeStats();
} 