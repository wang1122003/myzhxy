package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Classroom;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 教室服务接口
 */
public interface ClassroomService extends IService<Classroom> {

    /**
     * 根据ID获取教室信息
     *
     * @param id 教室ID
     * @return 教室实体
     */
    Classroom getClassroomById(Long id);

    /**
     * 根据教室名称查询教室信息
     *
     * @param name 教室名称
     * @return 教室实体
     */
    Classroom getClassroomByName(String name);

    /**
     * 获取所有教室列表
     *
     * @return 教室列表
     */
    List<Classroom> getAllClassrooms();

    /**
     * 分页并按条件查询教室列表
     *
     * @param page     页码
     * @param size     每页数量
     * @param keyword  关键词 (搜索教室编号/名称)
     * @param building 教学楼 (可选)
     * @param status   状态 (可选, Integer)
     * @param roomType 教室类型 (可选, String)
     * @return 分页后的教室列表
     */
    IPage<Classroom> getClassroomsByPage(int page, int size, String keyword, String building, Integer status, String roomType);

    /**
     * 获取可用教室列表
     *
     * @return 可用教室列表
     */
    List<Classroom> getAvailableClassrooms();

    /**
     * 添加教室
     *
     * @param classroom 教室实体
     * @return 是否成功
     */
    boolean addClassroom(Classroom classroom);

    /**
     * 更新教室信息
     *
     * @param classroom 教室实体
     * @return 是否成功
     */
    boolean updateClassroom(Classroom classroom);

    /**
     * 删除教室
     *
     * @param id 教室ID
     * @return 是否成功
     */
    boolean deleteClassroom(Long id);

    /**
     * 检查教室在指定时间段是否可用
     *
     * @param classroomId 教室ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param dayOfWeek   星期几 (String)
     * @return 是否可用
     */
    boolean checkClassroomAvailability(Long classroomId, Date startTime, Date endTime, String dayOfWeek);

    /**
     * 批量更新教室状态
     *
     * @param ids    教室ID列表
     * @param status 状态值 (Integer)
     */
    void batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 批量删除教室
     *
     * @param ids 教室ID列表
     * @return 是否成功
     */
    boolean batchDeleteClassrooms(List<Long> ids);

    /**
     * 获取教室使用情况
     *
     * @param classroomId 教室ID
     * @param termInfo    学期信息
     * @param date        日期（格式：yyyy-MM-dd）
     * @return 教室使用情况数据
     */
    Map<String, Object> getClassroomUsage(Long classroomId, String termInfo, String date);

    /**
     * 获取可用的空闲教室
     *
     * @param date        日期（格式：yyyy-MM-dd）
     * @param timeSlot    时间段（格式：HH:mm-HH:mm）
     * @param weekday     星期几（1-7，1代表周一）
     * @param termInfo    学期信息
     * @param building    教学楼（可选）
     * @param roomType    教室类型（可选）
     * @param minCapacity 最小容量（可选）
     * @return 符合条件的空闲教室列表
     */
    List<Classroom> getAvailableRooms(String date, String timeSlot, Integer weekday,
                                      String termInfo, String building, String roomType,
                                      Integer minCapacity);
}