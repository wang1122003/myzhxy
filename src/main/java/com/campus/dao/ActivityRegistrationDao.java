package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动注册数据访问接口
 */
@Mapper
public interface ActivityRegistrationDao extends BaseMapper<ActivityRegistration> {

    /**
     * 根据活动ID获取所有注册信息
     *
     * @param activityId 活动ID
     * @return 活动注册列表
     */
    List<ActivityRegistration> getRegistrationsByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据用户ID获取所有注册信息
     *
     * @param userId 用户ID
     * @return 活动注册列表
     */
    List<ActivityRegistration> getRegistrationsByUserId(@Param("userId") Long userId);

    /**
     * 添加活动注册信息
     *
     * @param registration 注册信息
     * @return 影响的行数
     */
    int addRegistration(ActivityRegistration registration);

    /**
     * 更新活动注册信息
     *
     * @param registration 注册信息
     * @return 影响的行数
     */
    int updateRegistration(ActivityRegistration registration);

    /**
     * 删除活动注册信息
     *
     * @param id 注册ID
     * @return 影响的行数
     */
    int deleteRegistration(@Param("id") Long id);

    /**
     * 根据活动ID和用户ID查询注册信息
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 注册信息
     */
    ActivityRegistration getRegistrationByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);

    /**
     * 获取活动的注册人数
     *
     * @param activityId 活动ID
     * @return 注册人数
     */
    int getRegistrationCount(@Param("activityId") Long activityId);
} 