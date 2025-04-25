package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Schedule;

import java.util.List;
import java.util.Map;

/**
 * 课程表服务接口
 */
public interface ScheduleService extends IService<Schedule> {
    
    /**
     * 根据ID查询课程表
     * @param id 课程表ID
     * @return 课程表对象
     */
    Schedule getScheduleById(Long id);
    
    /**
     * 根据教师ID查询课程表
     * @param teacherId 教师ID
     * @return 课程表列表
     */
    List<Schedule> getSchedulesByTeacherId(Long teacherId);
    
    /**
     * 根据课程ID查询课程表
     * @param courseId 课程ID
     * @return 课程表列表
     */
    List<Schedule> getSchedulesByCourseId(Long courseId);
    
    /**
     * 根据教室ID查询课程表
     * @param classroomId 教室ID
     * @return 课程表列表
     */
    List<Schedule> getSchedulesByClassroomId(Long classroomId);
    
    /**
     * 添加课程表
     * @param schedule 课程表对象
     * @return 是否成功
     */
    boolean addSchedule(Schedule schedule);
    
    /**
     * 更新课程表
     * @param schedule 课程表对象
     * @return 是否成功
     */
    boolean updateSchedule(Schedule schedule);
    
    /**
     * 删除课程表
     * @param id 课程表ID
     * @return 是否成功
     */
    boolean deleteSchedule(Long id);
    
    /**
     * 批量删除课程表
     * @param ids 课程表ID数组
     * @return 是否成功
     */
    boolean batchDeleteSchedules(Long[] ids);
    
    /**
     * 根据学生用户ID查询课程表
     * @param userId 学生用户ID
     * @return 课表列表
     */
    List<Schedule> getSchedulesByUserId(Long userId);
    
    /**
     * 检查课程表时间冲突
     * @param schedule 待检查的课程表对象
     * @return 冲突信息，包含是否冲突和冲突详情
     */
    Map<String, Object> checkScheduleConflict(Schedule schedule);
    
    /**
     * 获取教师的周课表
     * @param teacherId 教师ID
     * @param termInfo 学期信息 (String)
     * @return 周课表数据
     */
    Map<String, Object> getTeacherWeeklySchedule(Long teacherId, String termInfo);
    
    /**
     * 获取学生的周课表
     * @param studentId 学生ID
     * @param termInfo 学期信息 (String)
     * @return 周课表数据
     */
    Map<String, Object> getStudentWeeklySchedule(Long studentId, String termInfo);
    
    /**
     * 获取教室的周课表
     * @param classroomId 教室ID
     * @param termInfo 学期信息 (String)
     * @return 周课表数据
     */
    Map<String, Object> getClassroomWeeklySchedule(Long classroomId, String termInfo);
    
    /**
     * 根据教师ID和学期ID查询课表
     * @param teacherId 教师ID
     * @param termInfo 学期信息 (String)
     * @return 课表列表
     */
    List<Schedule> getSchedulesByTeacherIdAndTerm(Long teacherId, String termInfo);
    
    /**
     * 根据教室ID和学期ID查询课表
     * @param classroomId 教室ID
     * @param termInfo 学期信息 (String)
     * @return 课表列表
     */
    List<Schedule> getSchedulesByClassroomIdAndTerm(Long classroomId, String termInfo);
    
    /**
     * 检查时间冲突
     * @param schedule 课表对象
     * @return 是否冲突
     */
    boolean checkTimeConflict(Schedule schedule);
    
    /**
     * 分页查询课表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 课表列表
     */
    List<Schedule> getSchedulesByPage(int pageNum, int pageSize);
    
    /**
     * 获取课表总数
     * @return 课表数量
     */
    int getScheduleCount();
    
    /**
     * 获取教师课程数统计
     * @param teacherId 教师ID
     * @return 课程数
     */
    int getTeacherScheduleCount(Long teacherId);
    
    /**
     * 获取教室课程数统计
     * @param classroomId 教室ID
     * @return 课程数
     */
    int getClassroomScheduleCount(Long classroomId);
    
    /**
     * 获取课程课程数统计
     * @param courseId 课程ID
     * @return 课程数
     */
    int getCourseScheduleCount(Long courseId);
    
    /**
     * 获取学期课程数统计
     * @param termInfo 学期信息 (String)
     * @return 课程数
     */
    int getTermScheduleCount(String termInfo);
    
    /**
     * 获取教师课程时间分布统计
     * @param teacherId 教师ID
     * @return 时间分布统计
     */
    List<Map<String, Object>> getTeacherScheduleTimeDistribution(Long teacherId, String termInfo);
    
    /**
     * 获取教室课程时间分布统计
     * @param classroomId 教室ID
     * @param termInfo 学期信息 (String)
     * @return 时间分布统计
     */
    List<Map<String, Object>> getClassroomScheduleTimeDistribution(Long classroomId, String termInfo);
    
    /**
     * 检查课程是否已被排课
     *
     * @param courseId 课程ID
     * @return 如果已排课返回 true，否则返回 false
     */
    boolean isCourseScheduled(Long courseId);

    /**
     * 查找给定课程ID列表中已被排课的课程ID
     *
     * @param courseIds 课程ID列表
     * @return 已被排课的课程ID列表
     */
    List<Long> findScheduledCourseIds(List<Long> courseIds);

    /**
     * 更新课表状态
     *
     * @param id     课表ID
     * @param status 状态 (String)
     * @return 是否成功
     */
    boolean updateScheduleStatus(Long id, String status);

    /**
     * 分页查询课表 (支持筛选)
     *
     * @param params 查询参数 (如 termId, courseName, teacherName, className 等)
     * @param page   页码
     * @param size   每页数量
     * @return 分页结果对象
     */
    IPage<Schedule> getSchedulesPage(Map<String, Object> params, int page, int size);
}