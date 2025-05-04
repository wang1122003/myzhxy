package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Activity;
import com.campus.exception.ResourceNotFoundException;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 根据ID获取活动信息
     *
     * @param id 活动ID
     * @return 活动实体
     */
    Activity getActivityById(Long id);

    /**
     * 获取所有活动列表
     *
     * @return 活动列表
     */
    List<Activity> getAllActivities();

    /**
     * 根据活动类型查询活动
     *
     * @param type 活动类型 (String)
     * @return 活动列表
     */
    List<Activity> getActivitiesByType(String type);

    /**
     * 根据状态查询活动
     *
     * @param status 状态 (String)
     * @return 活动列表
     */
    List<Activity> getActivitiesByStatus(String status);

    /**
     * 查询进行中的活动
     *
     * @return 活动列表
     */
    List<Activity> getOngoingActivities();

    /**
     * 查询即将开始的活动
     *
     * @param days 未来天数
     * @return 活动列表
     */
    List<Activity> getUpcomingActivities(Integer days);

    /**
     * 添加活动
     *
     * @param activity 活动实体
     * @return 是否成功
     */
    boolean addActivity(Activity activity);

    /**
     * 更新活动信息
     *
     * @param activity 活动实体
     * @return 是否成功
     */
    boolean updateActivity(Activity activity);

    /**
     * 删除活动
     *
     * @param id 活动ID
     * @return 是否成功
     */
    boolean deleteActivity(Long id);

    /**
     * 批量删除活动
     *
     * @param ids 活动ID数组
     * @return 是否成功
     */
    boolean batchDeleteActivities(Long[] ids);

    /**
     * 更新活动状态
     *
     * @param id     活动ID
     * @param status 状态值 (String)
     * @return 是否成功
     */
    boolean updateActivityStatus(Long id, String status);

    /**
     * 分页并按条件查询活动列表
     *
     * @param page    页码
     * @param size    每页数量
     * @param keyword 关键词 (可选, 搜索标题或描述)
     * @param type    活动类型 (可选, String)
     * @param status  活动状态 (可选, String)
     * @return 分页后的活动列表
     */
    IPage<Activity> getActivityPage(int page, int size, String keyword, String type, String status);

    /**
     * 学生报名参加活动
     *
     * @param activityId 活动ID
     * @param userId     学生用户ID
     * @return 是否成功
     * @throws ResourceNotFoundException 如果活动不存在
     * @throws IllegalStateException     如果活动状态不允许报名或已报名
     */
    boolean joinActivity(Long activityId, Long userId);

    /**
     * 学生取消报名活动
     *
     * @param activityId 活动ID
     * @param userId     学生用户ID
     * @return 是否成功
     * @throws ResourceNotFoundException 如果活动不存在或未报名
     * @throws IllegalStateException     如果活动状态不允许取消报名
     */
    boolean cancelJoinActivity(Long activityId, Long userId);

    /**
     * 根据发布者ID获取活动分页列表
     *
     * @param publisherId 发布者ID
     * @param page        页码
     * @param size        每页数量
     * @return 分页后的活动列表
     */
    IPage<Activity> getActivitiesByPublisher(Long publisherId, int page, int size);

    /**
     * 根据用户ID获取其参加的活动分页列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 分页后的活动列表
     */
    IPage<Activity> getActivitiesJoinedByUser(Long userId, int page, int size);

    /**
     * 用户评价活动
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @param rating     评分 (e.g., 1-5)
     * @param comment    评论内容
     * @return 是否成功
     * @throws ResourceNotFoundException 如果活动或用户不存在
     * @throws IllegalArgumentException  如果评分无效
     * @throws IllegalStateException     如果用户未参加该活动或活动未结束等
     */
    boolean rateActivity(Long activityId, Long userId, Integer rating, String comment);

    /**
     * 获取活动报名列表
     *
     * @param activityId 活动ID
     * @param page       页码
     * @param size       每页数量
     * @return 活动报名学生列表
     */
    IPage<?> getActivityEnrollments(Long activityId, int page, int size);
}