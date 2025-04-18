package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Course;

import java.util.List;
import java.util.Map;

/**
 * 课程服务接口
 */
public interface CourseService {
    
    /**
     * 根据ID查询课程
     * @param id 课程ID
     * @return 课程对象
     */
    Course getCourseById(Long id);
    
    /**
     * 根据课程编号查询课程
     * @param courseNo 课程编号
     * @return 课程对象
     */
    Course getCourseByCourseNo(String courseNo);
    
    /**
     * 查询所有课程 (分页)
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 课程分页结果
     */
    IPage<Course> getAllCourses(int pageNum, int pageSize);
    
    /**
     * 根据课程类型查询课程
     * @param courseType 课程类型
     * @return 课程列表
     */
    List<Course> getCoursesByCourseType(Integer courseType);
    
    /**
     * 根据学院ID查询课程
     * @param collegeId 学院ID
     * @return 课程列表
     */
    List<Course> getCoursesByCollegeId(Long collegeId);
    
    /**
     * 添加课程
     * @param course 课程对象
     * @return 是否成功
     */
    boolean addCourse(Course course);
    
    /**
     * 更新课程
     * @param course 课程对象
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
     * 修改课程状态
     * @param id 课程ID
     * @param status 状态值
     * @return 是否成功
     */
    boolean updateCourseStatus(Long id, Integer status);
    
    /**
     * 分页查询课程 (此方法可能与 getAllCourses(pageNum, pageSize) 重复，考虑是否保留)
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
    boolean isCourseNoExists(String courseNo);
    
    /**
     * 获取课程统计信息
     * @return 统计信息
     */
    Map<String, Object> getCourseStats();
    
    /**
     * 获取课程类型统计
     * @return 课程类型统计列表
     */
    List<Map<String, Object>> getCourseTypeStats();
    
    /**
     * 获取学院课程统计
     * @return 学院课程统计列表
     */
    List<Map<String, Object>> getCollegeCourseStats();
    
    /**
     * 获取学分分布统计
     * @return 学分分布统计列表
     */
    List<Map<String, Object>> getCreditDistribution();
    
    /**
     * 获取课程状态统计
     * @return 课程状态统计列表
     */
    List<Map<String, Object>> getCourseStatusStats();
}