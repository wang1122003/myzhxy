package com.campus.service.impl;

import com.campus.dao.ScheduleDao;
import com.campus.entity.Schedule;
import com.campus.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课表服务实现类
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

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
        return scheduleDao.findByTeacherIdAndTermId(teacherId, termId);
    }

    @Override
    public List<Schedule> getSchedulesByClassIdAndTermId(Long classId, Long termId) {
        return scheduleDao.findByClassIdAndTermId(classId, termId);
    }

    @Override
    public List<Schedule> getSchedulesByClassroomIdAndTermId(Long classroomId, Long termId) {
        return scheduleDao.findByClassroomIdAndTermId(classroomId, termId);
    }

    @Override
    @Transactional
    public boolean addSchedule(Schedule schedule) {
        // 检查时间冲突
        if (checkTimeConflict(schedule)) {
            return false;
        }
        
        // 设置创建时间和更新时间
        Date now = new Date();
        schedule.setCreateTime(now);
        schedule.setUpdateTime(now);
        
        return scheduleDao.insert(schedule) > 0;
    }

    @Override
    @Transactional
    public boolean updateSchedule(Schedule schedule) {
        // 检查时间冲突
        if (checkTimeConflict(schedule)) {
            return false;
        }
        
        // 设置更新时间
        schedule.setUpdateTime(new Date());
        
        return scheduleDao.update(schedule) > 0;
    }

    @Override
    @Transactional
    public boolean deleteSchedule(Long id) {
        return scheduleDao.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteSchedules(Long[] ids) {
        return scheduleDao.batchDelete(ids) > 0;
    }

    @Override
    public boolean checkTimeConflict(Schedule schedule) {
        // 检查教师时间冲突
        if (checkTeacherTimeConflict(schedule.getTeacherId(), schedule.getWeekDay(), 
            schedule.getStartTime(), schedule.getEndTime())) {
            return true;
        }
        
        // 检查班级时间冲突
        if (checkClassTimeConflict(schedule.getClassId(), schedule.getWeekDay(), 
            schedule.getStartTime(), schedule.getEndTime())) {
            return true;
        }
        
        return false;
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
    
    @Override
    public List<Schedule> getSchedulesByTimeSlot(int weekDay, int timeSlot) {
        return scheduleDao.findByTimeSlot(weekDay, timeSlot);
    }
    
    @Override
    public boolean checkTeacherTimeConflict(Long teacherId, int weekDay, int startTime, int endTime) {
        return scheduleDao.checkTeacherTimeConflict(teacherId, weekDay, startTime, endTime);
    }
    
    @Override
    public boolean checkClassTimeConflict(Long classId, int weekDay, int startTime, int endTime) {
        return scheduleDao.checkClassTimeConflict(classId, weekDay, startTime, endTime);
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
    
    @Override
    @Transactional
    public boolean updateScheduleStatus(Long id, Integer status) {
        return scheduleDao.updateStatus(id, status) > 0;
    }

    @Override
    public Map<String, Object> getClassroomWeeklySchedule(Long classroomId, Long termId) {
        // 获取该教室在该学期的所有课表
        List<Schedule> schedules = scheduleDao.findByClassroomIdAndTermId(classroomId, termId);
        
        // 构建周课表数据
        Map<String, Object> weeklySchedule = new HashMap<>();
        
        // 初始化每天的课表
        for (int i = 1; i <= 7; i++) {
            weeklySchedule.put("day" + i, new HashMap<String, Object>());
        }
        
        // 填充课表数据
        for (Schedule schedule : schedules) {
            int weekDay = schedule.getWeekDay();
            int startTime = schedule.getStartTime();
            int endTime = schedule.getEndTime();
            
            // 获取当天的课表
            @SuppressWarnings("unchecked")
            Map<String, Object> daySchedule = (Map<String, Object>) weeklySchedule.get("day" + weekDay);
            
            // 构建课程信息
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("id", schedule.getId());
            courseInfo.put("courseId", schedule.getCourseId());
            courseInfo.put("courseName", schedule.getCourseName());
            courseInfo.put("teacherId", schedule.getTeacherId());
            courseInfo.put("teacherName", schedule.getTeacherName());
            courseInfo.put("classId", schedule.getClassId());
            courseInfo.put("className", schedule.getClassName());
            courseInfo.put("startTime", startTime);
            courseInfo.put("endTime", endTime);
            
            // 将课程信息添加到对应时间段
            String timeSlotKey = startTime + "-" + endTime;
            daySchedule.put(timeSlotKey, courseInfo);
        }
        
        // 添加教室信息
        weeklySchedule.put("classroomId", classroomId);
        // Assuming classroomDao is available to getClassroomById
        // Classroom classroom = classroomDao.getClassroomById(classroomId);
        // weeklySchedule.put("classroomName", classroom.getName());
        weeklySchedule.put("classroomName", "教室" + classroomId);
        
        // 添加学期信息
        weeklySchedule.put("termId", termId);
        // Assuming termDao is available to getTermById
        // Term term = termDao.getTermById(termId);
        // weeklySchedule.put("termName", term.getName());
        weeklySchedule.put("termName", "学期" + termId);
        
        return weeklySchedule;
    }
    
    @Override
    public Map<String, Object> getTeacherWeeklySchedule(Long teacherId, Long termId) {
        // 获取该教师在该学期的所有课表
        List<Schedule> schedules = scheduleDao.findByTeacherIdAndTermId(teacherId, termId);
        
        // 构建周课表数据
        Map<String, Object> weeklySchedule = new HashMap<>();
        
        // 初始化每天的课表
        for (int i = 1; i <= 7; i++) {
            weeklySchedule.put("day" + i, new HashMap<String, Object>());
        }
        
        // 填充课表数据
        for (Schedule schedule : schedules) {
            int weekDay = schedule.getWeekDay();
            int startTime = schedule.getStartTime();
            int endTime = schedule.getEndTime();
            
            // 获取当天的课表
            @SuppressWarnings("unchecked")
            Map<String, Object> daySchedule = (Map<String, Object>) weeklySchedule.get("day" + weekDay);
            
            // 构建课程信息
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("id", schedule.getId());
            courseInfo.put("courseId", schedule.getCourseId());
            courseInfo.put("courseName", schedule.getCourseName());
            courseInfo.put("classroomId", schedule.getClassroomId());
            courseInfo.put("classroomName", schedule.getClassroomName());
            courseInfo.put("classId", schedule.getClassId());
            courseInfo.put("className", schedule.getClassName());
            courseInfo.put("startTime", startTime);
            courseInfo.put("endTime", endTime);
            
            // 将课程信息添加到对应时间段
            String timeSlotKey = startTime + "-" + endTime;
            daySchedule.put(timeSlotKey, courseInfo);
        }
        
        // 添加教师信息
        weeklySchedule.put("teacherId", teacherId);
        // Assuming teacherDao is available to getTeacherById
        // Teacher teacher = teacherDao.getTeacherById(teacherId);
        // weeklySchedule.put("teacherName", teacher.getName());
        weeklySchedule.put("teacherName", "教师" + teacherId);
        
        // 添加学期信息
        weeklySchedule.put("termId", termId);
        // Assuming termDao is available to getTermById
        // Term term = termDao.getTermById(termId);
        // weeklySchedule.put("termName", term.getName());
        weeklySchedule.put("termName", "学期" + termId);
        
        return weeklySchedule;
    }
    
    @Override
    public Map<String, Object> getStudentWeeklySchedule(Long studentId, Long termId) {
        // 获取该学生的班级ID
        // Assuming studentDao is available to getStudentById
        // Student student = studentDao.getStudentById(studentId);
        // Long classId = student.getClassId();
        Long classId = 1L; // 临时使用固定值，实际应从数据库获取
        
        // 获取该班级在该学期的所有课表
        List<Schedule> schedules = scheduleDao.findByClassIdAndTermId(classId, termId);
        
        // 构建周课表数据
        Map<String, Object> weeklySchedule = new HashMap<>();
        
        // 初始化每天的课表
        for (int i = 1; i <= 7; i++) {
            weeklySchedule.put("day" + i, new HashMap<String, Object>());
        }
        
        // 填充课表数据
        for (Schedule schedule : schedules) {
            int weekDay = schedule.getWeekDay();
            int startTime = schedule.getStartTime();
            int endTime = schedule.getEndTime();
            
            // 获取当天的课表
            @SuppressWarnings("unchecked")
            Map<String, Object> daySchedule = (Map<String, Object>) weeklySchedule.get("day" + weekDay);
            
            // 构建课程信息
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("id", schedule.getId());
            courseInfo.put("courseId", schedule.getCourseId());
            courseInfo.put("courseName", schedule.getCourseName());
            courseInfo.put("teacherId", schedule.getTeacherId());
            courseInfo.put("teacherName", schedule.getTeacherName());
            courseInfo.put("classroomId", schedule.getClassroomId());
            courseInfo.put("classroomName", schedule.getClassroomName());
            courseInfo.put("startTime", startTime);
            courseInfo.put("endTime", endTime);
            
            // 将课程信息添加到对应时间段
            String timeSlotKey = startTime + "-" + endTime;
            daySchedule.put(timeSlotKey, courseInfo);
        }
        
        // 添加学生信息
        weeklySchedule.put("studentId", studentId);
        // Assuming studentDao is available to getStudentById
        // Student student = studentDao.getStudentById(studentId);
        // weeklySchedule.put("studentName", student.getName());
        weeklySchedule.put("studentName", "学生" + studentId);
        
        // 添加班级信息
        weeklySchedule.put("classId", classId);
        // Assuming classDao is available to getClassById
        // Class clazz = classDao.getClassById(classId);
        // weeklySchedule.put("className", clazz.getName());
        weeklySchedule.put("className", "班级" + classId);
        
        // 添加学期信息
        weeklySchedule.put("termId", termId);
        // Assuming termDao is available to getTermById
        // Term term = termDao.getTermById(termId);
        // weeklySchedule.put("termName", term.getName());
        weeklySchedule.put("termName", "学期" + termId);
        
        return weeklySchedule;
    }
    
    @Override
    public Map<String, Object> checkScheduleConflict(Schedule schedule) {
        Map<String, Object> result = new HashMap<>();
        result.put("conflict", false);
        
        // 检查教师时间冲突
        if (checkTeacherTimeConflict(schedule.getTeacherId(), schedule.getWeekDay(), 
            schedule.getStartTime(), schedule.getEndTime())) {
            result.put("conflict", true);
            result.put("type", "teacher");
            result.put("message", "该时间段教师已有其他课程安排");
            return result;
        }
        
        // 检查班级时间冲突
        if (checkClassTimeConflict(schedule.getClassId(), schedule.getWeekDay(), 
            schedule.getStartTime(), schedule.getEndTime())) {
            result.put("conflict", true);
            result.put("type", "class");
            result.put("message", "该时间段班级已有其他课程安排");
            return result;
        }
        
        // 检查教室时间冲突
        // 此处通过LambdaQueryWrapper查询，而不是使用不存在的方法
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
        // 获取该学生的班级ID
        // Assuming studentDao is available to getStudentById
        // Student student = studentDao.getStudentById(studentId);
        // Long classId = student.getClassId();
        Long classId = 1L; // 临时使用固定值，实际应从数据库获取
        
        // 获取该班级的所有课表
        return scheduleDao.findByClassId(classId);
    }
}