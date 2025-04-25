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
     * 根据教师ID查询课程
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> findByTeacherId(Long teacherId);
    
    /**
     * 根据课程代码查询课程
     * @param courseNo 课程代码
     * @return 课程对象
     */
    Course findByCourseCode(String courseCode);
    
    /**
     * 根据课程类型查询课程
     * @param courseType 课程类型 (Integer for TINYINT)
     * @return 课程列表
     */
    List<Course> findByCourseType(Integer courseType);
    
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
     * @param status 课程状态 (String)
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 检查课程代码是否存在 (修改方法名以匹配 Mapper)
     * @param courseCode 课程代码
     * @return 是否存在 (改为 boolean)
     */
    boolean checkCourseCodeExists(String courseCode);
    
    /**
     * 通过关键词搜索课程
     * @param keyword 关键词
     * @return 课程列表
     */
    List<Course> searchCoursesByKeyword(@Param("keyword") String keyword);

    /**
     * 获取课程名称
     *
     * @param courseId 课程ID
     * @return 课程名称
     */
    String getCourseNameById(@Param("courseId") Long courseId);

    /**
     * 获取所有课程ID和名称
     *
     * @return 课程ID和名称列表
     */
    List<Map<String, Object>> getAllCourseIdsAndNames();
}