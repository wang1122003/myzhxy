package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ActivityDao;
import com.campus.dao.UserDao;
import com.campus.entity.Activity;
import com.campus.entity.User;
import com.campus.service.ActivityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 校园活动服务实现类
 * 提供校园活动相关的业务逻辑实现
 * 包括活动的增删改查、状态管理等功能
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityDao, Activity> implements ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private UserDao userDao;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 根据ID查询活动
     * @param id 活动ID
     * @return 活动对象
     */
    @Override
    public Activity getActivityById(Long id) {
        return activityDao.findById(id);
    }
    
    /**
     * 查询所有活动
     * 返回按开始时间降序排序的活动列表
     * @return 活动列表
     */
    @Override
    public List<Activity> getAllActivities() {
        return activityDao.findAll();
    }
    
    /**
     * 根据活动类型查询活动
     * @param type 活动类型 (String)
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivitiesByType(String type) {
        return activityDao.findByType(type);
    }
    
    /**
     * 根据状态查询活动
     * @param status 状态 (String)
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivitiesByStatus(String status) {
        return activityDao.findByStatus(status);
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
            // 异常处理
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
            // 异常处理
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
        // Basic validation
        if (activity == null || !StringUtils.hasText(activity.getTitle())) {
            return false;
        }
        // Ensure create/update times are set
        Date now = new Date();
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        // Ensure status is valid string if provided, otherwise default?
        // Example: Default to "1" (active/pending) if not set or invalid?
        // For now, assume status comes pre-validated or handle in controller.
        // Linter fix: Ensure status is String if set externally before calling this.

        // If a numerical status was somehow passed, convert it here
        // This part is speculative based on linter errors elsewhere, might not be needed here
        /*
        if (activity.getStatus() != null && !(activity.getStatus() instanceof String)) {
             // This check is problematic, status is already String type in Activity entity
             // Let's assume the input `activity` object has its status correctly set as String
             // by the controller or caller. If not, the error originates there.
        }
        */

        return save(activity);
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
            // 异常处理
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
            // 异常处理
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
            // 异常处理
            // 将数组转为集合
            List<Long> idList = java.util.Arrays.asList(ids);
            return removeByIds(idList);
        }
    }

    /**
     * 更新活动状态
     * @param id 活动ID
     * @param status 状态 (String)
     * @return 是否成功
     */
    @Override
    public boolean updateActivityStatus(Long id, String status) {
        return activityDao.updateStatus(id, status) > 0;
    }

    /**
     * 分页查询活动
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param keyword 关键字
     * @param type 活动类型 (String)
     * @param status 状态 (String)
     * @return 分页结果
     */
    @Override
    public IPage<Activity> getActivityPage(int pageNo, int pageSize, String keyword, String type, String status) {
        Page<Activity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();

        // 根据关键字查询
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Activity::getTitle, keyword)
                    .or(w -> w.like(Activity::getDescription, keyword));
        }

        // 根据活动类型查询
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Activity::getType, type);
        }

        // 根据状态查询
        if (status != null) {
            queryWrapper.eq(Activity::getStatus, status);
        }

        // 按开始时间降序排序
        queryWrapper.orderByDesc(Activity::getStartTime);

        return this.page(page, queryWrapper);
    }
}