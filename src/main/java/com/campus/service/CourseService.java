package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Course;

import java.util.List;

/**
 * 课程服务接口
 */
public interface CourseService {
    
    /**
     * 根据ID查询课程信息
     * @param id 课程ID
     * @return 课程实体
     */
    Course getCourseById(Long id);
    
    /**
     * 根据课程编号查询课程信息
     * @param courseNo 课程编号
     * @return 课程实体
     */
    Course getCourseByCourseCode(String courseNo);
    
    /**
     * 查询所有课程 (分页)
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 课程分页结果
     */
    IPage<Course> getAllCourses(int pageNum, int pageSize);
    
    /**
     * 根据课程类型查询课程列表
     * @param courseType 课程类型
     * @return 课程列表
     */
    List<Course> getCoursesByCourseType(Integer courseType);
    
    /**
     * 添加课程
     * @param course 课程实体
     * @return 是否成功
     */
    boolean addCourse(Course course);
    
    /**
     * 更新课程信息
     * @param course 课程实体
     * @return 是否成功
     */
    boolean updateCourse(Course course);
    
    /**
     * 删除课程
     * @param id 课程ID
     * @return 是否成功
     */
    boolean deleteCourse(Long id);
    
    /**
     * 批量删除课程
     * @param ids 课程ID数组
     * @return 是否成功
     */
    boolean batchDeleteCourses(Long[] ids);
    
    /**
     * 分页查询课程列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 课程分页结果 (IPage)
     */
    IPage<Course> getCoursesByPage(int pageNum, int pageSize);
    
    /**
     * 获取课程总数
     * @return 课程总数
     */
    int getCourseCount();
    
    /**
     * 搜索课程
     * @param keyword 关键词
     * @return 课程列表
     */
    List<Course> searchCourses(String keyword);
    
    /**
     * 检查课程编号是否存在
     * @param courseNo 课程编号
     * @return 是否存在
     */
    boolean checkCourseCodeExists(String courseNo);
}