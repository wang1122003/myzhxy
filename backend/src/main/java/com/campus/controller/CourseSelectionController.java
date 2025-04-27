package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.CourseSelection;
import com.campus.service.CourseSelectionService;
import com.campus.utils.Result;
import com.campus.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 选课管理控制器
 */
@RestController
@RequestMapping("/api/course-selections")
public class CourseSelectionController {

    @Autowired
    private CourseSelectionService courseSelectionService;

    /**
     * 分页查询选课记录
     */
    @GetMapping("/page")
    public Result<IPage<CourseSelection>> pageQuery(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Map<String, Object> params) {
        IPage<CourseSelection> pageResult = courseSelectionService.pageQuery(current, size, params);
        return Result.success(pageResult);
    }

    /**
     * 获取选课记录详情
     */
    @GetMapping("/{id}")
    public Result<CourseSelection> getById(@PathVariable Long id) {
        CourseSelection selection = courseSelectionService.getById(id);
        if (selection == null) {
            return Result.error(404, "未找到选课记录");
        }
        return Result.success(selection);
    }

    /**
     * 根据学生ID获取选课记录
     */
    @GetMapping("/student/{userId}")
    public Result<List<CourseSelection>> getByStudentId(@PathVariable Long userId) {
        List<CourseSelection> selections = courseSelectionService.getByStudentId(userId);
        return Result.success(selections);
    }

    /**
     * 根据课程ID获取选课记录
     */
    @GetMapping("/course/{courseId}")
    public Result<List<CourseSelection>> getByCourseId(@PathVariable Long courseId) {
        List<CourseSelection> selections = courseSelectionService.getByCourseId(courseId);
        return Result.success(selections);
    }

    /**
     * 学生选课
     * @param courseSelection 包含 userId, courseId, termInfo
     */
    @PostMapping("/select")
    public Result<Void> selectCourse(@RequestBody CourseSelection courseSelection) {
        try {
            boolean result = courseSelectionService.selectCourse(courseSelection);
            return result ? Result.success("选课成功") : Result.error("选课失败，可能已选或存在冲突");
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            return Result.error("选课时发生错误: " + e.getMessage());
        }
    }

    /**
     * 学生退选
     */
    @PostMapping("/drop")
    public Result<Void> dropCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId,
            @RequestParam String termInfo) {
        try {
            boolean result = courseSelectionService.dropCourse(userId, courseId, termInfo);
            return result ? Result.success("退选成功") : Result.error("退选失败，未找到记录或已录入成绩");
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            return Result.error("退选时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除选课记录 (通常管理员操作)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean result = courseSelectionService.removeById(id);
        return result ? Result.success("删除成功") : Result.error("删除失败");
    }
} 