package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Course;
import com.campus.entity.User;
import com.campus.exception.AuthenticationException;
import com.campus.exception.CustomException;
import com.campus.service.AuthService;
import com.campus.service.CourseService;
import com.campus.service.ScheduleService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程管理控制器
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private AuthService authService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 获取课程详情
     * 
     * @param id 课程ID
     * @return 课程详细信息
     */
    @GetMapping("/{id}")
    public Result<Course> getCourseById(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id);
            if (course != null) {
                return Result.success(course);
            } else {
                return Result.error("课程不存在");
            }
        } catch (Exception e) {
            return Result.error("获取课程详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据课程编号获取课程
     * 
     * @param courseNo 课程编号
     * @return 课程详细信息
     */
    @GetMapping("/no/{courseNo}")
    public Result<Course> getCourseByNo(@PathVariable String courseNo) {
        try {
            Course course = courseService.getCourseByCourseCode(courseNo);
            if (course != null) {
                return Result.success(course);
            } else {
                return Result.error("课程不存在");
            }
        } catch (Exception e) {
            return Result.error("获取课程失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程列表 (分页)
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词(可选)
     * @return 课程分页结果
     */
    @GetMapping
    public Result<Map<String, Object>> getAllCourses(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            IPage<Course> pageData = courseService.getCoursesByPage(pageNum, pageSize);
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageData.getRecords());
            result.put("total", pageData.getTotal());
            return Result.success(result); // 返回分页结果
        } catch (Exception e) {
            return Result.error("获取课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据课程类型获取课程
     * 
     * @param courseType 课程类型
     * @return 课程列表
     */
    @GetMapping("/type/{courseType}")
    public Result<List<Course>> getCoursesByType(@PathVariable Integer courseType) {
        try {
            List<Course> courses = courseService.getCoursesByCourseType(courseType);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error("获取课程失败: " + e.getMessage());
        }
    }

    /**
     * 根据学院ID获取课程
     * 
     * @param collegeId 学院ID
     * @return 课程列表
     */
    @GetMapping("/college/{collegeId}")
    public Result<List<Course>> getCoursesByCollege(@PathVariable Long collegeId) {
        return Result.error(501, "根据学院获取课程功能暂未实现或已移除");
    }

    /**
     * 添加课程
     * 
     * @param course 课程信息
     * @return 添加结果
     */
    @PostMapping
    public Result<Void> addCourse(@RequestBody Course course) {
        try {
            boolean result = courseService.addCourse(course);
            // Service 层现在会在失败时抛出异常
            return result ? Result.success("添加成功") : Result.error("添加失败");
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            return Result.error("添加课程时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新课程
     * 
     * @param id 课程ID
     * @param course 课程信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        try {
            User currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser == null) {
                throw new AuthenticationException("用户未登录");
            }

            course.setId(id);
            if (course.getCourseNo() == null || course.getCourseNo().isEmpty() ||
                    course.getCourseName() == null || course.getCourseName().isEmpty()) {
                return Result.error("课程代码和课程名称不能为空");
            }
            boolean result = courseService.updateCourse(course);
            return result ? Result.success("更新成功") : Result.error("更新失败");
        } catch (AuthenticationException ae) {
            return Result.error(401, ae.getMessage());
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除课程
     * 
     * @param id 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser == null) {
                throw new AuthenticationException("用户未登录");
            }

            boolean isScheduled = scheduleService.isCourseScheduled(id);
            if (isScheduled) {
                return Result.error("该课程已被排课使用，无法删除");
            }
            
            boolean result = courseService.deleteCourse(id);
            return result ? Result.success("删除成功") : Result.error("删除失败");
        } catch (AuthenticationException ae) {
            return Result.error(401, ae.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除课程
     *
     * @param ids 课程ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteCourses(@RequestBody List<Long> ids) {
        try {
            List<Long> scheduledCourseIds = scheduleService.findScheduledCourseIds(ids);

            if (!scheduledCourseIds.isEmpty()) {
                return Result.error("部分课程已被排课使用，无法删除");
            }

            boolean success = courseService.batchDeleteCourses(ids.toArray(new Long[0]));
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新课程状态
     *
     * @param id        课程ID
     * @param statusInt 课程状态 (Integer for path variable)
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{statusInt}")
    public Result<Void> updateCourseStatus(@PathVariable Long id, @PathVariable Integer statusInt) {
        try {
            Course course = courseService.getCourseById(id);
            if (course == null) {
                return Result.error("课程不存在");
            }
            String status = String.valueOf(statusInt);
            course.setStatus(status);
            boolean result = courseService.updateCourse(course);
            return result ? Result.success("更新状态成功") : Result.error("更新状态失败");
        } catch (Exception e) {
            return Result.error("更新状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch/status/{statusInt}")
    public Result<Void> batchUpdateCourseStatus(@RequestBody List<Long> ids, @PathVariable Integer statusInt) {
        try {
            String status = String.valueOf(statusInt);
            boolean overallResult = true;
            for (Long id : ids) {
                Course course = courseService.getCourseById(id);
                if (course != null) {
                    course.setStatus(status);
                    overallResult = overallResult && courseService.updateCourse(course);
                } else {
                    overallResult = false;
                }
            }
            return overallResult ? Result.success("批量更新状态成功") : Result.error("批量更新状态失败 (部分课程可能未找到或更新失败)");
        } catch (Exception e) {
            return Result.error("批量更新状态失败: " + e.getMessage());
        }
    }
}