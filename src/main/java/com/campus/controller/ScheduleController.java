package com.campus.controller;

// import com.campus.dto.ScheduleDTO; // 移除未使用

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Schedule;
// import com.campus.entity.Term; // Removed Term entity dependency
import com.campus.entity.User;
import com.campus.enums.UserType; // Import UserType
import com.campus.exception.CustomException;
import com.campus.service.AuthService;
import com.campus.service.ScheduleService;
// import com.campus.service.TermService; // Removed TermService dependency
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 课程表管理控制器
 * 提供课程安排查询和管理的相关API
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private AuthService authService;

    // @Autowired
    // private TermService termService; // Removed TermService injection

    /**
     * 分页获取课表列表，支持多条件模糊查询
     *
     * @param termCode 学期代码 (可选)
     * // TODO: [学期功能] - Replace termCode filtering with config-based logic or remove if not needed immediately
     * @param courseName 课程名称 (可选)
     * @param teacherName 教师姓名 (可选)
     * @param className 班级名称 (可选)
     * @param classroomId 教室ID (可选)
     * @param page 页码
     * @param size 每页数量
     * @return 课表分页数据
     */
    @GetMapping
    public Result<IPage<Schedule>> getSchedules(
            @RequestParam(required = false) String termCode,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> params = new HashMap<>();
            // Long termId = null;
            // Term entity and service removed, handle termCode filtering differently if needed
            /*
            if (termCode != null && !termCode.isEmpty()) {
                Term term = termService.getByCode(termCode);
                if (term != null) {
                    termId = term.getId();
                }
            }
            if (termId != null) params.put("termId", termId); 
            */
            // Pass termCode directly to service if it handles string-based filtering
            if (termCode != null && !termCode.isEmpty()) params.put("termInfo", termCode);
            
            if (courseName != null && !courseName.isEmpty()) params.put("courseName", courseName);
            if (teacherName != null && !teacherName.isEmpty()) params.put("teacherName", teacherName);
            if (className != null && !className.isEmpty()) params.put("className", className);
            if (classroomId != null) params.put("classroomId", classroomId);

            IPage<Schedule> schedulePage = scheduleService.getSchedulesPage(params, page, size);

            return Result.success(schedulePage);
        } catch (Exception e) {
            return Result.error("获取课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个课表
     * @param id 课表ID
     * @return 课表详情
     */
    @GetMapping("/{id}")
    public Result<Schedule> getScheduleById(@PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.getScheduleById(id);
            if (schedule != null) {
                return Result.success(schedule);
            } else {
                return Result.error("课表不存在");
            }
        } catch (Exception e) {
            return Result.error("获取课表详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加课表
     * @param schedule 课表信息
     * @return 添加结果
     */
    @PostMapping
    public Result<Object> addSchedule(@RequestBody Schedule schedule) {
        try {
            // 参数校验
            if (schedule.getCourseId() == null) {
                return Result.error("课程不能为空");
            }
            if (schedule.getTeacherId() == null) {
                return Result.error("教师不能为空");
            }
            if (schedule.getClassroomId() == null) {
                return Result.error("教室不能为空");
            }
            if (schedule.getDayOfWeek() == null || schedule.getDayOfWeek().isEmpty()) {
                return Result.error("星期几不能为空");
            }
            
            // 检查时间冲突
            if (scheduleService.checkTimeConflict(schedule)) {
                return Result.error("所选时间段已被占用，请选择其他时间");
            }
            
            boolean success = scheduleService.addSchedule(schedule);
            if (success) {
                return Result.success("课表添加成功");
            } else {
                return Result.error("课表添加失败");
            }
        } catch (Exception e) {
            return Result.error("添加课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新课表
     * @param id 课表ID
     * @param schedule 课表信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Object> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        schedule.setId(id);
        try {
            // 可在此处添加验证 (例如，检查必填字段)
            boolean result = scheduleService.updateSchedule(schedule);
            return result ? Result.success("更新成功") : Result.error("更新失败，可能存在时间冲突或记录不存在");
        } catch (CustomException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新课表时发生未知错误");
        }
    }
    
    /**
     * 删除课表
     * @param id 课表ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Object> deleteSchedule(@PathVariable Long id) {
        try {
            boolean success = scheduleService.deleteSchedule(id);
            if (success) {
                return Result.success("课表删除成功");
            } else {
                return Result.error("课表删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除课表
     * @param ids 课表ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Object> batchDeleteSchedules(@RequestBody Long[] ids) {
        try {
            if (ids == null || ids.length == 0) {
                return Result.error("未选择要删除的课表");
            }
            
            boolean success = scheduleService.batchDeleteSchedules(ids);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查时间冲突
     * @param schedule 课表信息
     * @return 冲突检查结果
     */
    @PostMapping("/check-conflict")
    public Result<Map<String, Object>> checkConflict(@RequestBody Schedule schedule) {
        try {
            Map<String, Object> result = scheduleService.checkScheduleConflict(schedule);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检查时间冲突失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取教师周课表
     * @param teacherId 教师ID
     * @param termId 学期ID
     * @return 周课表数据
     */
    @GetMapping("/teacher-weekly")
    public Result<Map<String, Object>> getTeacherWeeklySchedule(
            @RequestParam Long teacherId,
            @RequestParam Long termId) {
        try {
            Map<String, Object> result = scheduleService.getTeacherWeeklySchedule(teacherId, String.valueOf(termId));
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取教师周课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生周课表
     * // TODO: [学期功能] - termId parameter might need adjustment based on config implementation.
     * @param userId 学生用户ID
     * @param termId 学期ID (Currently assumes ID, might change to termCode)
     * @return 周课表数据
     */
    @GetMapping("/student-weekly")
    public Result<Map<String, Object>> getStudentWeeklySchedule(
            @RequestParam Long userId,
            @RequestParam Long termId) { // Keep Long for now, might change to String termCode later
        try {
            Map<String, Object> result = scheduleService.getStudentWeeklySchedule(userId, String.valueOf(termId));
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取学生周课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取教室周课表
     * @param classroomId 教室ID
     * @param termId 学期ID
     * @return 周课表数据
     */
    @GetMapping("/classroom-weekly")
    public Result<Map<String, Object>> getClassroomWeeklySchedule(
            @RequestParam Long classroomId,
            @RequestParam Long termId) {
        try {
            Map<String, Object> result = scheduleService.getClassroomWeeklySchedule(classroomId, String.valueOf(termId));
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取教室周课表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录学生的课表
     *
     * @param semester 学期信息 (可选)
     * // TODO: [学期功能] - Logic for handling semester/term needs update based on config implementation.
     * @param request HTTP请求
     * @return 课表数据
     */
    @GetMapping("/student")
    public Result<Map<String, Object>> getStudentSchedule(
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        try {
            User currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser == null) {
                return Result.error("未登录或会话已过期");
            }
            if (currentUser.getUserType() != UserType.STUDENT) {
                return Result.error("只有学生可以查看课表");
            }
            String termCodeToUse = semester;

            if (termCodeToUse == null || termCodeToUse.isEmpty()) {
                return Result.error("当前版本需要明确的学期参数");
            }
            // TODO: Refactor service call based on term handling
            return Result.error("学期功能正在调整中，请稍后重试");

        } catch (Exception e) {
            return Result.error("获取课表失败: " + e.getMessage());
        }
    }
}