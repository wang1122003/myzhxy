package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ActivityDao;
import com.campus.entity.Activity;
import com.campus.service.ActivityService;
import com.campus.entity.ActivityEnrollment;
import com.campus.enums.ActivityStatus;
import com.campus.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.util.Date;
import java.util.List;

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

    /**
     * 根据ID查询活动
     *
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
     *
     * @return 活动列表
     */
    @Override
    public List<Activity> getAllActivities() {
        return activityDao.findAll();
    }

    /**
     * 根据活动类型查询活动
     *
     * @param type 活动类型 (String)
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivitiesByType(String type) {
        return activityDao.findByType(type);
    }

    /**
     * 根据状态查询活动
     *
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
     *
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
     *
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
     *
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
     *
     * @param activity 活动对象
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateActivity(Activity activity) {
        activity.setUpdateTime(new Date());
        // 直接使用 ServiceImpl 的 updateById
        return this.updateById(activity);
    }

    /**
     * 删除活动
     *
     * @param id 活动ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteActivity(Long id) {
        // 直接使用 ServiceImpl 的 removeById
        return this.removeById(id);
    }

    /**
     * 批量删除活动
     *
     * @param ids 活动ID数组
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean batchDeleteActivities(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return true;
        }
        // 直接使用 ServiceImpl 的 removeByIds
        return this.removeByIds(java.util.Arrays.asList(ids));
    }

    /**
     * 更新活动状态
     *
     * @param id     活动ID
     * @param status 状态 (String)
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateActivityStatus(Long id, String status) {
        // 使用 LambdaUpdateWrapper 更新状态
        LambdaUpdateWrapper<Activity> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Activity::getId, id)
                .set(Activity::getStatus, status)
                .set(Activity::getUpdateTime, new Date());
        return this.update(updateWrapper);
        // return activityDao.updateStatus(id, status) > 0; // 移除对 DAO 自定义方法的调用
    }

    /**
     * 分页查询活动
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @param keyword  关键字
     * @param type     活动类型 (String)
     * @param status   状态 (String)
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

        // 根据活动状态查询
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Activity::getStatus, status);
        }

        // 按开始时间降序排序
        queryWrapper.orderByDesc(Activity::getStartTime);

        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional // Assume transaction needed
    public boolean joinActivity(Long activityId, Long userId) {
        log.info("User {} attempting to join activity {}", userId, activityId);
        // TODO: Implement actual logic
        // 1. Find activity by ID
        Activity activity = this.getById(activityId);
        if (activity == null) {
            log.warn("Join failed: Activity not found: {}", activityId);
            // throw new ResourceNotFoundException("Activity not found");
            return false; // Or throw exception
        }
        // 2. Check activity status/dates (is it open for registration?)
        // Example: if (!"Open".equals(activity.getStatus()) || new Date().after(activity.getEndTime())) {
        //     log.warn("Join failed: Activity {} not open for registration.", activityId);
        //     throw new IllegalStateException("Activity not open for registration");
        // }
        // 3. Check if user already joined (Requires enrollment tracking - new table/entity?)
        // Example: boolean alreadyJoined = enrollmentDao.existsByActivityAndUser(activityId, userId);
        // if (alreadyJoined) {
        //     log.warn("Join failed: User {} already joined activity {}.", userId, activityId);
        //     throw new IllegalStateException("User already joined this activity");
        // }
        // 4. Create enrollment record
        // Example: Enrollment enrollment = new Enrollment(activityId, userId, new Date());
        // enrollmentDao.insert(enrollment);

        log.warn("joinActivity logic is not fully implemented.");
        // Placeholder: Return true for now, replace with actual logic
        return true;
    }

    @Override
    @Transactional // Assume transaction needed
    public boolean cancelJoinActivity(Long activityId, Long userId) {
        log.info("User {} attempting to cancel join for activity {}", userId, activityId);
        // TODO: Implement actual logic
        // 1. Find activity by ID
        Activity activity = this.getById(activityId);
        if (activity == null) {
            log.warn("Cancel join failed: Activity not found: {}", activityId);
            // throw new ResourceNotFoundException("Activity not found");
            return false;
        }
        // 2. Check activity status/dates (can cancellation happen now?)
        // Example: if (new Date().after(activity.getStartTime())) { // Example: Cannot cancel after start
        //     log.warn("Cancel join failed: Activity {} has already started.", activityId);
        //     throw new IllegalStateException("Cannot cancel join after activity starts");
        // }
        // 3. Check if user actually joined (Requires enrollment tracking)
        // Example: Enrollment enrollment = enrollmentDao.findByActivityAndUser(activityId, userId);
        // if (enrollment == null) {
        //     log.warn("Cancel join failed: User {} not enrolled in activity {}.", userId, activityId);
        //     throw new ResourceNotFoundException("User not enrolled in this activity");
        // }
        // 4. Delete enrollment record
        // Example: enrollmentDao.deleteById(enrollment.getId());

        log.warn("cancelJoinActivity logic is not fully implemented.");
        // Placeholder: Return true for now, replace with actual logic
        return true;
    }
}