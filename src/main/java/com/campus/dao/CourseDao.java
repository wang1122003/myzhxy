package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Course;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 课程数据访问接口
 */
@Repository
public interface CourseDao extends BaseMapper<Course> {
    /**
     * 根据ID查询课程
     * @param id 课程ID
     * @return 课程对象
     */
    Course findById(Long id);
    
    /**
     * 查询所有课程
     * @return 课程列表
     */
    List<Course> findAll();
    
    /**
     * 根据教师ID查询课程
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> findByTeacherId(Long teacherId);
    
    /**
     * 根据课程代码查询课程
     * @param courseCode 课程代码
     * @return 课程对象
     */
    Course findByCourseCode(String courseCode);
    
    /**
     * 根据部门/学院查询课程
     * @param department 部门/学院
     * @return 课程列表
     */
    List<Course> findByDepartment(String department);
    
    /**
     * 根据课程类型查询课程
     * @param courseType 课程类型
     * @return 课程列表
     */
    List<Course> findByCourseType(String courseType);
    
    /**
     * 分页查询课程
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 课程列表
     */
    List<Course> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取课程总数
     * @return 课程数量
     */
    int getCount();
    
    /**
     * 添加课程
     * @param course 课程对象
     * @return 影响行数
     */
    int insert(Course course);
    
    /**
     * 更新课程
     * @param course 课程对象
     * @return 影响行数
     */
    int update(Course course);
    
    /**
     * 删除课程
     * @param id 课程ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 批量删除课程
     * @param ids 课程ID数组
     * @return 影响行数
     */
    int batchDelete(Long[] ids);
    
    /**
     * 更新课程状态
     * @param id 课程ID
     * @param status 课程状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 搜索课程
     * @param keyword 关键词
     * @return 课程列表
     */
    List<Course> searchCourses(@Param("keyword") String keyword);
    
    /**
     * 获取教师课程数
     * @param teacherId 教师ID
     * @return 课程数量
     */
    int getTeacherCourseCount(Long teacherId);
    
    /**
     * 获取课程类型课程数
     * @param courseType 课程类型
     * @return 课程数量
     */
    int getCourseTypeCount(String courseType);
    
    /**
     * 检查课程代码是否存在
     * @param courseCode 课程代码
     * @return 影响行数 (通常 > 0 表示存在)
     */
    int countByCourseCode(String courseCode);
    
    /**
     * 获取指定状态的课程数量
     * @param status 课程状态
     * @return 课程数量
     */
    int getStatusCount(int status);
    
    /**
     * 获取不同部门/学院的数量
     * @return 部门/学院数量
     */
    int getDepartmentCount();
    
    /**
     * 获取课程平均学分
     * @return 平均学分
     */
    double getAverageCredits();
    
    /**
     * 获取课程类型统计
     * @return 课程类型统计列表
     */
    List<Map<String, Object>> getCourseTypeStats();
    
    /**
     * 获取部门/学院统计
     * @return 部门/学院统计列表
     */
    List<Map<String, Object>> getDepartmentStats();
    
    /**
     * 获取学分分布统计
     * @return 学分分布统计列表
     */
    List<Map<String, Object>> getCreditDistribution();
    
    /**
     * 获取课程状态统计
     * @return 课程状态统计列表
     */
    List<Map<String, Object>> getStatusStats();
    
    /**
     * 通过关键词搜索课程（另一种实现）
     * @param keyword 关键词
     * @return 课程列表
     */
    List<Course> searchCoursesByKeyword(@Param("keyword") String keyword);
}