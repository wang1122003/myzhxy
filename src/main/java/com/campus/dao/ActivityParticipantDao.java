package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ActivityParticipant;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 活动参与者数据访问接口
 */
@Repository
@Mapper
public interface ActivityParticipantDao extends BaseMapper<ActivityParticipant> {
    
    /**
     * 根据ID查询活动参与记录
     * @param id 记录ID
     * @return 活动参与记录
     */
    ActivityParticipant findById(Long id);
    
    /**
     * 根据活动ID查询所有参与者
     * @param activityId 活动ID
     * @return 参与者列表
     */
    List<ActivityParticipant> findByActivityId(Long activityId);
    
    /**
     * 根据用户ID查询已报名的活动
     * @param userId 用户ID
     * @return 活动参与记录列表
     */
    List<ActivityParticipant> findByUserId(Long userId);
    
    /**
     * 根据活动ID和用户ID查询参与记录
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 活动参与记录
     */
    ActivityParticipant findByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);
    
    /**
     * 根据活动ID和状态查询参与记录
     * @param activityId 活动ID
     * @param status 状态：0-待确认，1-已确认，2-已取消
     * @return 活动参与记录列表
     */
    List<ActivityParticipant> findByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") Integer status);
    
    /**
     * 检查用户是否已报名活动
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 存在则返回1，不存在返回0
     */
    int checkParticipation(@Param("activityId") Long activityId, @Param("userId") Long userId);
    
    /**
     * 插入活动参与记录
     * @param participant 活动参与记录
     * @return 影响行数
     */
    int insert(ActivityParticipant participant);
    
    /**
     * 更新活动参与记录
     * @param participant 活动参与记录
     * @return 影响行数
     */
    int update(ActivityParticipant participant);
    
    /**
     * 删除活动参与记录
     * @param id 记录ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 根据活动ID删除所有参与记录
     * @param activityId 活动ID
     * @return 影响行数
     */
    int deleteByActivityId(Long activityId);
    
    /**
     * 统计活动参与人数
     * @param activityId 活动ID
     * @return 参与人数
     */
    int countParticipants(Long activityId);
    
    /**
     * 统计活动签到人数
     * @param activityId 活动ID
     * @return 签到人数
     */
    int countCheckins(Long activityId);
    
    /**
     * 统计活动取消人数
     * @param activityId 活动ID
     * @return 取消人数
     */
    int countCancellations(Long activityId);
    
    /**
     * 获取用户参与活动统计
     * @param userId 用户ID
     * @return 统计数据：total-总数，signUp-已报名数，checkIn-已签到数，cancelled-已取消数
     */
    Map<String, Integer> getUserParticipationStats(Long userId);
} 