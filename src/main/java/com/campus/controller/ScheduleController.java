package com.campus.controller;

// import com.campus.dto.ScheduleDTO; // 移除未使用

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Schedule;
import com.campus.entity.User;
import com.campus.enums.Term;
import com.campus.enums.UserType;
import com.campus.exception.CustomException;
import com.campus.service.AuthService;
import com.campus.service.ScheduleService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 课程表管理控制器
 * 提供课程安排查询和管理的相关API
 */
@Slf4j
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private AuthService authService;

    // @Autowired
    // private TermService termService; // 移除 TermService 注入

    /**
     * 分页获取课表列表，支持多条件模糊查询
     *
     * @param termCode    学期代码 (可选)
     * @param courseName  课程名称 (可选)
     * @param teacherName 教师姓名 (可选)
     * @param className   班级名称 (可选)
     * @param classroomId 教室ID (可选)
     * @param page        页码
     * @param size        每页数量
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
            // Term 实体和服务已移除，如果需要，请以不同方式处理 termCode 过滤
            /*
            if (termCode != null && !termCode.isEmpty()) {
                Term term = termService.getByCode(termCode);
                if (term != null) {
                    termId = term.getId();
                }
            }
            if (termId != null) params.put("termId", termId); 
            */
            // 如果服务支持基于字符串的过滤，则直接将 termCode 传递给服务
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
     *
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
     *
     * @param schedule 课表信息
     * @return 添加结果
     */
    @PostMapping
    public Result<Object> addSchedule(@RequestBody Schedule schedule) {
        try {
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
     *
     * @param id       课表ID
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
     *
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
     *
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
     *
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
     * @param termCode  学期代码 (可选, 默认当前学期)
     */
    @GetMapping("/teacher-weekly")
    public Result<Map<String, Object>> getTeacherWeeklySchedule(
            @RequestParam Long teacherId,
            @RequestParam(required = false) String termCode) {
        String effectiveTermCode = getEffectiveTermCode(termCode);
        if (effectiveTermCode == null) return Result.error("无法确定学期");
        Map<String, Object> schedule = scheduleService.getTeacherWeeklySchedule(teacherId, effectiveTermCode);
        return Result.success(schedule);
    }

    /**
     * 获取学生周课表
     * @param studentId 学生ID (User ID)
     * @param termCode  学期代码 (可选, 默认当前学期)
     */
    @GetMapping("/student-weekly")
    public Result<Map<String, Object>> getStudentWeeklySchedule(
            @RequestParam Long studentId,
            @RequestParam(required = false) String termCode) {
        String effectiveTermCode = getEffectiveTermCode(termCode);
        if (effectiveTermCode == null) return Result.error("无法确定学期");
        Map<String, Object> schedule = scheduleService.getStudentWeeklySchedule(studentId, effectiveTermCode);
        return Result.success(schedule);
    }

    /**
     * 获取教室周课表
     * @param classroomId 教室ID
     * @param termCode    学期代码 (可选, 默认当前学期)
     */
    @GetMapping("/classroom-weekly")
    public Result<Map<String, Object>> getClassroomWeeklySchedule(
            @RequestParam Long classroomId,
            @RequestParam(required = false) String termCode) {
        String effectiveTermCode = getEffectiveTermCode(termCode);
        if (effectiveTermCode == null) return Result.error("无法确定学期");
        Map<String, Object> schedule = scheduleService.getClassroomWeeklySchedule(classroomId, effectiveTermCode);
        return Result.success(schedule);
    }

    /**
     * (学生端) 根据用户ID和学期信息获取课表列表
     *
     * @param termInfo 学期代码 (可选, 默认当前学期)
     * @param request  HttpServletRequest 用于获取当前用户信息
     * @return 学生在该学期的课表列表
     */
    @GetMapping("/student")
    public Result<Map<String, Object>> getStudentSchedule(
            @RequestParam(required = false) String termInfo,
            HttpServletRequest request) {
        try {
            User currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser == null || !currentUser.getUserType().equals(UserType.STUDENT)) {
                return Result.error("用户未登录或非学生用户");
            }

            String effectiveTermCode = getEffectiveTermCode(termInfo);
            if (effectiveTermCode == null) {
                return Result.error("无法确定学期");
            }

            // 使用周视图格式的数据，这样在前端可以同时支持周视图和列表视图
            Map<String, Object> schedule = scheduleService.getStudentWeeklySchedule(currentUser.getId(), effectiveTermCode);
            return Result.success(schedule);

        } catch (CustomException e) {
            // log.warn("获取学生课表时发生业务异常: {}", e.getMessage());
            // 根据异常类型决定返回的状态码和信息
            return Result.error(e.getCode() != null ? e.getCode() : 400, e.getMessage());
        } catch (Exception e) {
            log.error("获取学生课表时发生未知错误", e); // 取消日志记录注释
            return Result.error(500, "获取学生课表失败，请稍后重试或联系管理员");
        }
    }

    /**
     * 获取有效的学期代码
     * 如果提供了学期代码且有效，则使用提供的代码，否则尝试获取当前学期
     *
     * @param providedTermCode 提供的学期代码
     * @return 有效的学期代码，如果无法确定则返回null
     */
    private String getEffectiveTermCode(String providedTermCode) {
        if (providedTermCode != null && !providedTermCode.trim().isEmpty()) {
            // 可选：验证providedTermCode是否存在于Term枚举中
            if (Term.fromCode(providedTermCode) != null) {
                return providedTermCode;
            } else {
                // log.warn("提供的学期代码无效: {}", providedTermCode);
                return null; // 表示错误
            }
        }
        Term currentTerm = Term.getCurrentTerm();
        if (currentTerm != null) {
            return currentTerm.getCode();
        } else {
            // log.error("当前学期未在Term枚举中定义。");
            return null; // 表示错误
        }
    }
}