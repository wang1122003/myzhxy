package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ScheduleDao;
import com.campus.dao.UserDao;
import com.campus.entity.Schedule;
import com.campus.entity.User;
import com.campus.exception.CustomException;
import com.campus.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Arrays;

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

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleDao.findById(id);
    }

    @Override
    public List<Schedule> getSchedulesByCourseId(Long courseId) {
        return scheduleDao.findByCourseId(courseId);
    }

    @Override
    public List<Schedule> getSchedulesByTeacherId(Long teacherId) {
        return scheduleDao.findByTeacherId(teacherId);
    }

    @Override
    public List<Schedule> getSchedulesByClassroomId(Long classroomId) {
        return scheduleDao.findByClassroomId(classroomId);
    }

    @Override
    public List<Schedule> getSchedulesByTeacherIdAndTerm(Long teacherId, String termInfo) {
        return scheduleDao.findByTeacherIdAndTerm(teacherId, termInfo);
    }

    @Override
    public List<Schedule> getSchedulesByClassroomIdAndTerm(Long classroomId, String termInfo) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getClassroomId, classroomId)
                .eq(Schedule::getTermInfo, termInfo);
        queryWrapper.orderByAsc(Schedule::getDayOfWeek).orderByAsc(Schedule::getStartTime);
        return list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean addSchedule(Schedule schedule) {
        schedule.setStatus("Active");
        schedule.setCreateTime(new Date());
        schedule.setUpdateTime(new Date());
        return save(schedule);
    }

    @Override
    @Transactional
    public boolean updateSchedule(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        if (schedule.getStatus() == null) {
            schedule.setStatus("Active");
        }
        return updateById(schedule);
    }

    @Override
    @Transactional
    public boolean deleteSchedule(Long id) {
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteSchedules(Long[] ids) {
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    public boolean checkTimeConflict(Schedule schedule) {
        return isTimeConflict(schedule);
    }

    @Override
    public List<Schedule> getSchedulesByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        Page<Schedule> page = new Page<>(pageNum, pageSize);
        return page(page).getRecords();
    }
    
    @Override
    public int getScheduleCount() {
        return scheduleDao.getCount();
    }
    
    @Override
    public int getTeacherScheduleCount(Long teacherId) {
        return scheduleDao.getTeacherScheduleCount(teacherId);
    }
    
    @Override
    public int getClassroomScheduleCount(Long classroomId) {
        return scheduleDao.getClassroomScheduleCount(classroomId);
    }
    
    @Override
    public int getCourseScheduleCount(Long courseId) {
        return scheduleDao.getCourseScheduleCount(courseId);
    }
    
    @Override
    public int getTermScheduleCount(String termInfo) {
        return scheduleDao.getTermScheduleCount(termInfo);
    }
    
    @Override
    public List<Map<String, Object>> getTeacherScheduleTimeDistribution(Long teacherId, String termInfo) {
        return scheduleDao.getTeacherScheduleTimeDistribution(teacherId, termInfo);
    }
    
    @Override
    public List<Map<String, Object>> getClassroomScheduleTimeDistribution(Long classroomId, String termInfo) {
        return scheduleDao.getClassroomScheduleTimeDistribution(classroomId, termInfo);
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
    public Map<String, Object> getClassroomWeeklySchedule(Long classroomId, String termInfo) {
        List<Schedule> schedules = getSchedulesByClassroomIdAndTerm(classroomId, termInfo);
        Map<String, Object> weeklySchedule = buildWeeklyScheduleMap(schedules, classroomId, termInfo, "classroom");
        return weeklySchedule;
    }
    
    @Override
    public Map<String, Object> getTeacherWeeklySchedule(Long teacherId, String termInfo) {
        List<Schedule> schedules = getSchedulesByTeacherIdAndTerm(teacherId, termInfo);
        Map<String, Object> weeklySchedule = buildWeeklyScheduleMap(schedules, teacherId, termInfo, "teacher");
        return weeklySchedule;
    }
    
    @Override
    public Map<String, Object> getStudentWeeklySchedule(Long studentId, String termInfo) {
        User student = userDao.selectById(studentId);

        Long classId = null;
        if (student != null && "Student".equals(student.getUserType())) {
            classId = getClassIdForStudent(studentId);
        }

        if (classId == null) {
            log.warn("无法找到学生或学生未分配班级, studentId: {}", studentId);
            Map<String, Object> emptySchedule = new HashMap<>();
            emptySchedule.put("schedules", new ArrayList<>());
            emptySchedule.put("termInfo", termInfo);
            emptySchedule.put("type", "student");
            emptySchedule.put("studentId", studentId);
            emptySchedule.put("classId", null);
            return emptySchedule;
        }

        List<Schedule> schedules = getSchedulesByUserIdAndTerm(studentId, termInfo);
        Map<String, Object> weeklySchedule = buildWeeklyScheduleMap(schedules, studentId, termInfo, "student");
        weeklySchedule.put("classId", classId);
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
        return scheduleDao.findByUserId(userId);
    }

    @Override
    public boolean isCourseScheduled(Long courseId) {
        return scheduleDao.countByCourseId(courseId) > 0;
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

        if (params.containsKey("termInfo") && params.get("termInfo") != null) {
            queryWrapper.eq("term_info", params.get("termInfo"));
        }
        if (params.containsKey("courseId") && params.get("courseId") != null) {
            queryWrapper.eq("course_id", params.get("courseId"));
        }
        if (params.containsKey("teacherId") && params.get("teacherId") != null) {
            queryWrapper.eq("teacher_id", params.get("teacherId"));
        }

        return this.page(pageRequest, queryWrapper);
    }

    private boolean isTimeConflict(Schedule schedule) {
        log.warn("isTimeConflict check is currently incomplete due to DAO changes. Returning false.");
        return false;
    }

    private Time convertToSqlTime(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new Time(date.getTime());
    }

    private Map<String, Object> buildWeeklyScheduleMap(List<Schedule> schedules, Long primaryId, String termInfo, String type) {
        Map<String, Object> weeklySchedule = new HashMap<>();
        weeklySchedule.put("type", type);
        weeklySchedule.put("id", primaryId);
        weeklySchedule.put("termInfo", termInfo);

        Map<String, List<Schedule>> scheduleByDay = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            scheduleByDay.put(String.valueOf(i), new ArrayList<>());
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        for (Schedule schedule : schedules) {
            String dayValue = schedule.getDayOfWeek();
            String dayKey = null;
            if (dayValue != null) {
                dayKey = convertDayNameToNumberString(dayValue); 
            }

            if (dayKey != null && scheduleByDay.containsKey(dayKey)) {
                scheduleByDay.get(dayKey).add(schedule);
            }
        }

        for (List<Schedule> dailySchedules : scheduleByDay.values()) {
            dailySchedules.sort(Comparator.comparing(Schedule::getStartTime));
        }

        weeklySchedule.put("schedule", scheduleByDay);
        return weeklySchedule;
    }

    private String convertDayNameToNumberString(String dayValue) {
        if (dayValue == null) return null;
        String lowerDay = dayValue.trim().toLowerCase();
        switch (lowerDay) {
            case "monday": case "1": return "1";
            case "tuesday": case "2": return "2";
            case "wednesday": case "3": return "3";
            case "thursday": case "4": return "4";
            case "friday": case "5": return "5";
            case "saturday": case "6": return "6";
            case "sunday": case "7": return "7";
            default:
                return null;
        }
    }

    private Long getClassIdForStudent(Long studentId) {
        return 1L;
    }

    private List<Schedule> getSchedulesByUserIdAndTerm(Long userId, String termInfo) {
        log.warn("getSchedulesByUserIdAndTerm is not fully implemented.");
        return new ArrayList<>();
    }
}