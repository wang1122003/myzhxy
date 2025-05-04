package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Schedule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 课表数据访问接口 (DAO)
 */
@Repository
public interface ScheduleDao extends BaseMapper<Schedule> {

    /**
     * 检查指定时间段内教室是否已被占用 (复杂查询, 保留原生 SQL 实现)
     *
     * @param classroomId 教室ID
     * @param dayOfWeek   星期几 (数字 1-7)
     * @param startTime   开始时间 (通常为 TIME 类型)
     * @param endTime     结束时间 (通常为 TIME 类型)
     * @param termInfo    学期信息代码 (例如 "2023-2024-1")
     * @param startWeek   开始周
     * @param endWeek     结束周
     * @param id          要排除的排课ID (可选, 用于更新时检查冲突, 排除自身)
     * @return 冲突的排课数量 (大于0表示存在冲突)
     */
    Integer countClassroomTimeConflict(@Param("classroomId") Long classroomId,
                                       @Param("dayOfWeek") Integer dayOfWeek,
                                       @Param("startTime") Date startTime, // 参数类型需与数据库和实体类匹配 (Date 或 LocalTime)
                                       @Param("endTime") Date endTime,
                                       @Param("termInfo") String termInfo,
                                       @Param("startWeek") Integer startWeek,
                                       @Param("endWeek") Integer endWeek,
                                       @Param("id") Long id);

    /**
     * 检查指定时间段内教师是否已有安排 (复杂查询, 保留原生 SQL 实现)
     *
     * @param teacherId 教师ID
     * @param dayOfWeek 星期几 (数字 1-7)
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param termInfo  学期信息代码
     * @param startWeek 开始周
     * @param endWeek   结束周
     * @param id        要排除的排课ID (可选, 用于更新时检查冲突, 排除自身)
     * @return 冲突的排课数量 (大于0表示存在冲突)
     */
    Integer countTeacherTimeConflict(@Param("teacherId") Long teacherId,
                                     @Param("dayOfWeek") Integer dayOfWeek,
                                     @Param("startTime") Date startTime,
                                     @Param("endTime") Date endTime,
                                     @Param("termInfo") String termInfo,
                                     @Param("startWeek") Integer startWeek,
                                     @Param("endWeek") Integer endWeek,
                                     @Param("id") Long id);

    /**
     * 获取指定教师在指定学期的课程时间分布统计 (聚合查询)
     *
     * @param teacherId 教师ID
     * @param termInfo  学期信息代码
     * @return 时间分布统计结果列表 (例如 List<Map<String, Object>>)
     */
    List<Map<String, Object>> getTeacherScheduleTimeDistribution(@Param("teacherId") Long teacherId, @Param("termInfo") String termInfo);

    /**
     * 获取指定教室在指定学期的课程时间分布统计 (聚合查询)
     *
     * @param classroomId 教室ID
     * @param termInfo    学期信息代码
     * @return 时间分布统计结果列表
     */
    List<Map<String, Object>> getClassroomScheduleTimeDistribution(@Param("classroomId") Long classroomId, @Param("termInfo") String termInfo);

    /**
     * 查找给定课程ID列表中已被排课的课程ID (用于检查课程是否可删除等场景)
     *
     * @param courseIds 课程ID列表
     * @return 已被排课的课程ID列表 (去重)
     */
    List<Long> findScheduledCourseIds(@Param("courseIds") List<Long> courseIds);

    /**
     * 根据课程ID列表和学期信息查询课表记录 (可能用于特定业务场景, 保留)
     *
     * @param courseIds 课程ID列表
     * @param termInfo  学期信息代码
     * @return 对应的课表记录列表 (包含详细信息)
     */
    List<Schedule> findSchedulesByCourseIdsAndTerm(@Param("courseIds") List<Long> courseIds, @Param("termInfo") String termInfo);

    /**
     * 根据学生用户ID和学期查询其选课对应的课表信息 (包含课程、教师、教室、班级名称)
     *
     * @param userId   学生的 User ID
     * @param termInfo 学期代码
     * @return 包含详细信息的课表列表 (Schedule 实体填充了关联名称)
     */
    List<Schedule> findByUserIdAndTermWithDetails(@Param("userId") Long userId, @Param("termInfo") String termInfo);

    /**
     * 根据教师ID和学期查询其教授的课表信息 (包含课程、教室、班级名称)
     *
     * @param teacherId 教师的 User ID
     * @param termInfo  学期代码
     * @return 包含详细信息的课表列表
     */
    List<Schedule> findByTeacherIdAndTermWithDetails(@Param("teacherId") Long teacherId, @Param("termInfo") String termInfo);

    /**
     * 根据教室ID和学期查询该教室的课表信息 (包含课程、教师、班级名称)
     *
     * @param classroomId 教室ID
     * @param termInfo    学期代码
     * @return 包含详细信息的课表列表
     */
    List<Schedule> findByClassroomIdAndTermWithDetails(@Param("classroomId") Long classroomId, @Param("termInfo") String termInfo);
}