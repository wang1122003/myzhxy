package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 课表数据访问接口
 */
@Repository
public interface ScheduleDao extends BaseMapper<Schedule> {

    /**
     * 根据ID查询课程表
     * @param id 课程表ID
     * @return 课程表对象
     */
    Schedule findById(Long id);

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
     * 根据教师ID和学期信息查询课程表
     * @param teacherId 教师ID
     * @param termInfo 学期信息
     * @return 课程表列表
     */
    List<Schedule> findByTeacherIdAndTerm(@Param("teacherId") Long teacherId, @Param("termInfo") String termInfo);
    
    /**
     * 检查教室时间冲突
     * @param classroomId 教室ID
     * @param dayOfWeek 星期几 (String)
     * @param startTime 开始时间 (Time)
     * @param endTime 结束时间 (Time)
     * @param termInfo 学期信息
     * @param startWeek 开始周
     * @param endWeek 结束周
     * @param id 要排除的排课ID (可选, 更新时使用)
     * @return 是否冲突
     */
    boolean checkClassroomTimeConflict(@Param("classroomId") Long classroomId,
                                       @Param("dayOfWeek") String dayOfWeek,
                                       @Param("startTime") Date startTime,
                                       @Param("endTime") Date endTime,
                                       @Param("termInfo") String termInfo,
                                       @Param("startWeek") Integer startWeek,
                                       @Param("endWeek") Integer endWeek,
                                       @Param("id") Long id);
    
    /**
     * 检查教师时间冲突
     * @param teacherId 教师ID
     * @param dayOfWeek 星期几 (String)
     * @param startTime 开始时间 (Time)
     * @param endTime 结束时间 (Time)
     * @param termInfo 学期信息
     * @param startWeek 开始周
     * @param endWeek 结束周
     * @param id 要排除的排课ID (可选, 更新时使用)
     * @return 是否冲突
     */
    boolean checkTeacherTimeConflict(@Param("teacherId") Long teacherId,
                                     @Param("dayOfWeek") String dayOfWeek,
                                     @Param("startTime") Date startTime,
                                     @Param("endTime") Date endTime,
                                     @Param("termInfo") String termInfo,
                                     @Param("startWeek") Integer startWeek,
                                     @Param("endWeek") Integer endWeek,
                                     @Param("id") Long id);

    /**
     * 分页查询课程表
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 课程表列表
     */
    List<Schedule> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据时间段查询课程表
     *
     * @param dayOfWeek 星期几 (String)
     * @param startTime 开始时间 (Time)
     * @param endTime   结束时间 (Time)
     * @return 课程表列表
     */
    List<Schedule> findByTimeSlot(@Param("dayOfWeek") String dayOfWeek, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 获取课程表总数
     * @return 课程表数量
     */
    int getCount();
    
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
     * @param termInfo 学期信息 (String)
     * @return 时间分布统计
     */
    List<Map<String, Object>> getTeacherScheduleTimeDistribution(@Param("teacherId") Long teacherId, @Param("termInfo") String termInfo);
    
    /**
     * 获取教室课程时间分布统计
     * @param classroomId 教室ID
     * @param termInfo 学期信息 (String)
     * @return 时间分布统计
     */
    List<Map<String, Object>> getClassroomScheduleTimeDistribution(@Param("classroomId") Long classroomId, @Param("termInfo") String termInfo);
    
    /**
     * 更新课程表状态
     * @param id 课程表ID
     * @param status 状态 (String)
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

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
     * 根据学生用户ID查询课程表
     * @param userId 学生用户ID
     * @return 课程表列表
     */
    List<Schedule> findByUserId(@Param("userId") Long userId);

    /**
     * 添加 Mapper 中新增的 insert, update, delete 方法
     *
     * @param schedule 课程表对象
     * @return 影响行数
     */
    int insert(Schedule schedule);

    int update(Schedule schedule);

    int delete(Long id);
}