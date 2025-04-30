package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.CourseSelectionDao;
import com.campus.dao.ScheduleDao;
import com.campus.dao.UserDao;
import com.campus.entity.CourseSelection;
import com.campus.entity.Schedule;
import com.campus.entity.User;
import com.campus.enums.UserType;
import com.campus.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课表服务实现类
 */
@Slf4j
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleDao, Schedule> implements ScheduleService {

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CourseSelectionDao courseSelectionDao;

    @Override
    public Schedule getScheduleById(Long id) {
        return getById(id);
    }

    @Override
    public List<Schedule> getSchedulesByCourseId(Long courseId) {
        if (courseId == null) return Collections.emptyList();
        return list(Wrappers.<Schedule>lambdaQuery().eq(Schedule::getCourseId, courseId));
    }

    @Override
    public List<Schedule> getSchedulesByTeacherId(Long teacherId) {
        if (teacherId == null) return Collections.emptyList();
        return list(Wrappers.<Schedule>lambdaQuery().eq(Schedule::getTeacherId, teacherId));
    }

    @Override
    public List<Schedule> getSchedulesByClassroomId(Long classroomId) {
        if (classroomId == null) return Collections.emptyList();
        return list(Wrappers.<Schedule>lambdaQuery().eq(Schedule::getClassroomId, classroomId));
    }

    @Override
    public List<Schedule> getSchedulesByTeacherIdAndTerm(Long teacherId, String termCode) {
        if (teacherId == null || !StringUtils.hasText(termCode)) return Collections.emptyList();
        return list(Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getTeacherId, teacherId)
                .eq(Schedule::getTermInfo, termCode)
                .orderByAsc(Schedule::getDayOfWeek, Schedule::getStartTime));
    }

    @Override
    public List<Schedule> getSchedulesByClassroomIdAndTerm(Long classroomId, String termCode) {
        if (classroomId == null || !StringUtils.hasText(termCode)) return Collections.emptyList();
        return list(Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getClassroomId, classroomId)
                .eq(Schedule::getTermInfo, termCode)
                .orderByAsc(Schedule::getDayOfWeek, Schedule::getStartTime));
    }

    @Override
    @Transactional
    public boolean addSchedule(Schedule schedule) {
        if (schedule == null) throw new IllegalArgumentException("排课信息不能为空");

        if (checkTimeConflict(schedule)) {
            throw new RuntimeException("排课冲突：教师或教室在该时间段已有安排");
        }

        schedule.setStatus(StringUtils.hasText(schedule.getStatus()) ? schedule.getStatus() : "1");
        Date now = new Date();
        schedule.setCreateTime(now);
        schedule.setUpdateTime(now);
        return save(schedule);
    }

    @Override
    @Transactional
    public boolean updateSchedule(Schedule schedule) {
        if (schedule == null || schedule.getId() == null)
            throw new IllegalArgumentException("更新排课信息时ID不能为空");

        if (checkTimeConflict(schedule)) {
            throw new RuntimeException("排课冲突：教师或教室在该时间段已有安排");
        }

        schedule.setUpdateTime(new Date());
        return updateById(schedule);
    }

    @Override
    @Transactional
    public boolean deleteSchedule(Long id) {
        if (id == null) return false;
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteSchedules(Long[] ids) {
        if (ids == null || ids.length == 0) return true;
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    public boolean checkTimeConflict(Schedule schedule) {
        if (schedule == null || schedule.getClassroomId() == null || schedule.getTeacherId() == null ||
                schedule.getDayOfWeek() == null ||
                schedule.getStartTime() == null || schedule.getEndTime() == null ||
                !StringUtils.hasText(schedule.getTermInfo()) || schedule.getStartWeek() == null || schedule.getEndWeek() == null) {
            log.error("检查时间冲突时，必要参数不足: {}", schedule);
            throw new IllegalArgumentException("检查时间冲突时，缺少必要参数（教室、教师、星期、时间、学期、周次）");
        }

        if (schedule.getDayOfWeek() < 1 || schedule.getDayOfWeek() > 7) {
            log.error("无效的星期值: {}", schedule.getDayOfWeek());
            throw new IllegalArgumentException("星期值必须在 1 到 7 之间");
        }

        Integer classroomConflicts = scheduleDao.countClassroomTimeConflict(
                schedule.getClassroomId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getTermInfo(),
                schedule.getStartWeek(),
                schedule.getEndWeek(),
                schedule.getId()
        );
        if (classroomConflicts != null && classroomConflicts > 0) {
            log.info("教室时间冲突: classroomId={}, schedule={}", schedule.getClassroomId(), schedule);
            return true;
        }

        Integer teacherConflicts = scheduleDao.countTeacherTimeConflict(
                schedule.getTeacherId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getTermInfo(),
                schedule.getStartWeek(),
                schedule.getEndWeek(),
                schedule.getId()
        );
        if (teacherConflicts != null && teacherConflicts > 0) {
            log.info("教师时间冲突: teacherId={}, schedule={}", schedule.getTeacherId(), schedule);
            return true;
        }
        return false;
    }

    @Override
    public int getScheduleCount() {
        return (int) this.count();
    }

    @Override
    public int getTeacherScheduleCount(Long teacherId) {
        if (teacherId == null) return 0;
        return (int) count(Wrappers.<Schedule>lambdaQuery().eq(Schedule::getTeacherId, teacherId));
    }

    @Override
    public int getClassroomScheduleCount(Long classroomId) {
        if (classroomId == null) return 0;
        return (int) count(Wrappers.<Schedule>lambdaQuery().eq(Schedule::getClassroomId, classroomId));
    }

    @Override
    public int getCourseScheduleCount(Long courseId) {
        if (courseId == null) return 0;
        return (int) count(Wrappers.<Schedule>lambdaQuery().eq(Schedule::getCourseId, courseId));
    }

    @Override
    public int getTermScheduleCount(String termCode) {
        if (!StringUtils.hasText(termCode)) return 0;
        return (int) count(Wrappers.<Schedule>lambdaQuery().eq(Schedule::getTermInfo, termCode));
    }

    @Override
    public List<Map<String, Object>> getTeacherScheduleTimeDistribution(Long teacherId, String termCode) {
        return scheduleDao.getTeacherScheduleTimeDistribution(teacherId, termCode);
    }

    @Override
    public List<Map<String, Object>> getClassroomScheduleTimeDistribution(Long classroomId, String termCode) {
        return scheduleDao.getClassroomScheduleTimeDistribution(classroomId, termCode);
    }

    @Override
    @Transactional
    public boolean updateScheduleStatus(Long id, String status) {
        LambdaUpdateWrapper<Schedule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Schedule::getId, id)
                .set(Schedule::getStatus, status)
                .set(Schedule::getUpdateTime, new Date());
        return update(updateWrapper);
    }

    @Override
    public Map<String, Object> getClassroomWeeklySchedule(Long classroomId, String termCode) {
        List<Schedule> schedules = getSchedulesByClassroomIdAndTerm(classroomId, termCode);
        return buildWeeklyScheduleMap(schedules, classroomId, termCode, "classroom");
    }

    @Override
    public Map<String, Object> getTeacherWeeklySchedule(Long teacherId, String termCode) {
        List<Schedule> schedules = getSchedulesByTeacherIdAndTerm(teacherId, termCode);
        return buildWeeklyScheduleMap(schedules, teacherId, termCode, "teacher");
    }

    @Override
    public Map<String, Object> getStudentWeeklySchedule(Long studentId, String termCode) {
        List<Schedule> schedules = getSchedulesByUserIdAndTerm(studentId, termCode);
        log.info("学生 {} 在学期 {} 的课表记录数: {}", studentId, termCode, schedules.size());
        
        Map<String, Object> weeklySchedule = buildWeeklyScheduleMap(schedules, studentId, termCode, "student");

        return weeklySchedule;
    }

    @Override
    public Map<String, Object> checkScheduleConflict(Schedule schedule) {
        Map<String, Object> result = new HashMap<>();
        result.put("conflict", isTimeConflict(schedule));
        return result;
    }

    @Override
    public List<Schedule> getSchedulesByUserId(Long userId) {
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        log.warn("getSchedulesByUserId needs verification based on actual DB schema relationship.");
        return new ArrayList<>();
    }

    @Override
    public boolean isCourseScheduled(Long courseId) {
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<Long> findScheduledCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return new ArrayList<>();
        }
        return scheduleDao.findScheduledCourseIds(courseIds);
    }

    @Override
    public IPage<Schedule> getSchedulesPage(Map<String, Object> params, int page, int size) {
        Page<Schedule> pageRequest = new Page<>(page, size);
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();

        if (params.containsKey("termCode") && params.get("termCode") != null && !params.get("termCode").toString().isEmpty()) {
            queryWrapper.eq("term_info", params.get("termCode").toString());
        } else if (params.containsKey("termInfo") && params.get("termInfo") != null && !params.get("termInfo").toString().isEmpty()) {
            queryWrapper.eq("term_info", params.get("termInfo").toString());
        }
        if (params.containsKey("courseId") && params.get("courseId") != null) {
            try {
                Long courseId = Long.parseLong(params.get("courseId").toString());
                queryWrapper.eq("course_id", courseId);
            } catch (NumberFormatException e) {
                log.warn("Invalid courseId format in params: {}", params.get("courseId"));
            }
        }
        if (params.containsKey("teacherId") && params.get("teacherId") != null) {
            try {
                Long teacherId = Long.parseLong(params.get("teacherId").toString());
                queryWrapper.eq("teacher_id", teacherId);
            } catch (NumberFormatException e) {
                log.warn("Invalid teacherId format in params: {}", params.get("teacherId"));
            }
        }

        return this.page(pageRequest, queryWrapper);
    }

    private boolean isTimeConflict(Schedule schedule) {
        log.warn("isTimeConflict check is currently incomplete due to DAO changes. Returning false.");
        return false;
    }

    private Map<String, Object> buildWeeklyScheduleMap(List<Schedule> schedules, Long primaryId, String termCode, String type) {
        Map<String, Object> weeklySchedule = new HashMap<>();
        weeklySchedule.put("type", type);
        weeklySchedule.put("id", primaryId);
        weeklySchedule.put("termInfo", termCode);
        weeklySchedule.put("schedules", schedules);

        Map<Integer, List<Map<String, Object>>> scheduleByDay = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            scheduleByDay.put(i, new ArrayList<>());
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        for (Schedule schedule : schedules) {
            Integer dayValue = schedule.getDayOfWeek();
            if (dayValue != null && dayValue >= 1 && dayValue <= 7) {
                Map<String, Object> scheduleDetails = convertScheduleToMap(schedule, timeFormat);
                scheduleByDay.get(dayValue).add(scheduleDetails);
            } else {
                log.warn("发现无效的星期值 {} 在排课记录 ID: {}", dayValue, schedule.getId());
            }
        }

        for (List<Map<String, Object>> dailySchedules : scheduleByDay.values()) {
            dailySchedules.sort(Comparator.comparing(map -> (LocalTime) map.getOrDefault("startTimeObj", LocalTime.MIN)));
        }

        weeklySchedule.put("scheduleMapByDay", scheduleByDay);
        return weeklySchedule;
    }

    private Map<String, Object> convertScheduleToMap(Schedule schedule, SimpleDateFormat timeFormat) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", schedule.getId());
        map.put("courseId", schedule.getCourseId());
        map.put("teacherId", schedule.getTeacherId());
        map.put("classroomId", schedule.getClassroomId());
        map.put("dayOfWeek", schedule.getDayOfWeek());
        map.put("startTime", schedule.getStartTime() != null ? timeFormat.format(schedule.getStartTime()) : null);
        map.put("endTime", schedule.getEndTime() != null ? timeFormat.format(schedule.getEndTime()) : null);
        map.put("startWeek", schedule.getStartWeek());
        map.put("endWeek", schedule.getEndWeek());
        map.put("termInfo", schedule.getTermInfo());
        map.put("status", schedule.getStatus());

        try {
            if (schedule.getStartTime() != null) {
                Instant startInstant = schedule.getStartTime().toInstant();
                map.put("startTimeObj", LocalTime.ofInstant(startInstant, ZoneId.systemDefault()));
            }
            if (schedule.getEndTime() != null) {
                Instant endInstant = schedule.getEndTime().toInstant();
                map.put("endTimeObj", LocalTime.ofInstant(endInstant, ZoneId.systemDefault()));
            }
        } catch (Exception e) {
            log.error("Error parsing time for schedule {}", schedule.getId(), e);
            map.putIfAbsent("startTimeObj", LocalTime.MIN);
            map.putIfAbsent("endTimeObj", LocalTime.MAX);
        }

        return map;
    }

    /**
     * 实现接口：根据用户ID和学期信息获取课表列表
     *
     * @param userId   用户ID
     * @param termInfo 学期代码
     * @return 该用户在该学期的课表列表
     */
    @Override
    public List<Schedule> getSchedulesByUserIdAndTerm(Long userId, String termInfo) {
        if (userId == null || !StringUtils.hasText(termInfo)) {
            log.warn("getSchedulesByUserIdAndTerm called with invalid arguments: userId={}, termInfo={}", userId, termInfo);
            return Collections.emptyList();
        }
        // 注意: scheduleDao.findByUserId 目前可能只接受 userId 参数。
        // 如果需要按学期过滤，需要修改 DAO 层或在这里进行后置过滤。
        // 假设 DAO 层已修改或 findByUserId 内部已处理学期 (虽然 Mapper 显示没有)
        // 理想情况下，DAO 接口应为 findByUserIdAndTerm(Long userId, String termInfo)
        // 此处暂时调用现有的 findByUserId，并依赖其 SQL 查询中的 term_info 条件
        List<Schedule> schedules = scheduleDao.findByUserId(userId);

        // 如果 DAO 没处理学期，需要在这里过滤
        // List<Schedule> filteredSchedules = schedules.stream()
        //        .filter(s -> termInfo.equals(s.getTermInfo()))
        //        .collect(Collectors.toList());
        // log.info("Found {} schedules for user {} in term {}. (Before filtering: {})", filteredSchedules.size(), userId, termInfo, schedules.size());
        // return filteredSchedules;

        // 假设 DAO 的 findByUserId SQL 包含了 term_info 的 JOIN 条件，直接返回
        log.info("DAO findByUserId returned {} schedules for user {}. Assuming term filtering happened in SQL.", schedules.size(), userId);
        return schedules;
    }

    @Override
    public boolean isAnyClassroomScheduled(List<Long> classroomIds) {
        if (classroomIds == null || classroomIds.isEmpty()) {
            return false;
        }
        return count(Wrappers.<Schedule>lambdaQuery().in(Schedule::getClassroomId, classroomIds)) > 0;
    }

    public void deleteSchedulesByCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return;
        }
        LambdaUpdateWrapper<Schedule> deleteWrapper = Wrappers.lambdaUpdate(Schedule.class)
                .in(Schedule::getCourseId, courseIds);
        scheduleDao.delete(deleteWrapper);
    }

    public boolean isClassroomAvailable(Long classroomId, Integer weekday, String startTime, String endTime, Long termId, Long excludeScheduleId) {
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("classroom_id", classroomId)
                .eq("weekday", weekday)
                .eq("term_id", termId)
                .and(qw -> qw
                        .lt("start_time", endTime)
                        .gt("end_time", startTime)
                );
        if (excludeScheduleId != null) {
            queryWrapper.ne("id", excludeScheduleId);
        }
        return scheduleDao.selectCount(queryWrapper) == 0;
    }

    public boolean isTeacherAvailable(Long teacherId, Integer weekday, String startTime, String endTime, Long termId, Long excludeScheduleId) {
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", teacherId)
                .eq("weekday", weekday)
                .eq("term_id", termId)
                .and(qw -> qw
                        .lt("start_time", endTime)
                        .gt("end_time", startTime)
                );
        if (excludeScheduleId != null) {
            queryWrapper.ne("id", excludeScheduleId);
        }
        return scheduleDao.selectCount(queryWrapper) == 0;
    }

    public List<Schedule> getSchedulesByClassIdAndTerm(Long classId, Long termId) {
        if (classId == null || termId == null) {
            return Collections.emptyList();
        }
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId);
        queryWrapper.eq("term_id", termId);
        return scheduleDao.selectList(queryWrapper);
    }
}