package com.campus.service;

import com.campus.entity.Schedule;

import java.util.List;
import java.util.Map;

/**
 * 课程表服务接口
 */
public interface ScheduleService {
    
    /**
     * 根据ID查询课程表
     * @param id 课程表ID
     * @return 课程表对象
     */
    Schedule getScheduleById(Long id);
    
    /**
     * 查询所有课程表
     * @return 课程表列表
     */
    List<Schedule> getAllSchedules();
    
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
     * 根据学期ID查询课程表
     * @param termId 学期ID
     * @return 课程表列表
     */
    List<Schedule> getSchedulesByTermId(Long termId);
    
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
     * 根据学生ID查询课程表
     * @param studentId 学生ID
     * @return 课程表列表
     */
    List<Schedule> getSchedulesByStudentId(Long studentId);
    
    /**
     * 检查课程表时间冲突
     * @param schedule 待检查的课程表对象
     * @return 冲突信息，包含是否冲突和冲突详情
     */
    Map<String, Object> checkScheduleConflict(Schedule schedule);
    
    /**
     * 获取教师的周课表
     * @param teacherId 教师ID
     * @param termId 学期ID
     * @return 周课表数据
     */
    Map<String, Object> getTeacherWeeklySchedule(Long teacherId, Long termId);
    
    /**
     * 获取学生的周课表
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return 周课表数据
     */
    Map<String, Object> getStudentWeeklySchedule(Long studentId, Long termId);
    
    /**
     * 获取教室的周课表
     * @param classroomId 教室ID
     * @param termId 学期ID
     * @return 周课表数据
     */
    Map<String, Object> getClassroomWeeklySchedule(Long classroomId, Long termId);
    
    /**
     * 根据班级ID查询课表
     * @param classId 班级ID
     * @return 课表列表
     */
    List<Schedule> getSchedulesByClassId(Long classId);
    
    /**
     * 根据教师ID和学期ID查询课表
     * @param teacherId 教师ID
     * @param termId 学期ID
     * @return 课表列表
     */
    List<Schedule> getSchedulesByTeacherIdAndTermId(Long teacherId, Long termId);
    
    /**
     * 根据班级ID和学期ID查询课表
     * @param classId 班级ID
     * @param termId 学期ID
     * @return 课表列表
     */
    List<Schedule> getSchedulesByClassIdAndTermId(Long classId, Long termId);
    
    /**
     * 根据教室ID和学期ID查询课表
     * @param classroomId 教室ID
     * @param termId 学期ID
     * @return 课表列表
     */
    List<Schedule> getSchedulesByClassroomIdAndTermId(Long classroomId, Long termId);
    
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
     * 根据时间段查询课表
     * @param weekDay 星期几
     * @param timeSlot 时间段
     * @return 课表列表
     */
    List<Schedule> getSchedulesByTimeSlot(int weekDay, int timeSlot);
    
    /**
     * 检查教师时间冲突
     * @param teacherId 教师ID
     * @param weekDay 星期几
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否冲突
     */
    boolean checkTeacherTimeConflict(Long teacherId, int weekDay, int startTime, int endTime);
    
    /**
     * 检查班级时间冲突
     * @param classId 班级ID
     * @param weekDay 星期几
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否冲突
     */
    boolean checkClassTimeConflict(Long classId, int weekDay, int startTime, int endTime);
    
    /**
     * 获取教师课程数统计
     * @param teacherId 教师ID
     * @return 课程数
     */
    int getTeacherScheduleCount(Long teacherId);
    
    /**
     * 获取班级课程数统计
     * @param classId 班级ID
     * @return 课程数
     */
    int getClassScheduleCount(Long classId);
    
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
     * @param termId 学期ID
     * @return 课程数
     */
    int getTermScheduleCount(Long termId);
    
    /**
     * 获取教师课程时间分布统计
     * @param teacherId 教师ID
     * @return 时间分布统计
     */
    List<Map<String, Object>> getTeacherScheduleTimeDistribution(Long teacherId);
    
    /**
     * 获取班级课程时间分布统计
     * @param classId 班级ID
     * @return 时间分布统计
     */
    List<Map<String, Object>> getClassScheduleTimeDistribution(Long classId);
    
    /**
     * 获取教室课程时间分布统计
     * @param classroomId 教室ID
     * @return 时间分布统计
     */
    List<Map<String, Object>> getClassroomScheduleTimeDistribution(Long classroomId);
    
    /**
     * 更新课表状态
     * @param id 课表ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateScheduleStatus(Long id, Integer status);

    /**
     * 检查教室是否在课表中使用
     *
     * @param classroomId 教室ID
     * @return 如果使用返回 true，否则返回 false
     */
    boolean isClassroomInUse(Long classroomId);

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
}