package com.campus.controller;

// import com.campus.dto.ScheduleDTO; // 移除未使用

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Schedule;
import com.campus.entity.User;
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

    /**
     * 获取课表列表 (支持分页和筛选)
     * @param termId 学期ID
     * @param courseName 课程名称 (用于模糊查询)
     * @param teacherName 教师名称 (用于模糊查询)
     * @param className 班级名称 (用于模糊查询)
     * @param page 页码
     * @param size 每页数量
     * @return 分页后的课表列表
     */
    @GetMapping
    public Result getSchedules(
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Map<String, Object> params = new HashMap<>();
            if (termId != null) params.put("termId", termId);
            if (courseName != null && !courseName.isEmpty()) params.put("courseName", courseName);
            if (teacherName != null && !teacherName.isEmpty()) params.put("teacherName", teacherName);
            if (className != null && !className.isEmpty()) params.put("className", className);
            if (classroomId != null) params.put("classroomId", classroomId);

            IPage<Schedule> schedulePage = scheduleService.getSchedulesPage(params, page, size);

            // 添加判断，如果列表为空，可以返回更具体的消息
            if (schedulePage.getRecords() == null || schedulePage.getRecords().isEmpty()) {
                return Result.success("未查询到符合条件的课表记录", schedulePage);
            }

            return Result.success(schedulePage);

        } catch (Exception e) {
            log.error("获取课表列表失败", e);
            return Result.error("获取课表列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个课表
     * @param id 课表ID
     * @return 课表详情
     */
    @GetMapping("/{id}")
    public Result getScheduleById(@PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.getScheduleById(id);
            if (schedule != null) {
                return Result.success(schedule);
            } else {
                return Result.error("课表不存在");
            }
        } catch (Exception e) {
            log.error("获取课表详情失败", e);
            return Result.error("获取课表详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加课表
     * @param schedule 课表信息
     * @return 添加结果
     */
    @PostMapping
    public Result addSchedule(@RequestBody Schedule schedule) {
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
            if (schedule.getWeekDay() == null) {
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
            log.error("添加课表失败", e);
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
    public Result updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        log.debug("更新课表 ID {}: 课程 {}, 教师 {}, 教室 {}, 星期 {}",
                id, schedule.getCourseId(), schedule.getTeacherId(), schedule.getClassroomId(), schedule.getWeekDay());
        schedule.setId(id);
        try {
            // 可在此处添加验证 (例如，检查必填字段)
            boolean result = scheduleService.updateSchedule(schedule);
            log.info("课表 ID {} 更新结果: {}", id, result);
            return result ? Result.success("更新成功") : Result.error("更新失败，可能存在时间冲突或记录不存在");
        } catch (CustomException e) {
            log.error("更新课表 ID {} 出错: {}", id, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新课表 ID {} 时发生未知错误", id, e);
            return Result.error("更新课表时发生未知错误");
        }
    }
    
    /**
     * 删除课表
     * @param id 课表ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteSchedule(@PathVariable Long id) {
        try {
            boolean success = scheduleService.deleteSchedule(id);
            if (success) {
                return Result.success("课表删除成功");
            } else {
                return Result.error("课表删除失败");
            }
        } catch (Exception e) {
            log.error("删除课表失败", e);
            return Result.error("删除课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除课表
     * @param ids 课表ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result batchDeleteSchedules(@RequestBody Long[] ids) {
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
            log.error("批量删除课表失败", e);
            return Result.error("批量删除课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查时间冲突
     * @param schedule 课表信息
     * @return 冲突检查结果
     */
    @PostMapping("/check-conflict")
    public Result checkConflict(@RequestBody Schedule schedule) {
        try {
            Map<String, Object> result = scheduleService.checkScheduleConflict(schedule);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查时间冲突失败", e);
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
    public Result getTeacherWeeklySchedule(
            @RequestParam Long teacherId,
            @RequestParam Long termId) {
        try {
            Map<String, Object> result = scheduleService.getTeacherWeeklySchedule(teacherId, termId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取教师周课表失败", e);
            return Result.error("获取教师周课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生周课表
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return 周课表数据
     */
    @GetMapping("/student-weekly")
    public Result getStudentWeeklySchedule(
            @RequestParam Long studentId,
            @RequestParam Long termId) {
        try {
            Map<String, Object> result = scheduleService.getStudentWeeklySchedule(studentId, termId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取学生周课表失败", e);
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
    public Result getClassroomWeeklySchedule(
            @RequestParam Long classroomId,
            @RequestParam Long termId) {
        try {
            Map<String, Object> result = scheduleService.getClassroomWeeklySchedule(classroomId, termId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取教室周课表失败", e);
            return Result.error("获取教室周课表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录学生的课表
     *
     * @param semester 学期 (可选, 格式如 2023-2024-1)
     * @param request  HTTP请求
     * @return 学生课表列表
     */
    @GetMapping("/student")
    public Result getStudentSchedule(
            @RequestParam(required = false) String semester, // 接收学期参数
            HttpServletRequest request) {
        try {
            User currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser == null) {
                log.warn("获取学生周课表失败: 用户未认证.");
                return Result.error(401, "用户未认证");
            }

            // 假设学生的用户类型现在是 "Student"
            if (!"Student".equals(currentUser.getUserType())) { // 使用 equals 进行字符串比较
                log.warn("用户 {} (类型 {}) 尝试访问学生课表.", currentUser.getUsername(), currentUser.getUserType());
                return Result.error(403, "权限不足，只有学生可以查看个人课表");
            }

            Long studentId = currentUser.getId(); // 已修正: getId()
            Long termId = Long.parseLong(semester.split("-")[0]); // 从 semester 提取 termId
            log.info("获取学生 ID: {} 在学期 ID: {} 的周课表", studentId, termId);
            try {
                Map<String, Object> weeklySchedule = scheduleService.getStudentWeeklySchedule(studentId, termId);
                log.debug("成功获取学生 ID: {} 的周课表", studentId);
                return Result.success("获取学生周课表成功", weeklySchedule);
            } catch (Exception e) {
                log.error("获取学生周课表失败", e);
                return Result.error("获取学生周课表失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("获取学生课表失败", e);
            // 返回更友好的错误信息给前端
            return Result.error("获取课表信息时发生错误，请稍后重试");
        }
    }
}