package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Course;
import com.campus.exception.CustomException;
import com.campus.service.CourseService;
import com.campus.service.ScheduleService;
import com.campus.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 获取课程详情
     * 
     * @param id 课程ID
     * @return 课程详细信息
     */
    @GetMapping("/{id}")
    public Result getCourseById(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id);
            if (course != null) {
                return Result.success(course);
            } else {
                return Result.error("课程不存在");
            }
        } catch (Exception e) {
            log.error("获取课程详情失败 (ID: {})", id, e);
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
    public Result getCourseByCourseNo(@PathVariable String courseNo) {
        try {
            Course course = courseService.getCourseByCourseNo(courseNo);
            if (course != null) {
                return Result.success(course);
            } else {
                return Result.error("课程不存在");
            }
        } catch (Exception e) {
            log.error("通过编号获取课程失败 (Code: {})", courseNo, e);
            return Result.error("获取课程失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程列表 (分页)
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 课程分页结果
     */
    @GetMapping
    public Result getAllCourses(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            IPage<Course> pageResult = courseService.getAllCourses(pageNum, pageSize);
            return Result.success(pageResult); // 返回分页结果
        } catch (Exception e) {
            log.error("获取课程列表失败", e);
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
    public Result getCoursesByCourseType(@PathVariable Integer courseType) {
        try {
            List<Course> courses = courseService.getCoursesByCourseType(courseType);
            return Result.success(courses);
        } catch (Exception e) {
            log.error("按类型获取课程失败 (Type: {})", courseType, e);
            return Result.error("获取课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据学院ID获取课程
     * 
     * @param collegeId 学院ID
     * @return 课程列表
     */
    @GetMapping("/college/{collegeId}")
    public Result getCoursesByCollegeId(@PathVariable Long collegeId) {
        try {
            List<Course> courses = courseService.getCoursesByCollegeId(collegeId);
            return Result.success(courses);
        } catch (Exception e) {
            log.error("按学院获取课程失败 (CollegeID: {})", collegeId, e);
            return Result.error("获取课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加课程
     * 
     * @param course 课程信息
     * @return 添加结果
     */
    @PostMapping
    public Result addCourse(@RequestBody Course course) {
        try {
            boolean result = courseService.addCourse(course);
            // Service 层现在会在失败时抛出异常
            return Result.success("添加成功");
        } catch (CustomException ce) {
            log.warn("添加课程失败: {}", ce.getMessage());
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            log.error("添加课程时发生错误", e);
            return Result.error("添加课程失败: " + e.getMessage());
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
    public Result updateCourse(@PathVariable Long id, @RequestBody Course course) {
        log.debug("更新课程 ID {}: {}", id, course.getCourseName());
        course.setId(id);
        try {
            // 基本验证 (可以添加更多)
            if (course.getCourseCode() == null || course.getCourseCode().isEmpty() ||
                    course.getCourseName() == null || course.getCourseName().isEmpty()) {
                return Result.error("课程代码和课程名称不能为空");
            }

            boolean result = courseService.updateCourse(course);
            log.info("课程 ID {} 更新结果: {}", id, result);
            return result ? Result.success("更新成功") : Result.error("更新失败，课程代码可能重复或课程不存在");
        } catch (CustomException e) {
            log.error("更新课程 ID {} 出错: {}", id, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新课程 ID {} 时发生未知错误", id, e);
            return Result.error("更新课程时发生未知错误");
        }
    }

    /**
     * 删除课程
     * 
     * @param id 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteCourse(@PathVariable Long id) {
        try {
            if (scheduleService.isCourseScheduled(id)) {
                return Result.error("课程已被排课使用，无法删除");
            }
            boolean result = courseService.deleteCourse(id);
            if (result) {
                return Result.success("删除成功");
            } else {
                // 如果 ServiceImpl.removeById 返回 false，通常意味着 ID 不存在
                return Result.error("删除失败或课程不存在");
            }
        } catch (Exception e) {
            log.error("删除课程失败 (ID: {})", id, e);
            return Result.error("删除课程失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除课程
     * 
     * @param ids 课程ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result batchDeleteCourses(@RequestBody Long[] ids) {
        try {
            if (ids == null || ids.length == 0) {
                return Result.error("未选择要删除的课程");
            }
            List<Long> idList = List.of(ids);
            List<Long> scheduledCourseIds = scheduleService.findScheduledCourseIds(idList);
            if (scheduledCourseIds != null && !scheduledCourseIds.isEmpty()) {
                log.warn("尝试批量删除课程，但以下课程已被排课使用: {}", scheduledCourseIds);
                return Result.error("部分课程已被排课使用，无法删除。请先处理相关排课。");
            }
            boolean result = courseService.batchDeleteCourses(ids);
            if (result) {
                return Result.success("批量删除成功");
            } else {
                // 如果 ServiceImpl.removeByIds 返回 false，通常意味着部分或全部 ID 不存在
                return Result.error("批量删除失败或部分课程不存在");
            }
        } catch (Exception e) {
            log.error("批量删除课程失败", e);
            return Result.error("批量删除课程失败: " + e.getMessage());
        }
    }

    /**
     * 更新课程状态
     * 
     * @param id 课程ID
     * @param status 课程状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    public Result updateCourseStatus(@PathVariable Long id, @PathVariable Integer status) {
        try {
            boolean result = courseService.updateCourseStatus(id, status);
            if (result) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新课程状态失败 (ID: {}, Status: {})", id, status, e);
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }
}