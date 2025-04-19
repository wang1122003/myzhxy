package com.campus.controller;

import com.campus.entity.Schedule;
import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.service.ScheduleService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * 获取课表列表
     * @param termId 学期ID
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param classroomId 教室ID
     * @return 课表列表
     */
    @GetMapping
    public Result getSchedules(
            @RequestParam(value = "termId", required = false) Long termId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "teacherId", required = false) Long teacherId,
            @RequestParam(value = "classroomId", required = false) Long classroomId,
            @RequestParam(value = "classId", required = false) Long classId) {
        
        try {
            List<Schedule> schedules;
            
            // 根据不同的查询条件组合返回不同的结果
            if (termId != null && teacherId != null) {
                schedules = scheduleService.getSchedulesByTeacherIdAndTermId(teacherId, termId);
            } else if (termId != null && classId != null) {
                schedules = scheduleService.getSchedulesByClassIdAndTermId(classId, termId);
            } else if (termId != null && classroomId != null) {
                schedules = scheduleService.getSchedulesByClassroomIdAndTermId(classroomId, termId);
            } else if (termId != null) {
                schedules = scheduleService.getSchedulesByTermId(termId);
            } else if (teacherId != null) {
                schedules = scheduleService.getSchedulesByTeacherId(teacherId);
            } else if (courseId != null) {
                schedules = scheduleService.getSchedulesByCourseId(courseId);
            } else if (classroomId != null) {
                schedules = scheduleService.getSchedulesByClassroomId(classroomId);
            } else if (classId != null) {
                schedules = scheduleService.getSchedulesByClassId(classId);
            } else {
                schedules = scheduleService.getAllSchedules();
            }
            
            return Result.success(schedules);
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
            
            // 设置ID
            schedule.setId(id);
            
            // 检查时间冲突
            if (scheduleService.checkTimeConflict(schedule)) {
                return Result.error("所选时间段已被占用，请选择其他时间");
            }
            
            boolean success = scheduleService.updateSchedule(schedule);
            if (success) {
                return Result.success("课表更新成功");
            } else {
                return Result.error("课表更新失败");
            }
        } catch (Exception e) {
            log.error("更新课表失败", e);
            return Result.error("更新课表失败: " + e.getMessage());
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
     * 批量生成课表
     * @param params 参数
     * @return 生成结果
     */
    @PostMapping("/batch-generate")
    public Result batchGenerateSchedules(@RequestBody Map<String, Object> params) {
        try {
            // 添加类型检查和转换
            if (!params.containsKey("termId") || !params.containsKey("options")) {
                return Result.error("参数不完整");
            }
            
            Long termId;
            try {
                termId = Long.parseLong(params.get("termId").toString());
            } catch (NumberFormatException e) {
                return Result.error("学期ID格式错误");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Boolean> options = (Map<String, Boolean>) params.get("options");
            
            // 验证参数
            if (termId == null) {
                return Result.error("学期不能为空");
            }
            
            if (options == null) {
                return Result.error("选项不能为空");
            }
            
            // TODO: 实现批量生成课表的逻辑
            // 这里需要根据项目实际情况实现较复杂的排课算法
            // 1. 获取所有课程、教师、教室、班级等数据
            // 2. 根据各种约束条件（如教师和教室不能冲突、均衡负担等）进行排课
            // 3. 生成课表数据并保存到数据库
            
            // 模拟批量生成成功
            return Result.success("课表批量生成成功");
        } catch (Exception e) {
            log.error("批量生成课表失败", e);
            return Result.error("批量生成课表失败: " + e.getMessage());
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
            User currentUser = authService.getCurrentUser(request);
            if (currentUser == null) {
                log.warn("getStudentSchedule: 未找到当前登录用户");
                return Result.error("未登录或无法获取用户信息");
            }

            // 确保是学生用户 (userType=1)
            if (currentUser.getUserType() != 1) {
                log.warn("getStudentSchedule: 用户 {} 不是学生，无法获取课表", currentUser.getUsername());
                return Result.error("非学生用户无法获取课表");
            }

            Long studentId = currentUser.getId(); // 假设可以直接用 User ID
            log.info("getStudentSchedule: 开始获取学生ID {} 在学期 '{}' 的课表", studentId, semester);

            // 调用 Service 层方法获取课表
            // !!! 重要: 确保 ScheduleService 中有 getSchedulesByStudentIdAndSemester 方法 !!!
            List<Schedule> schedules = scheduleService.getSchedulesByStudentIdAndSemester(studentId, semester);

            log.info("getStudentSchedule: 成功获取学生ID {} 在学期 '{}' 的课表 {} 条", studentId, semester, schedules == null ? 0 : schedules.size());
            return Result.success(schedules);

        } catch (Exception e) {
            log.error("获取学生课表失败", e);
            // 返回更友好的错误信息给前端
            return Result.error("获取课表信息时发生错误，请稍后重试");
        }
    }
}