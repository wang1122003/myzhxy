package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Classroom;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 教室数据访问接口
 */
@Repository
public interface ClassroomDao extends BaseMapper<Classroom> {
    /**
     * 根据ID查询教室
     * @param id 教室ID
     * @return 教室对象
     */
    Classroom findById(Long id);
    
    /**
     * 查询所有教室
     * @return 教室列表
     */
    List<Classroom> findAll();
    
    /**
     * 根据教室编号查询教室
     * @param roomNo 教室编号
     * @return 教室对象
     */
    Classroom findByRoomNo(String roomNo);
    
    /**
     * 根据教学楼查询教室
     * @param building 教学楼
     * @return 教室列表
     */
    List<Classroom> findByBuilding(String building);
    
    /**
     * 根据教室类型查询教室
     * @param type 教室类型
     * @return 教室列表
     */
    List<Classroom> findByType(String type);
    
    /**
     * 根据教室类型查询教室（Integer类型）
     * @param roomType 教室类型（Integer）
     * @return 教室列表
     */
    List<Classroom> findByRoomType(Integer roomType);
    
    /**
     * 根据容量范围查询教室
     * @param minCapacity 最小容量
     * @param maxCapacity 最大容量
     * @return 教室列表
     */
    List<Classroom> findByCapacityRange(@Param("minCapacity") Integer minCapacity, 
                                       @Param("maxCapacity") Integer maxCapacity);
    
    /**
     * 查询可用教室
     * @param dayOfWeek 星期几
     * @param timeSlot 时间段
     * @return 教室列表
     */
    List<Classroom> findAvailable(@Param("dayOfWeek") Integer dayOfWeek, 
                                 @Param("timeSlot") Integer timeSlot);
    
    /**
     * 分页查询教室
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 教室列表
     */
    List<Classroom> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取教室总数
     * @return 教室数量
     */
    int getCount();
    
    /**
     * 添加教室
     * @param classroom 教室对象
     * @return 影响行数
     */
    int insert(Classroom classroom);
    
    /**
     * 更新教室
     * @param classroom 教室对象
     * @return 影响行数
     */
    int update(Classroom classroom);
    
    /**
     * 删除教室
     * @param id 教室ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 批量删除教室
     * @param ids 教室ID数组
     * @return 影响行数
     */
    int batchDelete(Long[] ids);
    
    /**
     * 更新教室状态
     * @param id 教室ID
     * @param status 教室状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 搜索教室
     * @param keyword 关键词
     * @return 教室列表
     */
    List<Classroom> searchClassrooms(@Param("keyword") String keyword);
    
    /**
     * 获取教学楼教室数
     * @param building 教学楼
     * @return 教室数量
     */
    int getBuildingClassroomCount(String building);
    
    /**
     * 获取教室类型教室数
     * @param type 教室类型
     * @return 教室数量
     */
    int getTypeClassroomCount(String type);
    
    /**
     * 获取教室状态教室数
     * @param status 教室状态
     * @return 教室数量
     */
    int getStatusClassroomCount(Integer status);
    
    /**
     * 获取教室使用率统计
     * @return 教室使用率统计
     */
    List<Map<String, Object>> getClassroomUsageStatistics();
    
    /**
     * 获取教室容量统计
     * @return 教室容量统计
     */
    List<Map<String, Object>> getClassroomCapacityStatistics();
    
    /**
     * 检查教室编号是否存在
     * @param roomNumber 教室编号
     * @return 是否存在
     */
    boolean isRoomNumberExists(String roomNumber);
}