package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Notification;
// import com.campus.entity.NotificationUser; // Cannot be resolved
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 通知公告数据访问接口
 */
@Repository
public interface NotificationDao extends BaseMapper<Notification> {

    /**
     * 根据ID查询通知详情（保留，假设可能包含复杂的 Mapper XML 逻辑）
     *
     * @param id 通知ID
     * @return 通知对象
     */
    Notification findByIdWithDetails(@Param("id") Long id);

    // 以下方法可以通过 Service 层使用 BaseMapper + Wrapper 实现，移除
    // List<Notification> getAllNotifications();
    // List<Notification> findByType(@Param("type") String type);
    // List<Notification> findByStatus(@Param("status") String status);
    // List<Notification> findRecent(@Param("limit") Integer limit);
    // List<Notification> findTop();
    // List<Notification> findByPublisherId(@Param("publisherId") Long publisherId);

    // 业务逻辑应在 Service 层处理
    // int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);
    // int incrementViewCount(@Param("id") Long id);

    // BaseMapper 已提供分页查询能力，移除自定义分页接口
    // IPage<Notification> findPage(Page<Notification> page, @Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<Notification> queryWrapper);
}