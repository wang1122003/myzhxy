package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ActivityDao;
import com.campus.entity.Activity;
import com.campus.exception.CustomException;
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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private ObjectMapper objectMapper;

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
     * 设置初始状态、参与者信息和创建时间等
     *
     * @param activity 活动对象
     * @return 是否成功
     */
    @Override
    public boolean addActivity(Activity activity) {
        if (activity == null || !StringUtils.hasText(activity.getTitle())) {
            log.error("Failed to add activity: Title is missing or activity is null.");
            return false;
        }
        Date now = new Date();
        if (activity.getCreateTime() == null) {
            activity.setCreateTime(now);
        }
        activity.setUpdateTime(now);
        if (!StringUtils.hasText(activity.getStatus())) {
            activity.setStatus("PENDING");
            log.warn("Activity status not set for new activity '{}', defaulting to PENDING.", activity.getTitle());
        }
        if (activity.getCurrentParticipants() == null) {
            activity.setCurrentParticipants(0);
        }
        if (!StringUtils.hasText(activity.getParticipantsJson())) {
            activity.setParticipantsJson("[]");
        }

        boolean success = save(activity);
        if (success) {
            log.info("Activity '{}' (ID: {}) added successfully.", activity.getTitle(), activity.getId());
        } else {
            log.error("Failed to save new activity '{}' to database.", activity.getTitle());
        }
        return success;
    }

    /**
     * 更新活动
     * 自动更新修改时间
     *
     * @param activity 活动对象 (包含ID)
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateActivity(Activity activity) {
        if (activity == null || activity.getId() == null) {
            log.error("Failed to update activity: Activity or ID is null.");
            return false;
        }
        activity.setUpdateTime(new Date());

        if (activity.getParticipants() != null) {
            try {
                activity.setParticipantsJson(objectMapper.writeValueAsString(activity.getParticipants()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize participants list to JSON during activity update (ID: {}): {}", activity.getId(), e.getMessage());
            }
        }

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
        LambdaUpdateWrapper<Activity> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Activity::getId, id)
                .set(Activity::getStatus, status)
                .set(Activity::getUpdateTime, new Date());
        return this.update(updateWrapper);
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

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Activity::getTitle, keyword)
                    .or(w -> w.like(Activity::getDescription, keyword));
        }
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Activity::getType, type);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Activity::getStatus, status);
        }
        queryWrapper.orderByDesc(Activity::getStartTime);

        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public boolean joinActivity(Long activityId, Long userId) {
        log.info("User {} attempting to join activity {}", userId, activityId);

        Activity activity = this.getById(activityId);
        if (activity == null) {
            log.warn("Join failed: Activity not found: {}", activityId);
            throw new CustomException("活动不存在");
        }

        Date now = new Date();
        if (activity.getRegistrationDeadline() != null && now.after(activity.getRegistrationDeadline())) {
            log.warn("Join failed: Registration deadline passed for activity {}.", activityId);
            throw new CustomException("报名已截止");
        }
        if (activity.getStartTime() != null && now.after(activity.getStartTime())) {
            log.warn("Join failed: Activity {} has already started.", activityId);
            throw new CustomException("活动已开始，无法报名");
        }

        List<Map<String, Object>> participants = parseParticipantsJson(activity.getParticipantsJson(), activity.getId());

        boolean alreadyJoined = participants.stream()
                .anyMatch(p -> userId.equals(((Number) p.get("userId")).longValue()));
        if (alreadyJoined) {
            log.warn("Join failed: User {} already joined activity {}.", userId, activityId);
            throw new CustomException("您已报名该活动");
        }

        int currentCount = activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0;
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0 && currentCount >= activity.getMaxParticipants()) {
            log.warn("Join failed: Activity {} is full (Max: {}).", activityId, activity.getMaxParticipants());
            throw new CustomException("活动报名人数已满");
        }

        Map<String, Object> newParticipant = new HashMap<>();
        newParticipant.put("userId", userId);
        newParticipant.put("registrationTime", now);

        participants.add(newParticipant);

        try {
            activity.setParticipantsJson(objectMapper.writeValueAsString(participants));
            activity.setCurrentParticipants(participants.size());
            activity.setUpdateTime(now);

            boolean updated = this.updateById(activity);
            if (updated) {
                log.info("User {} successfully joined activity {}", userId, activityId);
                return true;
            } else {
                log.error("Failed to update activity {} after adding participant {}.", activityId, userId);
                throw new CustomException("报名失败，请稍后重试");
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize participants list to JSON during join (Activity ID: {}): {}", activityId, e.getMessage());
            throw new CustomException("报名处理失败，请联系管理员");
        }
    }

    @Override
    @Transactional
    public boolean cancelJoinActivity(Long activityId, Long userId) {
        log.info("User {} attempting to cancel join for activity {}", userId, activityId);

        Activity activity = this.getById(activityId);
        if (activity == null) {
            log.warn("Cancel join failed: Activity not found: {}", activityId);
            throw new CustomException("活动不存在");
        }

        Date now = new Date();
        if (activity.getStartTime() != null && now.after(activity.getStartTime())) {
            log.warn("Cancel join failed: Activity {} has already started.", activityId);
            throw new CustomException("活动已开始，无法取消报名");
        }

        List<Map<String, Object>> participants = parseParticipantsJson(activity.getParticipantsJson(), activity.getId());

        Optional<Map<String, Object>> participantToRemove = participants.stream()
                .filter(p -> userId.equals(((Number) p.get("userId")).longValue()))
                .findFirst();

        if (participantToRemove.isEmpty()) {
            log.warn("Cancel join failed: User {} was not enrolled in activity {}.", userId, activityId);
            throw new CustomException("您未报名该活动");
        }

        participants.remove(participantToRemove.get());

        try {
            activity.setParticipantsJson(objectMapper.writeValueAsString(participants));
            activity.setCurrentParticipants(participants.size());
            activity.setUpdateTime(now);

            boolean updated = this.updateById(activity);
            if (updated) {
                log.info("User {} successfully cancelled enrollment for activity {}", userId, activityId);
                return true;
            } else {
                log.error("Failed to update activity {} after removing participant {}.", activityId, userId);
                throw new CustomException("取消报名失败，请稍后重试");
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize participants list to JSON during cancellation (Activity ID: {}): {}", activityId, e.getMessage());
            throw new CustomException("取消报名处理失败，请联系管理员");
        }
    }

    @Override
    public IPage<Activity> getActivitiesByPublisher(Long publisherId, int page, int size) {
        log.info("Fetching activities published by user ID: {}, page: {}, size: {}", publisherId, page, size);
        if (publisherId == null) {
            log.error("Publisher ID cannot be null when fetching activities by publisher.");
            return new Page<>(page, size);
        }

        Page<Activity> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Activity::getOrganizerId, publisherId);
        queryWrapper.orderByDesc(Activity::getCreateTime);

        IPage<Activity> activityPage = baseMapper.selectPage(pageInfo, queryWrapper);
        log.info("Found {} activities published by user ID: {}", activityPage.getTotal(), publisherId);
        return activityPage;
    }

    /**
     * 根据用户ID获取其参加的活动分页列表 (效率较低)
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 分页后的活动列表
     */
    @Override
    public IPage<Activity> getActivitiesJoinedByUser(Long userId, int page, int size) {
        log.warn("Fetching joined activities for user {} using JSON parsing - This can be inefficient for large datasets!", userId);

        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Activity::getStartTime);
        List<Activity> allActivities = list(queryWrapper);

        List<Activity> joinedActivities = allActivities.stream()
                .filter(activity -> {
                    List<Map<String, Object>> participants = parseParticipantsJson(activity.getParticipantsJson(), activity.getId());
                    return participants.stream().anyMatch(p -> userId.equals(((Number) p.get("userId")).longValue()));
                })
                .collect(Collectors.toList());

        int total = joinedActivities.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Activity> paginatedList;
        if (fromIndex >= total) {
            paginatedList = Collections.emptyList();
        } else {
            paginatedList = joinedActivities.subList(fromIndex, toIndex);
        }

        IPage<Activity> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(paginatedList);

        log.info("Found {} joined activities for user ID: {} (Page: {}, Size: {})", total, userId, page, size);
        return resultPage;
    }

    /**
     * 用户评价活动
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @param rating     评分 (e.g., 1-5)
     * @param comment    评论内容
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean rateActivity(Long activityId, Long userId, Integer rating, String comment) {
        log.info("User {} attempting to rate activity {} with rating {} and comment: '{}'",
                userId, activityId, rating, comment);

        if (activityId == null || userId == null || rating == null || rating < 1 || rating > 5) {
            log.error("Invalid input for rating activity. Activity ID: {}, User ID: {}, Rating: {}", activityId, userId, rating);
            throw new IllegalArgumentException("评分输入无效");
        }
        if (comment != null && comment.length() > 1000) {
            log.error("Invalid input for rating activity: Comment exceeds maximum length (1000 chars).");
            throw new IllegalArgumentException("评论内容过长");
        }

        Activity activity = this.getById(activityId);
        if (activity == null) {
            log.warn("Rate failed: Activity not found: {}", activityId);
            throw new CustomException("活动不存在");
        }

        List<Map<String, Object>> participants = parseParticipantsJson(activity.getParticipantsJson(), activity.getId());
        boolean participated = participants.stream()
                .anyMatch(p -> userId.equals(((Number) p.get("userId")).longValue()));
        if (!participated) {
            log.warn("Rate failed: User {} did not participate in activity {}.", userId, activityId);
            throw new CustomException("您未参加该活动，无法评价");
        }

        log.warn("rateActivity: Check for existing rating is not implemented.");

        return true;
    }

    private List<Map<String, Object>> parseParticipantsJson(String json, Long activityId) {
        if (!StringUtils.hasText(json) || "[]".equals(json.trim())) {
            return new ArrayList<>();
        }
        try {
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {
            };
            List<Map<String, Object>> participants = objectMapper.readValue(json, typeRef);
            return participants != null ? new ArrayList<>(participants) : new ArrayList<>();
        } catch (IOException e) {
            log.error("Failed to parse participants JSON for activity ID {}: {}", activityId, e.getMessage());
            return new ArrayList<>();
        }
    }
}