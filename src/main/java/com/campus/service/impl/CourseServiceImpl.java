package com.campus.service.impl;

import com.campus.dao.CourseDao;
import com.campus.entity.Course;
import com.campus.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程服务实现类
 */
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseDao courseDao;

    @Override
    public Course getCourseById(Long id) {
        return courseDao.findById(id);
    }

    @Override
    public Course getCourseByCourseNo(String courseNo) {
        return courseDao.findByCourseCode(courseNo);
    }

    @Override
    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    @Override
    public List<Course> getCoursesByCourseType(Integer courseType) {
        return courseDao.findByCourseType(String.valueOf(courseType));
    }

    @Override
    public List<Course> getCoursesByCollegeId(Long collegeId) {
        // 假设学院ID对应department字段
        // 这里可能需要根据实际模型做调整
        return courseDao.findByDepartment(String.valueOf(collegeId));
    }

    @Override
    @Transactional
    public boolean addCourse(Course course) {
        // 设置创建时间和更新时间
        Date now = new Date();
        course.setCreateTime(now);
        course.setUpdateTime(now);
        
        // 如果没有设置状态，默认为未开课
        if (course.getStatus() == null) {
            course.setStatus(0);
        }
        
        return courseDao.insert(course) > 0;
    }

    @Override
    @Transactional
    public boolean updateCourse(Course course) {
        // 设置更新时间
        course.setUpdateTime(new Date());
        
        return courseDao.update(course) > 0;
    }

    @Override
    @Transactional
    public boolean deleteCourse(Long id) {
        return courseDao.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteCourses(Long[] ids) {
        return courseDao.batchDelete(ids) > 0;
    }

    @Override
    @Transactional
    public boolean updateCourseStatus(Long id, Integer status) {
        return courseDao.updateStatus(id, status) > 0;
    }
    
    @Override
    public List<Course> getCoursesByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return courseDao.findByPage(offset, pageSize);
    }
    
    @Override
    public int getCourseCount() {
        return courseDao.getCount();
    }
    
    @Override
    public List<Course> searchCourses(String keyword) {
        return courseDao.searchCourses(keyword);
    }
    
    @Override
    public boolean isCourseNoExists(String courseNo) {
        return courseDao.isCourseCodeExists(courseNo);
    }
    
    @Override
    public Map<String, Object> getCourseStats() {
        // 这个方法可能需要多个DAO调用来构建完整的统计信息
        Map<String, Object> stats = new HashMap<>();
        
        // 课程总数
        stats.put("totalCourses", courseDao.getCount());
        
        // 根据状态统计
        stats.put("activeCourses", getActiveCourseCount());
        stats.put("inactiveCourses", getInactiveCourseCount());
        
        // 其他可能的统计信息
        stats.put("departmentCount", getDepartmentCount());
        stats.put("averageCredits", getAverageCredits());
        
        return stats;
    }
    
    @Override
    public List<Map<String, Object>> getCourseTypeStats() {
        // 调用DAO层方法获取课程类型统计
        return courseDao.getCourseTypeStats();
    }
    
    @Override
    public List<Map<String, Object>> getCollegeCourseStats() {
        // 假设学院对应department
        return courseDao.getDepartmentStats();
    }
    
    @Override
    public List<Map<String, Object>> getCreditDistribution() {
        // 调用DAO层方法获取学分分布统计
        return courseDao.getCreditDistribution();
    }
    
    @Override
    public List<Map<String, Object>> getCourseStatusStats() {
        // 调用DAO层方法获取课程状态统计
        return courseDao.getStatusStats();
    }
    
    // 辅助方法
    
    private int getActiveCourseCount() {
        // 假设状态为1表示激活状态
        return courseDao.getStatusCount(1);
    }
    
    private int getInactiveCourseCount() {
        // 假设状态为0表示非激活状态
        return courseDao.getStatusCount(0);
    }
    
    private int getDepartmentCount() {
        // 获取不同学院/部门的数量
        return courseDao.getDepartmentCount();
    }
    
    private double getAverageCredits() {
        // 获取平均学分
        return courseDao.getAverageCredits();
    }
}