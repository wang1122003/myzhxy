package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Classroom;

import java.util.Date;
import java.util.List;

/**
 * 教室服务接口
 */
public interface ClassroomService extends IService<Classroom> {
    
    /**
     * 根据ID获取教室信息
     * @param id 教室ID
     * @return 教室实体
     */
    Classroom getClassroomById(Long id);

    /**
     * 根据教室名称查询教室信息
     * @param name 教室名称
     * @return 教室实体
     */
    Classroom getClassroomByName(String name);
    
    /**
     * 获取所有教室列表
     * @return 教室列表
     */
    List<Classroom> getAllClassrooms();
    
    /**
     * 分页并按条件查询教室列表
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词 (搜索教室编号/名称)
     * @param building 教学楼 (可选)
     * @param status 状态 (可选, 0:禁用, 1:正常)
     * @return 分页后的教室列表
     */
    IPage<Classroom> getClassroomsByPage(int page, int size, String keyword, String building, Integer status);
    
    /**
     * 获取可用教室列表
     * @return 可用教室列表
     */
    List<Classroom> getAvailableClassrooms();
    
    /**
     * 添加教室
     * @param classroom 教室实体
     * @return 是否成功
     */
    boolean addClassroom(Classroom classroom);
    
    /**
     * 更新教室信息
     * @param classroom 教室实体
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
     * 检查教室在指定时间段是否可用
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param weekDay 星期几
     * @return 是否可用
     */
    boolean checkClassroomAvailability(Long classroomId, Date startTime, Date endTime, Integer weekDay);

    /**
     * 批量更新教室状态
     * @param ids 教室ID列表
     * @param status 状态值
     */
    void batchUpdateStatus(List<Long> ids, Integer status);
}