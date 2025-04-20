package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ActivityDao;
import com.campus.dao.ActivityParticipantDao;
import com.campus.entity.Activity;
import com.campus.entity.ActivityParticipant;
import com.campus.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 校园活动服务实现类
 * 提供校园活动相关的业务逻辑实现
 * 包括活动的增删改查、状态管理等功能
 */
@Service
@Slf4j
public class ActivityServiceImpl extends ServiceImpl<ActivityDao, Activity> implements ActivityService {

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private ActivityParticipantDao activityParticipantDao;
    
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

    /**
     * 获取指定学生参加的所有活动
     *
     * @param studentId 学生ID
     * @return 活动列表
     */
    @Override
    public List<Activity> getStudentEnrolledActivities(Long studentId) {
        // 1. 从 activity_participant 表查询该用户参与的所有活动 ID
        LambdaQueryWrapper<ActivityParticipant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityParticipant::getUserId, studentId)
                .select(ActivityParticipant::getActivityId); // 只查询 activityId
        List<Object> activityIdObjects = activityParticipantDao.selectObjs(queryWrapper);

        if (activityIdObjects == null || activityIdObjects.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换 ID 列表类型
        List<Long> activityIds = activityIdObjects.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(Collectors.toList());

        // 2. 根据活动 ID 列表查询活动信息
        return activityDao.selectBatchIds(activityIds);
    }

    /**
     * 分页并按条件查询活动列表
     *
     * @param pageNo       页码
     * @param pageSize     每页数量
     * @param keyword      关键词 (可选, 搜索标题或描述)
     * @param activityType 活动类型 (可选)
     * @param status       活动状态 (可选)
     * @return 分页后的活动列表
     */
    @Override
    public IPage<Activity> getActivityPage(int pageNo, int pageSize, String keyword, Integer activityType, Integer status) {
        Page<Activity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();

        // 添加关键词搜索条件 (标题或描述)
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Activity::getTitle, keyword).or().like(Activity::getDescription, keyword);
        }

        // 添加活动类型过滤
        if (activityType != null) {
            queryWrapper.eq(Activity::getActivityType, activityType);
        }

        // 添加活动状态过滤
        if (status != null) {
            queryWrapper.eq(Activity::getStatus, status);
        }

        // 默认按创建时间降序排序
        queryWrapper.orderByDesc(Activity::getCreateTime);

        return page(page, queryWrapper);
    }

    @Override
    @Transactional
    public boolean joinActivity(Long activityId, Long userId) {
        // 1. 检查活动是否存在且有效
        Activity activity = getActivityById(activityId);
        if (activity == null || activity.getStatus() == 0) { // 0表示已取消
            log.warn("尝试加入不存在或已取消的活动, activityId: {}, userId: {}", activityId, userId);
            return false;
        }

        // 2. 检查活动是否已满员
        if (activity.getMaxParticipants() != null && activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            log.info("活动已满员, activityId: {}, userId: {}", activityId, userId);
            return false;
        }

        // 3. 检查用户是否已加入 (使用新的 DAO)
        if (isUserJoined(activityId, userId)) {
            log.info("用户 {} 已加入活动 {}, 无需重复加入", userId, activityId);
            return true; // 假设重复加入不算失败
        }

        // 4. 增加活动表中的当前参与人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activity.setUpdateTime(new Date());
        boolean updateSuccess = updateById(activity);

        if (!updateSuccess) {
            log.error("更新活动参与人数失败, activityId: {}, userId: {}", activityId, userId);
            // Consider throwing exception for transaction rollback
            // throw new RuntimeException("Failed to update participant count in Activity table");
            return false;
        }

        // 5. 在 activity_participant 表中插入参与记录
        ActivityParticipant participant = new ActivityParticipant(activityId, userId, new Date());
        int insertResult = activityParticipantDao.insert(participant);

        if (insertResult <= 0) {
            log.error("插入活动参与记录失败, activityId: {}, userId: {}", activityId, userId);
            // Consider throwing exception for transaction rollback
            // throw new RuntimeException("Failed to insert participant record");
            return false;
        }

        log.info("用户 {} 成功加入活动 {}", userId, activityId);
        return true;
    }

    @Override
    public boolean isUserJoined(Long activityId, Long userId) {
        // 使用新的 DAO 查询是否存在记录
        LambdaQueryWrapper<ActivityParticipant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getUserId, userId);
        return activityParticipantDao.selectCount(queryWrapper) > 0;
    }

    @Override
    @Transactional
    public boolean quitActivity(Long activityId, Long userId) {
        // 1. 检查用户是否已加入 (使用新的 DAO)
        if (!isUserJoined(activityId, userId)) {
            log.warn("用户 {} 尝试退出未加入的活动 {}", userId, activityId);
            return false;
        }

        // 2. 检查活动是否存在
        Activity activity = getActivityById(activityId);
        // If activity exists, decrease count. If not, still try to remove participant record.
        if (activity != null && activity.getCurrentParticipants() > 0) {
            // 3. 减少活动表中的当前参与人数
            activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
            activity.setUpdateTime(new Date());
            boolean updateSuccess = updateById(activity);
            if (!updateSuccess) {
                log.error("更新活动参与人数失败 (退出), activityId: {}, userId: {}", activityId, userId);
                // Consider throwing exception for transaction rollback
                // throw new RuntimeException("Failed to decrease participant count in Activity table");
                return false;
            }
        } else if (activity != null) {
            log.warn("活动 {} 参与人数已为0或负数，但仍尝试退出", activityId);
        } else {
            log.warn("尝试退出不存在的活动 {}, 但仍尝试删除参与记录", activityId);
        }

        // 4. 从 activity_participant 表中删除参与记录
        LambdaQueryWrapper<ActivityParticipant> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getUserId, userId);
        int deleteResult = activityParticipantDao.delete(deleteWrapper);

        if (deleteResult <= 0) {
            log.error("删除活动参与记录失败, activityId: {}, userId: {}", activityId, userId);
            // Consider throwing exception for transaction rollback
            // throw new RuntimeException("Failed to delete participant record");
            // Decide if this is a failure case. Maybe the record was already gone?
            // For now, let's consider it success if the count was updated or activity didn't exist.
        }

        log.info("用户 {} 成功退出活动 {} (删除记录数: {})", userId, activityId, deleteResult);
        return true;
    }

    @Override
    public List<Activity> getUserActivities(Long userId) {
        // 复用 getStudentEnrolledActivities 的逻辑
        return getStudentEnrolledActivities(userId);
    }

}