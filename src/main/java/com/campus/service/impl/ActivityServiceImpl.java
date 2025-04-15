package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ActivityDao;
import com.campus.entity.Activity;
import com.campus.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 校园活动服务实现类
 * 提供校园活动相关的业务逻辑实现
 * 包括活动的增删改查、状态管理等功能
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityDao, Activity> implements ActivityService {

    @Autowired
    private ActivityDao activityDao;
    
    /**
     * 根据ID查询活动
     * @param id 活动ID
     * @return 活动对象
     */
    @Override
    public Activity getActivityById(Long id) {
        return activityDao.findById(id) != null ? activityDao.findById(id) : getById(id);
    }
    
    /**
     * 查询所有活动
     * 返回按开始时间降序排序的活动列表
     * @return 活动列表
     */
    @Override
    public List<Activity> getAllActivities() {
        try {
            List<Activity> activities = activityDao.findAll();
            if (activities != null && !activities.isEmpty()) {
                return activities;
            }
        } catch (Exception e) {
            // 记录日志或处理异常
        }
        
        // 如果自定义方法失败，则使用MyBatis-Plus的方法
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Activity::getStartTime);
        return list(queryWrapper);
    }
    
    /**
     * 根据活动类型查询活动
     * @param activityType 活动类型
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivitiesByType(Integer activityType) {
        try {
            return activityDao.findByActivityType(activityType);
        } catch (Exception e) {
            // 记录日志或处理异常
            LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Activity::getActivityType, activityType)
                       .orderByDesc(Activity::getStartTime);
            return list(queryWrapper);
        }
    }
    
    /**
     * 根据状态查询活动
     * @param status 状态
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivitiesByStatus(Integer status) {
        try {
            return activityDao.findByStatus(status);
        } catch (Exception e) {
            // 记录日志或处理异常
            LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Activity::getStatus, status)
                       .orderByDesc(Activity::getStartTime);
            return list(queryWrapper);
        }
    }
    
    /**
     * 查询进行中的活动
     * 筛选当前时间在活动开始和结束时间之间的活动
     * @return 活动列表
     */
    @Override
    public List<Activity> getOngoingActivities() {
        try {
            return activityDao.findOngoing();
        } catch (Exception e) {
            // 记录日志或处理异常
            Date now = new Date();
            LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.le(Activity::getStartTime, now)
                       .ge(Activity::getEndTime, now)
                       .orderByDesc(Activity::getStartTime);
            return list(queryWrapper);
        }
    }
    
    /**
     * 查询即将开始的活动
     * @param days 未来天数
     * @return 活动列表
     */
    @Override
    public List<Activity> getUpcomingActivities(Integer days) {
        try {
            return activityDao.findUpcoming(days);
        } catch (Exception e) {
            // 记录日志或处理异常
            Date now = new Date();
            // 计算指定天数后的日期
            Date future = new Date(now.getTime() + days * 24 * 60 * 60 * 1000L);
            
            LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.gt(Activity::getStartTime, now)
                       .lt(Activity::getStartTime, future)
                       .orderByAsc(Activity::getStartTime);
            return list(queryWrapper);
        }
    }
    
    /**
     * 添加活动
     * 设置初始状态和创建时间等信息
     * @param activity 活动对象
     * @return 是否成功
     */
    @Override
    public boolean addActivity(Activity activity) {
        // 设置初始状态和时间
        if (activity.getStatus() == null) {
            activity.setStatus(0); // 默认未开始
        }
        
        Date now = new Date();
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        
        // 初始化报名人数
        if (activity.getCurrentParticipants() == null) {
            activity.setCurrentParticipants(0);
        }
        
        try {
            int result = activityDao.insert(activity);
            return result > 0;
        } catch (Exception e) {
            // 记录日志或处理异常
            return save(activity);
        }
    }
    
    /**
     * 更新活动
     * 自动更新修改时间
     * @param activity 活动对象
     * @return 是否成功
     */
    @Override
    public boolean updateActivity(Activity activity) {
        // 更新修改时间
        activity.setUpdateTime(new Date());
        
        try {
            int result = activityDao.update(activity);
            return result > 0;
        } catch (Exception e) {
            // 记录日志或处理异常
            return updateById(activity);
        }
    }
    
    /**
     * 删除活动
     * @param id 活动ID
     * @return 是否成功
     */
    @Override
    public boolean deleteActivity(Long id) {
        try {
            int result = activityDao.delete(id);
            return result > 0;
        } catch (Exception e) {
            // 记录日志或处理异常
            return removeById(id);
        }
    }
    
    /**
     * 批量删除活动
     * @param ids 活动ID数组
     * @return 是否成功
     */
    @Override
    public boolean batchDeleteActivities(Long[] ids) {
        try {
            int result = activityDao.batchDelete(ids);
            return result > 0;
        } catch (Exception e) {
            // 记录日志或处理异常
            // 将数组转为集合
            List<Long> idList = java.util.Arrays.asList(ids);
            return removeByIds(idList);
        }
    }
    
    /**
     * 更新活动状态
     * @param id 活动ID
     * @param status 状态值
     * @return 是否成功
     */
    @Override
    public boolean updateActivityStatus(Long id, Integer status) {
        try {
            int result = activityDao.updateStatus(id, status);
            return result > 0;
        } catch (Exception e) {
            // 记录日志或处理异常
            Activity activity = new Activity();
            activity.setId(id);
            activity.setStatus(status);
            activity.setUpdateTime(new Date());
            return updateById(activity);
        }
    }
}