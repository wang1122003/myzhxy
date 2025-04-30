package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Schedule;
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
     * 检查教室时间冲突 (保留复杂查询)
     *
     * @param classroomId 教室ID
     * @param dayOfWeek   星期几 (Integer, 1-7)
     * @param startTime   开始时间 (Time)
     * @param endTime     结束时间 (Time)
     * @param termInfo    学期信息
     * @param startWeek   开始周
     * @param endWeek     结束周
     * @param id          要排除的排课ID (可选, 更新时使用)
     * @return 冲突数量 > 0 if conflict
     */
    Integer countClassroomTimeConflict(@Param("classroomId") Long classroomId,
                                       @Param("dayOfWeek") Integer dayOfWeek,
                                       @Param("startTime") Date startTime, // Use Date/Time type matching entity/db
                                       @Param("endTime") Date endTime,
                                       @Param("termInfo") String termInfo,
                                       @Param("startWeek") Integer startWeek,
                                       @Param("endWeek") Integer endWeek,
                                       @Param("id") Long id);

    /**
     * 检查教师时间冲突 (保留复杂查询)
     *
     * @param teacherId 教师ID
     * @param dayOfWeek 星期几 (Integer, 1-7)
     * @param startTime 开始时间 (Time)
     * @param endTime   结束时间 (Time)
     * @param termInfo  学期信息
     * @param startWeek 开始周
     * @param endWeek   结束周
     * @param id        要排除的排课ID (可选, 更新时使用)
     * @return 冲突数量 > 0 if conflict
     */
    Integer countTeacherTimeConflict(@Param("teacherId") Long teacherId,
                                     @Param("dayOfWeek") Integer dayOfWeek,
                                     @Param("startTime") Date startTime, // Use Date/Time type matching entity/db
                                     @Param("endTime") Date endTime,
                                     @Param("termInfo") String termInfo,
                                     @Param("startWeek") Integer startWeek,
                                     @Param("endWeek") Integer endWeek,
                                     @Param("id") Long id);

    /**
     * 获取教师课程时间分布统计 (保留复杂聚合查询)
     *
     * @param teacherId 教师ID
     * @param termInfo  学期信息 (String)
     * @return 时间分布统计
     */
    List<Map<String, Object>> getTeacherScheduleTimeDistribution(@Param("teacherId") Long teacherId, @Param("termInfo") String termInfo);

    /**
     * 获取教室课程时间分布统计 (保留复杂聚合查询)
     *
     * @param classroomId 教室ID
     * @param termInfo    学期信息 (String)
     * @return 时间分布统计
     */
    List<Map<String, Object>> getClassroomScheduleTimeDistribution(@Param("classroomId") Long classroomId, @Param("termInfo") String termInfo);

    /**
     * 查找给定课程ID列表中已被排课的课程ID (保留特定查询)
     *
     * @param courseIds 课程ID列表
     * @return 已被排课的课程ID列表
     */
    List<Long> findScheduledCourseIds(@Param("courseIds") List<Long> courseIds);
}