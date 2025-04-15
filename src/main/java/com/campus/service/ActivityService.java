package com.campus.service;

import java.util.List;

import com.campus.entity.Activity;

/**
 * 校园活动服务接口
 */
public interface ActivityService {
    
    /**
     * 根据ID查询活动
     * @param id 活动ID
     * @return 活动对象
     */
    Activity getActivityById(Long id);
    
    /**
     * 查询所有活动
     * @return 活动列表
     */
    List<Activity> getAllActivities();
    
    /**
     * 根据活动类型查询活动
     * @param activityType 活动类型
     * @return 活动列表
     */
    List<Activity> getActivitiesByType(Integer activityType);
    
    /**
     * 根据状态查询活动
     * @param status 状态
     * @return 活动列表
     */
    List<Activity> getActivitiesByStatus(Integer status);
    
    /**
     * 查询进行中的活动
     * @return 活动列表
     */
    List<Activity> getOngoingActivities();
    
    /**
     * 查询即将开始的活动
     * @param days 未来天数
     * @return 活动列表
     */
    List<Activity> getUpcomingActivities(Integer days);
    
    /**
     * 添加活动
     * @param activity 活动对象
     * @return 是否成功
     */
    boolean addActivity(Activity activity);
    
    /**
     * 更新活动
     * @param activity 活动对象
     * @return 是否成功
     */
    boolean updateActivity(Activity activity);
    
    /**
     * 删除活动
     * @param id 活动ID
     * @return 是否成功
     */
    boolean deleteActivity(Long id);
    
    /**
     * 批量删除活动
     * @param ids 活动ID数组
     * @return 是否成功
     */
    boolean batchDeleteActivities(Long[] ids);
    
    /**
     * 更新活动状态
     * @param id 活动ID
     * @param status 状态值
     * @return 是否成功
     */
    boolean updateActivityStatus(Long id, Integer status);
}