package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Classroom;

import java.util.List;

/**
 * 教室服务接口
 */
public interface ClassroomService {
    
    /**
     * 根据ID查询教室
     * @param id 教室ID
     * @return 教室对象
     */
    Classroom getClassroomById(Long id);
    
    /**
     * 根据教室编号查询教室
     * @param roomNo 教室编号
     * @return 教室对象
     */
    Classroom getClassroomByRoomNo(String roomNo);
    
    /**
     * 查询所有教室
     * @return 教室列表
     */
    List<Classroom> getAllClassrooms();
    
    /**
     * 根据教学楼查询教室
     * @param building 教学楼
     * @return 教室列表
     */
    List<Classroom> getClassroomsByBuilding(String building);
    
    /**
     * 根据教室类型查询教室
     * @param roomType 教室类型
     * @return 教室列表
     */
    List<Classroom> getClassroomsByRoomType(Integer roomType);
    
    /**
     * 根据容量范围查询教室
     * @param minCapacity 最小容量
     * @param maxCapacity 最大容量
     * @return 教室列表
     */
    List<Classroom> getClassroomsByCapacityRange(Integer minCapacity, Integer maxCapacity);
    
    /**
     * 查询可用教室
     * @return 教室列表
     */
    List<Classroom> getAvailableClassrooms();
    
    /**
     * 添加教室
     * @param classroom 教室对象
     * @return 是否成功
     */
    boolean addClassroom(Classroom classroom);
    
    /**
     * 更新教室
     * @param classroom 教室对象
     * @return 是否成功
     */
    boolean updateClassroom(Classroom classroom);
    
    /**
     * 删除教室
     * @param id 教室ID
     * @return 是否成功
     */
    boolean deleteClassroom(Long id);
    
    /**
     * 批量删除教室
     * @param ids 教室ID数组
     * @return 是否成功
     */
    boolean batchDeleteClassrooms(Long[] ids);
    
    /**
     * 更新教室状态
     * @param id 教室ID
     * @param status 状态值
     * @return 是否成功
     */
    boolean updateClassroomStatus(Long id, Integer status);

    /**
     * 分页并按条件查询教室
     *
     * @param page     当前页码
     * @param size     每页数量
     * @param keyword  关键词 (教室编号/名称)
     * @param building 教学楼
     * @param status   状态
     * @return 分页教室列表 (MyBatis-Plus IPage)
     */
    IPage<Classroom> getClassroomsByPage(int page, int size, String keyword, String building, Integer status);
}