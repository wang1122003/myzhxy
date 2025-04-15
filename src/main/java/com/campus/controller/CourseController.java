package com.campus.controller;

import com.campus.entity.Course;
import com.campus.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程管理控制器
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取课程详情
     * 
     * @param id 课程ID
     * @return 课程详细信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course != null) {
            return ResponseEntity.ok(course);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据课程编号获取课程
     * 
     * @param courseNo 课程编号
     * @return 课程详细信息
     */
    @GetMapping("/no/{courseNo}")
    public ResponseEntity<Course> getCourseByCourseNo(@PathVariable String courseNo) {
        Course course = courseService.getCourseByCourseNo(courseNo);
        if (course != null) {
            return ResponseEntity.ok(course);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取所有课程
     * 
     * @return 课程列表
     */
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    /**
     * 根据课程类型获取课程
     * 
     * @param courseType 课程类型
     * @return 课程列表
     */
    @GetMapping("/type/{courseType}")
    public ResponseEntity<List<Course>> getCoursesByCourseType(@PathVariable Integer courseType) {
        return ResponseEntity.ok(courseService.getCoursesByCourseType(courseType));
    }

    /**
     * 根据学院ID获取课程
     * 
     * @param collegeId 学院ID
     * @return 课程列表
     */
    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<Course>> getCoursesByCollegeId(@PathVariable Long collegeId) {
        return ResponseEntity.ok(courseService.getCoursesByCollegeId(collegeId));
    }

    /**
     * 添加课程
     * 
     * @param course 课程信息
     * @return 添加结果
     */
    @PostMapping
    public ResponseEntity<String> addCourse(@RequestBody Course course) {
        boolean result = courseService.addCourse(course);
        if (result) {
            return ResponseEntity.ok("添加成功");
        } else {
            return ResponseEntity.badRequest().body("添加失败");
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
    public ResponseEntity<String> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        boolean result = courseService.updateCourse(course);
        if (result) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.badRequest().body("更新失败");
        }
    }

    /**
     * 删除课程
     * 
     * @param id 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
        boolean result = courseService.deleteCourse(id);
        if (result) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败");
        }
    }

    /**
     * 批量删除课程
     * 
     * @param ids 课程ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<String> batchDeleteCourses(@RequestBody Long[] ids) {
        boolean result = courseService.batchDeleteCourses(ids);
        if (result) {
            return ResponseEntity.ok("批量删除成功");
        } else {
            return ResponseEntity.badRequest().body("批量删除失败");
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
    public ResponseEntity<String> updateCourseStatus(@PathVariable Long id, @PathVariable Integer status) {
        boolean result = courseService.updateCourseStatus(id, status);
        if (result) {
            return ResponseEntity.ok("状态更新成功");
        } else {
            return ResponseEntity.badRequest().body("状态更新失败");
        }
    }
}