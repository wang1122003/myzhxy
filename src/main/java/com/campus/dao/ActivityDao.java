package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Activity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 校园活动数据访问接口
 * 提供对Activity实体的各种数据库操作
 */
@Repository
@Mapper
public interface ActivityDao extends BaseMapper<Activity> {
    /**
     * 根据ID查询活动
     * @param id 活动ID
     * @return 活动对象
     */
    Activity findById(Long id);
    
    /**
     * 查询所有活动
     * @return 活动列表
     */
    List<Activity> findAll();
    
    /**
     * 根据活动类型查询活动
     * @param type 活动类型
     * @return 活动列表
     */
    List<Activity> findByType(String type);
    
    /**
     * 根据活动类型查询活动（Integer类型）
     * @param activityType
     * @return 活动列表
     */
    List<Activity> findByActivityType(Integer activityType);
    
    /**
     * 根据日期范围查询活动
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 活动列表
     */
    List<Activity> findByDateRange(@Param("startDate") Date startDate, 
                                  @Param("endDate") Date endDate);
    
    /**
     * 根据状态查询活动
     * @param status 活动状态
     * @return 活动列表
     */
    List<Activity> findByStatus(Integer status);
    
    /**
     * 查询正在进行中的活动
     * @return 活动列表
     */
    List<Activity> findOngoing();
    
    /**
     * 查询即将开始的活动
     * @param days 未来天数
     * @return 活动列表
     */
    List<Activity> findUpcoming(Integer days);
    
    /**
     * 分页查询活动
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 活动列表
     */
    List<Activity> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取活动总数
     * @return 活动数量
     */
    int getCount();
    
    /**
     * 添加活动
     * @param activity 活动对象
     * @return 影响行数
     */
    int insert(Activity activity);
    
    /**
     * 更新活动
     * @param activity 活动对象
     * @return 影响行数
     */
    int update(Activity activity);
    
    /**
     * 删除活动
     * @param id 活动ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 批量删除活动
     * @param ids 活动ID数组
     * @return 影响行数
     */
    int batchDelete(Long[] ids);
    
    /**
     * 更新活动状态
     * @param id 活动ID
     * @param status 活动状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 搜索活动
     * @param keyword 关键词
     * @return 活动列表
     */
    List<Activity> searchActivities(@Param("keyword") String keyword);
    
    /**
     * 获取最近的活动
     * @param limit 数量限制
     * @return 活动列表
     */
    List<Activity> getRecentActivities(@Param("limit") Integer limit);
    
    /**
     * 获取活动类型统计
     * @return 活动类型统计
     */
    List<Map<String, Object>> getActivityTypeStatistics();
    
    /**
     * 获取活动状态统计
     * @return 活动状态统计
     */
    List<Map<String, Object>> getActivityStatusStatistics();
    
    /**
     * 获取活动参与人数统计
     * @param activityId 活动ID
     * @return 参与人数
     */
    int getActivityParticipantCount(Long activityId);
    
    /**
     * 获取活动类型活动数
     * @param type 活动类型
     * @return 活动数量
     */
    int getActivityTypeCount(String type);
    
    /**
     * 获取活动状态活动数
     * @param status 活动状态
     * @return 活动数量
     */
    int getActivityStatusCount(Integer status);
    
    /**
     * 检查活动名称是否存在
     * @param name 活动名称
     * @return 是否存在
     */
    boolean isActivityNameExists(String name);
    
    /**
     * 获取学生参与的活动
     * @param studentId 学生ID
     * @return 活动列表
     */
    List<Activity> getStudentActivities(Long studentId);
    
    /**
     * 获取组织者的活动
     * @param organizerId 组织者ID
     * @return 活动列表
     */
    List<Activity> getOrganizerActivities(Long organizerId);
}