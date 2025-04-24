package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Schedule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 课表数据访问接口
 */
@Repository
public interface ScheduleDao extends BaseMapper<Schedule> {

    @Override
    default Schedule selectById(Serializable id) {
        return selectOne(Wrappers.<Schedule>query().eq("id", id));
    }

    @Override
    default List<Schedule> selectList(Wrapper<Schedule> wrapper) {
        return selectAll(wrapper);
    }
    /**
     * 根据ID查询课程表
     * @param id 课程表ID
     * @return 课程表对象
     */
    
    
    /**
     * 根据课程ID查询课程表
     * @param courseId 课程ID
     * @return 课程表列表
     */
    List<Schedule> findByCourseId(Long courseId);
    
    /**
     * 根据教师ID查询课程表
     * @param teacherId 教师ID
     * @return 课程表列表
     */
    List<Schedule> findByTeacherId(Long teacherId);
    
    /**
     * 根据教室ID查询课程表
     * @param classroomId 教室ID
     * @return 课程表列表
     */
    List<Schedule> findByClassroomId(Long classroomId);
    
    /**
     * 根据班级ID查询课程表
     * @param classId 班级ID
     * @return 课程表列表
     */
    List<Schedule> findByClassId(Long classId);
    
    /**
     * 根据学期ID查询课程表
     * @param termId 学期ID
     * @return 课程表列表
     */
    List<Schedule> findByTermId(Long termId);
    
    /**
     * 根据教师ID和学期ID查询课程表
     * @param teacherId 教师ID
     * @param termId 学期ID
     * @return 课程表列表
     */
    List<Schedule> findByTeacherIdAndTermId(@Param("teacherId") Long teacherId, @Param("termId") Long termId);
    
    /**
     * 根据班级ID和学期ID查询课程表
     * @param classId 班级ID
     * @param termId 学期ID
     * @return 课程表列表
     */
    List<Schedule> findByClassIdAndTermId(@Param("classId") Long classId, @Param("termId") Long termId);
    
    /**
     * 根据教室ID和学期ID查询课程表
     * @param classroomId 教室ID
     * @param termId 学期ID
     * @return 课程表列表
     */
    List<Schedule> findByClassroomIdAndTermId(@Param("classroomId") Long classroomId, @Param("termId") Long termId);
    
    /**
     * 检查时间冲突
     * @param classroomId 教室ID
     * @param weekDay 星期几
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否冲突
     */
    boolean checkTimeConflict(@Param("classroomId") Integer classroomId, 
                            @Param("weekDay") Integer weekDay,
                            @Param("startTime") Integer startTime,
                            @Param("endTime") Integer endTime);
    
    
    /**
     * 批量删除课程表
     * @param ids 课程表ID数组
     * @return 影响行数
     */
    int batchDelete(Long[] ids);
    
    /**
     * 分页查询课程表
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 课程表列表
     */
    List<Schedule> selectAll(@Param("ew") Wrapper<Schedule> wrapper);
    
    /**
     * 获取课程表总数
     * @return 课程表数量
     */
    int getCount();
    
    /**
     * 根据时间段查询课程表
     * @param weekDay 星期几
     * @param timeSlot 时间段
     * @return 课程表列表
     */
    List<Schedule> findByTimeSlot(@Param("weekDay") Integer weekDay, @Param("timeSlot") Integer timeSlot);
    
    /**
     * 根据课程ID和学期ID查询课程表
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 课程表列表
     */
    List<Schedule> findByCourseIdAndTermId(@Param("courseId") Long courseId, @Param("termId") Long termId);
    
    /**
     * 检查教师时间冲突
     * @param teacherId 教师ID
     * @param weekDay 星期几
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否冲突
     */
    boolean checkTeacherTimeConflict(@Param("teacherId") Long teacherId,
                                   @Param("weekDay") Integer weekDay,
                                   @Param("startTime") Integer startTime,
                                   @Param("endTime") Integer endTime);
    
    /**
     * 检查班级时间冲突
     * @param classId 班级ID
     * @param weekDay 星期几
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否冲突
     */
    boolean checkClassTimeConflict(@Param("classId") Long classId,
                                 @Param("weekDay") Integer weekDay,
                                 @Param("startTime") Integer startTime,
                                 @Param("endTime") Integer endTime);
    
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
     * 更新课程表状态
     * @param id 课程表ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据教室ID统计课表数量
     *
     * @param classroomId 教室ID
     * @return 数量
     */
    long countByClassroomId(@Param("classroomId") Long classroomId);

    /**
     * 根据课程ID统计课表数量
     *
     * @param courseId 课程ID
     * @return 数量
     */
    long countByCourseId(@Param("courseId") Long courseId);

    /**
     * 查找给定课程ID列表中已被排课的课程ID
     *
     * @param courseIds 课程ID列表
     * @return 已被排课的课程ID列表
     */
    List<Long> findScheduledCourseIds(@Param("courseIds") List<Long> courseIds);

    /**
     * 根据学生ID查询其所有课表安排
     *
     * @param studentId 学生ID (通常是 user 表的 ID)
     * @return 该学生参与的课表列表
     */
    List<Schedule> findByStudentId(@Param("studentId") Long studentId);
}