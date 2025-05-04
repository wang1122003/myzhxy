package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ScheduleDao;
import com.campus.entity.Schedule;
import com.campus.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 课表服务实现类
 */
@Slf4j
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleDao, Schedule> implements ScheduleService {

    @Autowired
    private ScheduleDao scheduleDao;

    // 定义前端使用的10个时间槽边界用于映射
    private static final List<Map<String, LocalTime>> TIME_SLOT_BOUNDARIES = List.of(
            Map.of("start", LocalTime.of(8, 0), "end", LocalTime.of(8, 45)),   // 第1节
            Map.of("start", LocalTime.of(8, 55), "end", LocalTime.of(9, 40)),   // 第2节
            Map.of("start", LocalTime.of(10, 0), "end", LocalTime.of(10, 45)),  // 第3节
            Map.of("start", LocalTime.of(10, 55), "end", LocalTime.of(11, 40)), // 第4节
            Map.of("start", LocalTime.of(14, 0), "end", LocalTime.of(14, 45)), // 第5节
            Map.of("start", LocalTime.of(14, 55), "end", LocalTime.of(15, 40)), // 第6节
            Map.of("start", LocalTime.of(16, 0), "end", LocalTime.of(16, 45)), // 第7节
            Map.of("start", LocalTime.of(16, 55), "end", LocalTime.of(17, 40)), // 第8节
            Map.of("start", LocalTime.of(19, 0), "end", LocalTime.of(19, 45)), // 第9节
            Map.of("start", LocalTime.of(19, 55), "end", LocalTime.of(20, 40))  // 第10节
    );

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
        return scheduleDao.findByTeacherIdAndTermWithDetails(teacherId, termCode);
    }

    @Override
    public List<Schedule> getSchedulesByClassroomIdAndTerm(Long classroomId, String termCode) {
        if (classroomId == null || !StringUtils.hasText(termCode)) return Collections.emptyList();
        return scheduleDao.findByClassroomIdAndTermWithDetails(classroomId, termCode);
    }

    @Override
    @Transactional
    public boolean addSchedule(Schedule schedule) {
        if (schedule == null) throw new IllegalArgumentException("排课信息不能为空");

        if (checkTimeConflict(schedule)) {
            log.warn("添加排课冲突: {}", schedule);
            throw new RuntimeException("排课冲突：教师或教室在该时间段已有安排");
        }

        schedule.setStatus(StringUtils.hasText(schedule.getStatus()) ? schedule.getStatus() : "1");
        Date now = new Date();
        schedule.setCreateTime(now);
        schedule.setUpdateTime(now);
        boolean success = save(schedule);
        if (success) {
            log.info("成功添加排课记录: ID={}", schedule.getId());
        } else {
            log.error("添加排课记录失败: {}", schedule);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean updateSchedule(Schedule schedule) {
        if (schedule == null || schedule.getId() == null)
            throw new IllegalArgumentException("更新排课信息时ID不能为空");

        if (checkTimeConflict(schedule)) {
            log.warn("更新排课冲突: {}", schedule);
            throw new RuntimeException("排课冲突：教师或教室在该时间段已有安排");
        }

        schedule.setUpdateTime(new Date());
        boolean success = updateById(schedule);
        if (success) {
            log.info("成功更新排课记录: ID={}", schedule.getId());
        } else {
            log.warn("更新排课记录失败或未找到记录: ID={}", schedule.getId());
        }
        return success;
    }

    @Override
    @Transactional
    public boolean deleteSchedule(Long id) {
        if (id == null) return false;
        boolean success = removeById(id);
        if (success) {
            log.info("成功删除排课记录: ID={}", id);
        } else {
            log.warn("删除排课记录失败或未找到记录: ID={}", id);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean batchDeleteSchedules(Long[] ids) {
        if (ids == null || ids.length == 0) return true;
        boolean success = removeByIds(Arrays.asList(ids));
        if (success) {
            log.info("成功批量删除排课记录: IDs={}", Arrays.toString(ids));
        } else {
            log.warn("批量删除排课记录可能部分失败: IDs={}", Arrays.toString(ids));
        }
        return success;
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

        if (schedule.getStartTime().after(schedule.getEndTime())) {
            log.error("检查时间冲突时，开始时间晚于结束时间: {}", schedule);
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
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
        boolean success = update(updateWrapper);
        if (success) {
            log.info("成功更新排课状态: ID={}, Status={}", id, status);
        } else {
            log.warn("更新排课状态失败或未找到记录: ID={}", id);
        }
        return success;
    }

    @Override
    public Map<String, Object> getClassroomWeeklySchedule(Long classroomId, String termCode) {
        try {
            if (classroomId == null || !StringUtils.hasText(termCode)) {
                log.error("教室课表查询参数无效: classroomId={}, termCode={}", classroomId, termCode);
                return Map.of("error", "无效的查询参数", "schedules", Collections.emptyList());
            }

            log.info("开始获取教室课表: classroomId={}, termCode={}", classroomId, termCode);
            List<Schedule> schedules = scheduleDao.findByClassroomIdAndTermWithDetails(classroomId, termCode);
            log.info("教室 {} 在学期 {} 的课表记录数: {}", classroomId, termCode,
                    schedules == null ? "null" : schedules.size());

            if (schedules == null) {
                schedules = Collections.emptyList();
                log.warn("教室课表查询结果为null，使用空列表替代");
            }

            try {
                Map<String, Object> result = buildWeeklyScheduleMap(schedules, classroomId, termCode, "classroom");
                log.info("教室课表构建完成，包含 {} 条记录", schedules.size());
                return result;
            } catch (Exception e) {
                log.error("构建教室周课表时发生错误: {}", e.getMessage(), e);
                return Map.of("error", "构建课表格式时发生错误", "schedules", Collections.emptyList());
            }
        } catch (Exception e) {
            log.error("获取教室课表时发生错误: classroomId={}, termCode={}, 错误信息: {}", classroomId, termCode, e.getMessage(), e);
            return Map.of("error", "服务器内部错误", "schedules", Collections.emptyList());
        }
    }

    @Override
    public Map<String, Object> getTeacherWeeklySchedule(Long teacherId, String termCode) {
        try {
            if (teacherId == null || !StringUtils.hasText(termCode)) {
                log.error("教师课表查询参数无效: teacherId={}, termCode={}", teacherId, termCode);
                return Map.of("error", "无效的查询参数", "schedules", Collections.emptyList());
            }

            log.info("开始获取教师课表: teacherId={}, termCode={}", teacherId, termCode);
            List<Schedule> schedules = scheduleDao.findByTeacherIdAndTermWithDetails(teacherId, termCode);
            log.info("教师 {} 在学期 {} 的课表记录数: {}", teacherId, termCode, schedules == null ? "null" : schedules.size());

            if (schedules == null) {
                schedules = Collections.emptyList();
                log.warn("教师课表查询结果为null，使用空列表替代");
            }

            try {
                Map<String, Object> result = buildWeeklyScheduleMap(schedules, teacherId, termCode, "teacher");
                log.info("课表构建完成，包含 {} 条记录", schedules.size());
                return result;
            } catch (Exception e) {
                log.error("构建周课表时发生错误: {}", e.getMessage(), e);
                return Map.of("error", "构建课表格式时发生错误", "schedules", Collections.emptyList());
            }
        } catch (Exception e) {
            log.error("获取教师课表时发生错误: teacherId={}, termCode={}, 错误信息: {}", teacherId, termCode, e.getMessage(), e);
            return Map.of("error", "服务器内部错误", "schedules", Collections.emptyList());
        }
    }

    @Override
    public Map<String, Object> getStudentWeeklySchedule(Long studentId, String termCode) {
        try {
            if (studentId == null || !StringUtils.hasText(termCode)) {
                log.error("学生课表查询参数无效: studentId={}, termCode={}", studentId, termCode);
                return Map.of("error", "无效的查询参数", "schedules", Collections.emptyList());
            }

            log.info("开始获取学生课表: studentId={}, termCode={}", studentId, termCode);
            List<Schedule> rawSchedules = scheduleDao.findByUserIdAndTermWithDetails(studentId, termCode);
            log.info("学生 {} 在学期 {} 的课表记录数 (含详情): {}", studentId, termCode,
                    rawSchedules == null ? "null" : rawSchedules.size());

            if (rawSchedules == null) {
                rawSchedules = Collections.emptyList();
                log.warn("学生课表查询结果为null，使用空列表替代");
            }

            try {
                Map<String, Object> weeklySchedule = buildWeeklyScheduleMap(rawSchedules, studentId, termCode, "student");
                log.info("学生课表构建完成，包含 {} 条记录", rawSchedules.size());
        return weeklySchedule;
            } catch (Exception e) {
                log.error("构建学生周课表时发生错误: {}", e.getMessage(), e);
                return Map.of("error", "构建课表格式时发生错误", "schedules", Collections.emptyList());
            }
        } catch (Exception e) {
            log.error("获取学生课表时发生错误: studentId={}, termCode={}, 错误信息: {}", studentId, termCode, e.getMessage(), e);
            return Map.of("error", "服务器内部错误", "schedules", Collections.emptyList());
        }
    }

    @Override
    public List<Schedule> getStudentScheduleListWithDetails(Long userId, String termInfo) {
        if (userId == null || !StringUtils.hasText(termInfo)) {
            log.warn("getStudentScheduleListWithDetails 调用参数无效: userId={}, termInfo={}", userId, termInfo);
            return Collections.emptyList();
        }
        log.info("为用户 {} 获取学期 {} 的详细课表列表", userId, termInfo);

        // 获取原始课表数据
        List<Schedule> schedules = scheduleDao.findByUserIdAndTermWithDetails(userId, termInfo);

        // 日志记录获取到的记录
        if (schedules != null && !schedules.isEmpty()) {
            log.info("成功获取学生课表记录 {} 条", schedules.size());
        } else {
            log.info("学生 {} 在学期 {} 没有课表记录", userId, termInfo);
            return Collections.emptyList();
        }

        return schedules;
    }

    @Override
    public Map<String, Object> checkScheduleConflict(Schedule schedule) {
        Map<String, Object> result = new HashMap<>();
        boolean conflict = false;
        try {
            conflict = isTimeConflict(schedule);
            result.put("conflict", conflict);
            result.put("message", conflict ? "存在时间冲突" : "无时间冲突");
        } catch (IllegalArgumentException e) {
            result.put("conflict", true);
            result.put("message", "检查冲突失败: " + e.getMessage());
            log.warn("检查冲突时参数错误: {}", e.getMessage());
        } catch (Exception e) {
            result.put("conflict", true);
            result.put("message", "检查冲突时发生未知错误");
            log.error("检查冲突时发生未知错误 for schedule: {}", schedule, e);
        }
        return result;
    }

    @Override
    @Deprecated
    public List<Schedule> getSchedulesByUserId(Long userId) {
        log.warn("调用了过时的 getSchedulesByUserId 方法，未按学期过滤，可能返回过多数据。 User ID: {}", userId);
        if (userId == null) return Collections.emptyList();
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
            return Collections.emptyList();
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
                log.warn("分页查询课表时，无效的 courseId 格式: {}", params.get("courseId"));
            }
        }
        if (params.containsKey("teacherId") && params.get("teacherId") != null) {
            try {
                Long teacherId = Long.parseLong(params.get("teacherId").toString());
                queryWrapper.eq("teacher_id", teacherId);
            } catch (NumberFormatException e) {
                log.warn("分页查询课表时，无效的 teacherId 格式: {}", params.get("teacherId"));
            }
        }
        if (params.containsKey("classroomId") && params.get("classroomId") != null) {
            try {
                Long classroomId = Long.parseLong(params.get("classroomId").toString());
                queryWrapper.eq("classroom_id", classroomId);
            } catch (NumberFormatException e) {
                log.warn("分页查询课表时，无效的 classroomId 格式: {}", params.get("classroomId"));
            }
        }
        queryWrapper.orderByAsc("day_of_week", "start_time");

        return this.page(pageRequest, queryWrapper);
    }

    private boolean isTimeConflict(Schedule schedule) {
        if (schedule == null || schedule.getClassroomId() == null || schedule.getTeacherId() == null ||
                schedule.getDayOfWeek() == null || schedule.getStartTime() == null || schedule.getEndTime() == null ||
                !StringUtils.hasText(schedule.getTermInfo()) || schedule.getStartWeek() == null || schedule.getEndWeek() == null) {
            log.error("内部检查时间冲突时，必要参数不足: {}", schedule);
            return false;
        }
        if (schedule.getStartTime().after(schedule.getEndTime())) {
            log.error("内部检查时间冲突时，开始时间晚于结束时间: {}", schedule);
            return true;
        }
        if (schedule.getDayOfWeek() < 1 || schedule.getDayOfWeek() > 7) {
            log.error("内部检查时间冲突时，无效的星期值: {}", schedule.getDayOfWeek());
            return true;
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
        if (classroomConflicts != null && classroomConflicts > 0) return true;

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
        return teacherConflicts != null && teacherConflicts > 0;
    }

    private Map<String, Object> buildWeeklyScheduleMap(List<Schedule> schedules, Long primaryId, String termCode, String type) {
        Map<String, Object> weeklySchedule = new HashMap<>();
        weeklySchedule.put("type", type);
        weeklySchedule.put("id", primaryId);
        weeklySchedule.put("termInfo", termCode);

        // 数据检查与日志记录
        if (schedules == null) {
            log.warn("构建课表时收到了null的schedules列表");
            schedules = Collections.emptyList();
        } else if (schedules.isEmpty()) {
            log.info("构建课表时schedules列表为空，可能是该{}在指定学期没有课程", type);
        } else {
            log.info("开始构建课表，数据条目数: {}, 第一条记录ID: {}",
                    schedules.size(),
                    schedules.get(0) != null ? schedules.get(0).getId() : "null");
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        List<Map<String, Object>> processedSchedules = new ArrayList<>();

        for (Schedule schedule : schedules) {
            try {
                if (schedule == null) {
                    log.warn("发现空的课表记录");
                    continue;
                }

                log.debug("处理课表记录: ID={}, 课程={}, 教师={}, 星期={}, 开始时间={}, 结束时间={}",
                        schedule.getId(),
                        schedule.getCourseName(),
                        schedule.getTeacherName(),
                        schedule.getDayOfWeek(),
                        schedule.getStartTime() != null ? timeFormat.format(schedule.getStartTime()) : "null",
                        schedule.getEndTime() != null ? timeFormat.format(schedule.getEndTime()) : "null");

                Map<String, Object> scheduleMap = convertScheduleToMap(schedule, timeFormat);
                if (scheduleMap != null && !scheduleMap.isEmpty()) {
                    processedSchedules.add(scheduleMap);
                    log.debug("成功处理并添加课表记录: {}", scheduleMap);
                } else {
                    log.warn("课表记录转换结果为空或null: ID={}", schedule.getId());
                }
            } catch (Exception e) {
                log.error("处理排课记录时出错: scheduleId={}, error={}",
                        schedule != null ? schedule.getId() : "null",
                        e.getMessage(), e);
                // 继续处理下一条记录
            }
        }

        // 按星期和时间槽排序，保证在周视图中显示的顺序正确
        processedSchedules.sort(Comparator.comparingInt((Map<String, Object> map) -> (Integer) map.getOrDefault("weekday", 0))
                .thenComparingInt(map -> (Integer) map.getOrDefault("startSlot", 0)));

        weeklySchedule.put("schedules", processedSchedules);

        // 添加原始课表列表，不含额外处理，用于列表视图展示
        weeklySchedule.put("scheduleList", processedSchedules);

        // 添加总课程数
        weeklySchedule.put("total", processedSchedules.size());

        log.info("为类型={}, ID={}, 学期={} 构建了周课表 Map。处理成功 {} 条排课记录。",
                type, primaryId, termCode, processedSchedules.size());

        return weeklySchedule;
    }

    private Map<String, Object> convertScheduleToMap(Schedule schedule, SimpleDateFormat timeFormat) {
        if (schedule == null) {
            log.warn("尝试转换空的排课记录");
            return null;
        }

        try {
            Map<String, Object> map = new HashMap<>();

            // 添加ID字段，重要性最高，单独处理
            map.put("id", schedule.getId());

            // 添加基本课表字段
            if (schedule.getCourseId() != null) map.put("courseId", schedule.getCourseId());
            if (schedule.getTeacherId() != null) map.put("teacherId", schedule.getTeacherId());
            if (schedule.getClassroomId() != null) map.put("classroomId", schedule.getClassroomId());

            // 添加学期信息和状态
            if (schedule.getTermInfo() != null) map.put("termInfo", schedule.getTermInfo());
            map.put("status", schedule.getStatus() != null ? schedule.getStatus() : "1");

            // 添加周次信息
            if (schedule.getStartWeek() != null) map.put("startWeek", schedule.getStartWeek());
            if (schedule.getEndWeek() != null) map.put("endWeek", schedule.getEndWeek());

            // 课程名称可能为null，添加安全检查
            map.put("courseName", schedule.getCourseName() != null ? schedule.getCourseName() : "未命名课程");

            // 添加教师名称
            if (schedule.getTeacherName() != null) {
                map.put("teacherName", schedule.getTeacherName());
            }

            // 添加教室名称
            if (schedule.getClassroomName() != null) {
                map.put("classroomName", schedule.getClassroomName());
            }

            // 地点信息格式化
            String location = "";
            if (schedule.getClassroomName() != null) {
                if (schedule.getBuilding() != null && !schedule.getBuilding().isEmpty()) {
                    location = schedule.getBuilding() + " " + schedule.getClassroomName();
                } else {
                    location = schedule.getClassroomName();
                }
            }
            map.put("location", location);

            // 星期几 (前端需要数字)
            Integer weekday = schedule.getDayOfWeek();
            if (weekday == null || weekday < 1 || weekday > 7) {
                log.warn("排课记录 {} 的星期几值无效: {}", schedule.getId(), weekday);
                weekday = 1; // 默认星期一
            }
            map.put("weekday", weekday);
            map.put("dayOfWeek", formatWeekday(weekday)); // 星期几文本格式

            // 上课时间文本格式 (例如 "08:00-09:40")
            String timeText = "";
            if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
                try {
                    timeText = timeFormat.format(schedule.getStartTime()) + "-" + timeFormat.format(schedule.getEndTime());
                } catch (Exception e) {
                    log.warn("排课记录 {} 的时间格式化失败: {}", schedule.getId(), e.getMessage());
                }
            }
            map.put("timeText", timeText);

            // 将时间转为LocalTime用于映射时间槽，添加更多安全检查
            LocalTime startLocalTime = null;
            LocalTime endLocalTime = null;
            
            if (schedule.getStartTime() != null) {
                try {
                    startLocalTime = schedule.getStartTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime();
                    map.put("startTime", timeFormat.format(schedule.getStartTime())); // 添加原始开始时间
                } catch (Exception e) {
                    log.warn("排课记录 {} 的开始时间转换失败: {}", schedule.getId(), e.getMessage());
                }
            }
            
            if (schedule.getEndTime() != null) {
                try {
                    endLocalTime = schedule.getEndTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime();
                    map.put("endTime", timeFormat.format(schedule.getEndTime())); // 添加原始结束时间
                } catch (Exception e) {
                    log.warn("排课记录 {} 的结束时间转换失败: {}", schedule.getId(), e.getMessage());
                }
            }

            // 计算开始和结束时间槽 (1-10)
            int startSlot = 1; // 默认第一节
            int endSlot = 2;   // 默认第二节 (至少跨1节课)

            if (startLocalTime != null) {
                startSlot = mapTimeToSlot(startLocalTime);
            }

            if (endLocalTime != null) {
                // 确保结束时间槽至少比开始时间槽大1
                endSlot = Math.max(startSlot + 1, mapEndTimeToSlot(endLocalTime, startSlot));
            }

            map.put("startSlot", startSlot);
            map.put("endSlot", endSlot);
            map.put("slotCount", endSlot - startSlot + 1); // 跨多少节课

            // 添加教室和上课地点信息，供列表视图使用
            map.put("classroom", location); // 冗余字段，与location保持一致，便于前端显示

            // 添加原始时间信息的文本描述，适用于列表视图
            map.put("time", String.format("%s %s", formatWeekday(weekday), timeText));

            log.debug("成功转换排课记录ID={}, 课程={}, 时间槽: {} - {}",
                    schedule.getId(), schedule.getCourseName(), startSlot, endSlot);
            return map;
        } catch (Exception e) {
            log.error("转换排课记录时出错: scheduleId={}, error={}",
                    schedule.getId() != null ? schedule.getId() : "null", e.getMessage(), e);
            return Map.of("error", "数据格式转换错误");
        }
    }

    private String formatWeekday(Integer dayOfWeek) {
        if (dayOfWeek == null) return "未知";
        return switch (dayOfWeek) {
            case 1 -> "星期一";
            case 2 -> "星期二";
            case 3 -> "星期三";
            case 4 -> "星期四";
            case 5 -> "星期五";
            case 6 -> "星期六";
            case 7 -> "星期日";
            default -> "星期 " + dayOfWeek;
        };
    }

    private int mapTimeToSlot(LocalTime time) {
        if (time == null) return 1;

        for (int i = 0; i < TIME_SLOT_BOUNDARIES.size(); i++) {
            LocalTime slotStart = TIME_SLOT_BOUNDARIES.get(i).get("start");
            LocalTime slotEnd = TIME_SLOT_BOUNDARIES.get(i).get("end");

            if (!time.isBefore(slotStart.minusMinutes(5)) && !time.isAfter(slotEnd)) {
                return i + 1;
            }
            if (i < TIME_SLOT_BOUNDARIES.size() - 1) {
                LocalTime nextSlotStart = TIME_SLOT_BOUNDARIES.get(i + 1).get("start");
                if (time.isAfter(slotEnd) && time.isBefore(nextSlotStart)) {
                    if (time.plusMinutes(10).isAfter(nextSlotStart)) {
                        return i + 2;
                    } else {
                        return i + 1;
                    }
                }
            }
        }
        if (time.isAfter(TIME_SLOT_BOUNDARIES.get(TIME_SLOT_BOUNDARIES.size() - 1).get("end"))) {
            return TIME_SLOT_BOUNDARIES.size();
        }
        if (time.isBefore(TIME_SLOT_BOUNDARIES.get(0).get("start").minusMinutes(5))) {
            return 1;
        }

        log.warn("无法将开始时间 {} 映射到明确的节次 (1-10)，默认为 1", time);
        return 1;
    }

    private int mapEndTimeToSlot(LocalTime time, int startSlot) {
        if (time == null) return Math.max(startSlot, 1);

        for (int i = TIME_SLOT_BOUNDARIES.size() - 1; i >= 0; i--) {
            LocalTime slotStart = TIME_SLOT_BOUNDARIES.get(i).get("start");
            LocalTime slotEnd = TIME_SLOT_BOUNDARIES.get(i).get("end");

            if (!time.isBefore(slotStart) && !time.isAfter(slotEnd.plusMinutes(5))) {
                return Math.max(startSlot, i + 1);
            }

            if (i > 0) {
                LocalTime prevSlotEnd = TIME_SLOT_BOUNDARIES.get(i - 1).get("end");
                if (time.isAfter(prevSlotEnd) && time.isBefore(slotStart)) {
                    if (time.minusMinutes(10).isBefore(prevSlotEnd)) {
                        return Math.max(startSlot, i);
                    } else {
                        return Math.max(startSlot, i + 1);
                    }
                }
            }
        }
        if (time.isAfter(TIME_SLOT_BOUNDARIES.get(TIME_SLOT_BOUNDARIES.size() - 1).get("end").plusMinutes(5))) {
            return Math.max(startSlot, TIME_SLOT_BOUNDARIES.size());
        }
        if (time.isBefore(TIME_SLOT_BOUNDARIES.get(0).get("start"))) {
            return Math.max(startSlot, 1);
        }

        log.warn("无法将结束时间 {} 映射到明确的节次 (1-10)，默认为开始节次 {}", time, startSlot);
        return Math.max(startSlot, 1);
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
        int deletedCount = scheduleDao.delete(deleteWrapper);
        log.info("根据课程 ID 列表删除了 {} 条排课记录。 Course IDs: {}", deletedCount, courseIds);
    }

    public boolean isClassroomAvailable(Long classroomId, Integer weekday, Date startTime, Date endTime, Long termId, Long excludeScheduleId) {
        log.warn("isClassroomAvailable 方法未使用 termInfo 进行过滤，可能不准确。请使用 checkTimeConflict 方法替代。");
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("classroom_id", classroomId)
                .eq("day_of_week", weekday)
                .and(qw -> qw
                        .lt("start_time", endTime)
                        .gt("end_time", startTime)
                );
        if (excludeScheduleId != null) {
            queryWrapper.ne("id", excludeScheduleId);
        }
        return scheduleDao.selectCount(queryWrapper) == 0;
    }

    public boolean isTeacherAvailable(Long teacherId, Integer weekday, Date startTime, Date endTime, Long termId, Long excludeScheduleId) {
        log.warn("isTeacherAvailable 方法未使用 termInfo 进行过滤，可能不准确。请使用 checkTimeConflict 方法替代。");
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", teacherId)
                .eq("day_of_week", weekday)
                .and(qw -> qw
                        .lt("start_time", endTime)
                        .gt("end_time", startTime)
                );
        if (excludeScheduleId != null) {
            queryWrapper.ne("id", excludeScheduleId);
        }
        return scheduleDao.selectCount(queryWrapper) == 0;
    }

    public List<Schedule> getSchedulesByClassIdAndTerm(Long classId, String termInfo) {
        log.warn("getSchedulesByClassIdAndTerm 方法依赖 Schedule 表中的 class_id 字段，并且返回的是基础信息列表。");
        if (classId == null || !StringUtils.hasText(termInfo)) {
            return Collections.emptyList();
        }
        QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId);
        queryWrapper.eq("term_info", termInfo);
        return scheduleDao.selectList(queryWrapper);
    }
}