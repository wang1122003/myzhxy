package com.campus.controller;

import com.campus.service.CourseSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> pageQuery(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Map<String, Object> params) {
        return ResponseEntity.ok(courseSelectionService.pageQuery(current, size, params));
    }

    /**
     * 获取选课记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> result = courseSelectionService.getMapById(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 根据学生ID获取选课记录
     */
    @GetMapping("/student/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getByStudentId(@PathVariable Long userId) {
        return ResponseEntity.ok(courseSelectionService.getByStudentIdMaps(userId));
    }

    /**
     * 根据课程ID获取选课记录
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Map<String, Object>>> getByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseSelectionService.getByCourseIdMaps(courseId));
    }

    /**
     * 学生选课
     */
    @PostMapping("/select")
    public ResponseEntity<Boolean> selectCourse(@RequestBody Map<String, Object> courseSelectionMap) {
        boolean result = courseSelectionService.selectCourseByMap(courseSelectionMap);
        return ResponseEntity.ok(result);
    }

    /**
     * 学生退选
     */
    @PostMapping("/drop")
    public ResponseEntity<Boolean> dropCourse(
            @RequestParam Long userId,
            @RequestParam Long courseId,
            @RequestParam String termInfo) {
        boolean result = courseSelectionService.dropCourse(userId, courseId, termInfo);
        return ResponseEntity.ok(result);
    }

    /**
     * 教师录入成绩
     */
    @PutMapping("/score")
    public ResponseEntity<Boolean> updateScore(@RequestBody Map<String, Object> scoreMap) {
        boolean result = courseSelectionService.updateScoreByMap(scoreMap);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增选课记录
     */
    @PostMapping
    public ResponseEntity<Boolean> save(@RequestBody Map<String, Object> courseSelectionMap) {
        boolean result = courseSelectionService.saveByMap(courseSelectionMap);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新选课记录
     */
    @PutMapping
    public ResponseEntity<Boolean> update(@RequestBody Map<String, Object> courseSelectionMap) {
        boolean result = courseSelectionService.updateByMap(courseSelectionMap);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除选课记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        boolean result = courseSelectionService.removeById(id);
        return ResponseEntity.ok(result);
    }
} 