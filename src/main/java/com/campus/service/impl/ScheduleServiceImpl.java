package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ScheduleDao;
import com.campus.entity.Schedule;
import com.campus.exception.CustomException;
import com.campus.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 课表服务实现类
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleDao, Schedule> implements ScheduleService {

    @Autowired
    private ScheduleDao scheduleDao;

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleDao.findById(id);
    }

    @Override
    public List<Schedule> getAllSchedules() {
        return scheduleDao.findAll();
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
    public List<Schedule> getSchedulesByClassId(Long classId) {
        return scheduleDao.findByClassId(classId);
    }

    @Override
    public List<Schedule> getSchedulesByTermId(Long termId) {
        return scheduleDao.findByTermId(termId);
    }

    @Override
    public List<Schedule> getSchedulesByTeacherIdAndTermId(Long teacherId, Long termId) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getTeacherId, teacherId)
                .eq(Schedule::getTermId, termId)
                .eq(Schedule::getStatus, 1);
        queryWrapper.orderByAsc(Schedule::getWeekDay).orderByAsc(Schedule::getStartTime);
        return list(queryWrapper);
    }

    @Override
    public List<Schedule> getSchedulesByClassIdAndTermId(Long classId, Long termId) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getClassId, classId)
                .eq(Schedule::getTermId, termId)
                .eq(Schedule::getStatus, 1);
        queryWrapper.orderByAsc(Schedule::getWeekDay).orderByAsc(Schedule::getStartTime);
        return list(queryWrapper);
    }

    @Override
    public List<Schedule> getSchedulesByClassroomIdAndTermId(Long classroomId, Long termId) {
        return scheduleDao.findByClassroomIdAndTermId(classroomId, termId);
    }

    @Override
    @Transactional
    public boolean addSchedule(Schedule schedule) {
        if (isTimeConflict(schedule)) {
            throw new CustomException("排课时间冲突: 教室或时间段已被占用");
        }
        schedule.setStatus(1);
        schedule.setCreateTime(new Date());
        schedule.setUpdateTime(new Date());
        return save(schedule);
    }

    @Override
    @Transactional
    public boolean updateSchedule(Schedule schedule) {
        if (isTimeConflict(schedule)) {
            throw new CustomException("排课时间冲突: 教室或时间段已被占用");
        }
        schedule.setUpdateTime(new Date());
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
        return scheduleDao.batchDelete(ids) > 0;
    }

    @Override
    public boolean checkTimeConflict(Schedule schedule) {
        return isTimeConflict(schedule);
    }

    @Override
    public List<Schedule> getSchedulesByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return scheduleDao.findByPage(offset, pageSize);
    }
    
    @Override
    public int getScheduleCount() {
        return scheduleDao.getCount();
    }
    
    public List<Schedule> getSchedulesByTimeSlot(int weekDay, int timeSlot) {
        return scheduleDao.findByTimeSlot(weekDay, timeSlot);
    }
    
    @Override
    public int getTeacherScheduleCount(Long teacherId) {
        return scheduleDao.getTeacherScheduleCount(teacherId);
    }
    
    @Override
    public int getClassScheduleCount(Long classId) {
        return scheduleDao.getClassScheduleCount(classId);
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
    public int getTermScheduleCount(Long termId) {
        return scheduleDao.getTermScheduleCount(termId);
    }
    
    @Override
    public List<Map<String, Object>> getTeacherScheduleTimeDistribution(Long teacherId) {
        return scheduleDao.getTeacherScheduleTimeDistribution(teacherId);
    }
    
    @Override
    public List<Map<String, Object>> getClassScheduleTimeDistribution(Long classId) {
        return scheduleDao.getClassScheduleTimeDistribution(classId);
    }
    
    @Override
    public List<Map<String, Object>> getClassroomScheduleTimeDistribution(Long classroomId) {
        return scheduleDao.getClassroomScheduleTimeDistribution(classroomId);
    }
    
    @Transactional
    public boolean updateScheduleStatus(Long id, Integer status) {
        return scheduleDao.updateStatus(id, status) > 0;
    }

    @Override
    public Map<String, Object> getClassroomWeeklySchedule(Long classroomId, Long termId) {
        List<Schedule> schedules = scheduleDao.findByClassroomIdAndTermId(classroomId, termId);
        Map<String, Object> weeklySchedule = buildWeeklyScheduleMap(schedules, classroomId, termId, "classroom");
        return weeklySchedule;
    }
    
    @Override
    public Map<String, Object> getTeacherWeeklySchedule(Long teacherId, Long termId) {
        List<Schedule> schedules = scheduleDao.findByTeacherIdAndTermId(teacherId, termId);
        Map<String, Object> weeklySchedule = buildWeeklyScheduleMap(schedules, teacherId, termId, "teacher");
        return weeklySchedule;
    }
    
    @Override
    public Map<String, Object> getStudentWeeklySchedule(Long studentId, Long termId) {
        Long classId = 1L;
        if (classId == null) return new HashMap<>();

        List<Schedule> schedules = scheduleDao.findByClassIdAndTermId(classId, termId);
        Map<String, Object> weeklySchedule = buildWeeklyScheduleMap(schedules, studentId, termId, "student");
        weeklySchedule.put("classId", classId);
        return weeklySchedule;
    }
    
    @Override
    public Map<String, Object> checkScheduleConflict(Schedule schedule) {
        Map<String, Object> result = new HashMap<>();
        result.put("conflict", false);

        // Check only classroom conflict for now
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getClassroomId, schedule.getClassroomId())
                  .eq(Schedule::getWeekDay, schedule.getWeekDay())
                  .le(Schedule::getStartTime, schedule.getEndTime())
                  .ge(Schedule::getEndTime, schedule.getStartTime());
        
        List<Schedule> conflictSchedules = scheduleDao.selectList(queryWrapper);
        
        if (!conflictSchedules.isEmpty()) {
            result.put("conflict", true);
            result.put("type", "classroom");
            result.put("message", "该时间段教室已被占用");
            return result;
        }
        
        return result;
    }
    
    @Override
    public List<Schedule> getSchedulesByStudentId(Long studentId) {
        Long classId = 1L;
        
        return scheduleDao.findByClassId(classId);
    }

    @Override
    public boolean isCourseScheduled(Long courseId) {
        return scheduleDao.countByCourseId(courseId) > 0;
    }

    @Override
    public List<Long> findScheduledCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        return scheduleDao.findScheduledCourseIds(courseIds);
    }

    @Override
    public List<Schedule> getScheduleByClassIdAndTermId(Long classId, Long termId) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getClassId, classId)
                .eq(Schedule::getTermId, termId)
                .eq(Schedule::getStatus, 1);
        queryWrapper.orderByAsc(Schedule::getWeekDay).orderByAsc(Schedule::getStartTime);
        return list(queryWrapper);
    }

    @Override
    public List<Schedule> getScheduleByTeacherIdAndTermId(Long teacherId, Long termId) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getTeacherId, teacherId)
                .eq(Schedule::getTermId, termId)
                .eq(Schedule::getStatus, 1);
        queryWrapper.orderByAsc(Schedule::getWeekDay).orderByAsc(Schedule::getStartTime);
        return list(queryWrapper);
    }

    @Override
    public List<Schedule> getScheduleByCourseIdAndTermId(Long courseId, Long termId) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getCourseId, courseId)
                .eq(Schedule::getTermId, termId)
                .eq(Schedule::getStatus, 1);
        return list(queryWrapper);
    }

    @Override
    public IPage<Schedule> getSchedulesPage(Map<String, Object> params, int page, int size) {
        // 1. 创建分页对象
        Page<Schedule> pageRequest = new Page<>(page, size);

        // 2. 构建查询条件
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();

        // 添加学期条件 (通常是必选)
        if (params.containsKey("termId") && params.get("termId") != null) {
            queryWrapper.eq(Schedule::getTermId, params.get("termId"));
        } else {
            // 如果学期是必选的，可以在这里抛出异常或返回空分页
            // throw new CustomException("查询课表必须指定学期");
            return new Page<>(page, size, 0); // 返回空结果
        }

        // ---- 暂时注释掉模糊查询 ----
        // // 添加课程名称模糊查询
        // if (params.containsKey("courseName") && params.get("courseName") != null) {
        //     queryWrapper.apply("course_id IN (SELECT id FROM course WHERE course_name LIKE {0})", "%" + params.get("courseName") + "%");
        // }
        // 
        // // 添加教师名称模糊查询 
        // if (params.containsKey("teacherName") && params.get("teacherName") != null) {
        //      queryWrapper.apply("teacher_id IN (SELECT t.id FROM teacher t JOIN user u ON t.user_id = u.id WHERE u.real_name LIKE {0} OR u.username LIKE {0})", "%" + params.get("teacherName") + "%"); // 修正子查询
        // }
        // 
        // // 添加班级名称模糊查询 
        // if (params.containsKey("className") && params.get("className") != null) {
        //      queryWrapper.apply("class_id IN (SELECT id FROM clazz WHERE name LIKE {0})", "%" + params.get("className") + "%"); // 修正表名 class -> clazz
        // }
        // 
        // // 添加教室 ID 查询
        // if (params.containsKey("classroomId") && params.get("classroomId") != null) {
        //     queryWrapper.eq(Schedule::getClassroomId, params.get("classroomId"));
        // }
        // ---- 结束注释 ----

        // 添加排序 (按星期和开始时间)
        queryWrapper.orderByAsc(Schedule::getWeekDay).orderByAsc(Schedule::getStartTime);

        // 3. 执行分页查询
        // IPage<Schedule> schedulePage = this.page(pageRequest, queryWrapper); // 直接用 MybatisPlus 的 page 方法
        // 或者调用自定义的 Dao 方法（如果需要复杂 JOIN）
        IPage<Schedule> schedulePage = baseMapper.selectPage(pageRequest, queryWrapper); // 使用 baseMapper

        // 4. 可能需要填充非数据库字段 (如 courseName, teacherName, className)
        // 这通常在 Dao/Mapper XML 或 Service 层完成
        // 如果 baseMapper.selectPage 返回的 Schedule 对象没有这些字段，需要额外处理
        // 例如：遍历 schedulePage.getRecords() 并根据 ID 查询填充
        // (为了简化，这里暂时省略填充步骤，假设前端只显示 Schedule 表中的字段，或者 Mapper XML 中已处理 JOIN)

        return schedulePage;
    }

    private boolean isTimeConflict(Schedule schedule) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getClassroomId, schedule.getClassroomId())
                .eq(Schedule::getWeekDay, schedule.getWeekDay())
                .eq(Schedule::getTermId, schedule.getTermId())
                .eq(Schedule::getStatus, 1)
                .ne(schedule.getId() != null, Schedule::getId, schedule.getId());

        Time scheduleStartTime = convertToSqlTime(schedule.getStartTime());
        Time scheduleEndTime = convertToSqlTime(schedule.getEndTime());
        if (scheduleStartTime == null || scheduleEndTime == null) {
            return false;
        }

        queryWrapper.lt(Schedule::getStartTime, scheduleEndTime)
                .gt(Schedule::getEndTime, scheduleStartTime);

        queryWrapper.le(Schedule::getStartWeek, schedule.getEndWeek())
                .ge(Schedule::getEndWeek, schedule.getStartWeek());

        return count(queryWrapper) > 0;
    }

    private Time convertToSqlTime(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new Time(date.getTime());
    }

    private Map<String, Object> buildWeeklyScheduleMap(List<Schedule> schedules, Long primaryId, Long termId, String type) {
        Map<String, Object> weeklySchedule = new HashMap<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        for (int i = 1; i <= 7; i++) {
            weeklySchedule.put("day" + i, new HashMap<String, Object>());
        }

        for (Schedule schedule : schedules) {
            int weekDay = schedule.getWeekDay();
            Date startTime = schedule.getStartTime();
            Date endTime = schedule.getEndTime();

            @SuppressWarnings("unchecked")
            Map<String, Object> daySchedule = (Map<String, Object>) weeklySchedule.get("day" + weekDay);

            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("scheduleId", schedule.getId());
            courseInfo.put("courseId", schedule.getCourseId());
            courseInfo.put("weekDay", weekDay);
            courseInfo.put("startTime", startTime != null ? timeFormat.format(startTime) : "");
            courseInfo.put("endTime", endTime != null ? timeFormat.format(endTime) : "");
            courseInfo.put("startWeek", schedule.getStartWeek());
            courseInfo.put("endWeek", schedule.getEndWeek());
            courseInfo.put("location", "教室ID:" + schedule.getClassroomId());

            String timeSlotKey = (startTime != null ? timeFormat.format(startTime) : "N/A") + "-" +
                    (endTime != null ? timeFormat.format(endTime) : "N/A");
            daySchedule.put(timeSlotKey, courseInfo);
        }

        weeklySchedule.put(type + "Id", primaryId);
        weeklySchedule.put("termId", termId);

        return weeklySchedule;
    }
}