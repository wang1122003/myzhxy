package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.CourseDao;
import com.campus.entity.Course;
import com.campus.exception.CustomException;
import com.campus.service.CourseService;
import org.apache.commons.lang3.StringUtils;
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
public class CourseServiceImpl extends ServiceImpl<CourseDao, Course> implements CourseService {

    @Override
    public Course getCourseById(Long id) {
        return this.getById(id);
    }

    @Override
    public Course getCourseByCourseNo(String courseNo) {
        return baseMapper.findByCourseNo(courseNo);
    }

    @Override
    public IPage<Course> getAllCourses(int pageNum, int pageSize) {
        IPage<Course> page = new Page<>(pageNum, pageSize);
        return this.page(page);
    }

    @Override
    public List<Course> getCoursesByCourseType(Integer courseType) {
        return this.list(new LambdaQueryWrapper<Course>().eq(Course::getCourseType, courseType));
    }

    @Override
    public List<Course> getCoursesByCollegeId(Long collegeId) {
        return baseMapper.findByDepartment(String.valueOf(collegeId));
    }

    @Override
    @Transactional
    public boolean addCourse(Course course) {
        validateCourse(course);

        long count = this.count(new LambdaQueryWrapper<Course>().eq(Course::getCourseNo, course.getCourseNo()));
        if (count > 0) {
            throw new CustomException("课程代码 '" + course.getCourseNo() + "' 已存在");
        }

        Date now = new Date();
        course.setCreateTime(now);
        course.setUpdateTime(now);
        if (course.getStatus() == null) {
            course.setStatus(1);
        }

        return this.save(course);
    }

    @Override
    @Transactional
    public boolean updateCourse(Course course) {
        if (course.getId() == null) {
            throw new CustomException("更新课程时必须提供课程ID");
        }
        validateCourse(course);

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getCourseNo, course.getCourseNo());
        wrapper.ne(Course::getId, course.getId());
        if (this.count(wrapper) > 0) {
            throw new CustomException("课程代码 '" + course.getCourseNo() + "' 已被其他课程使用");
        }

        course.setUpdateTime(new Date());
        if (course.getStatus() == null) {
            course.setStatus(1);
        }

        return this.updateById(course);
    }

    @Override
    @Transactional
    public boolean deleteCourse(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteCourses(Long[] ids) {
        return this.removeByIds(List.of(ids));
    }

    @Override
    @Transactional
    public boolean updateCourseStatus(Long id, Integer status) {
        return baseMapper.updateStatus(id, status) > 0;
    }
    
    @Override
    public IPage<Course> getCoursesByPage(int pageNum, int pageSize) {
        IPage<Course> page = new Page<>(pageNum, pageSize);
        return this.page(page);
    }
    
    @Override
    public int getCourseCount() {
        return (int) this.count();
    }
    
    @Override
    public List<Course> searchCourses(String keyword) {
        return baseMapper.searchCourses(keyword);
    }
    
    @Override
    public boolean isCourseNoExists(String courseNo) {
        return this.count(new LambdaQueryWrapper<Course>().eq(Course::getCourseNo, courseNo)) > 0;
    }
    
    @Override
    public Map<String, Object> getCourseStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourses", (int) this.count());
        stats.put("activeCourses", getActiveCourseCount());
        stats.put("inactiveCourses", getInactiveCourseCount());
        stats.put("departmentCount", getDepartmentCount());
        stats.put("averageCredits", getAverageCredits());
        return stats;
    }
    
    @Override
    public List<Map<String, Object>> getCourseTypeStats() {
        return baseMapper.getCourseTypeStats();
    }
    
    @Override
    public List<Map<String, Object>> getCollegeCourseStats() {
        return baseMapper.getDepartmentStats();
    }
    
    @Override
    public List<Map<String, Object>> getCreditDistribution() {
        return baseMapper.getCreditDistribution();
    }
    
    @Override
    public List<Map<String, Object>> getCourseStatusStats() {
        return baseMapper.getStatusStats();
    }
    
    private int getActiveCourseCount() {
        return baseMapper.getStatusCount(1);
    }
    
    private int getInactiveCourseCount() {
        return baseMapper.getStatusCount(0);
    }
    
    private int getDepartmentCount() {
        return baseMapper.getDepartmentCount();
    }
    
    private double getAverageCredits() {
        return baseMapper.getAverageCredits();
    }

    private void validateCourse(Course course) {
        if (course == null) {
            throw new CustomException("课程信息不能为空");
        }
        if (StringUtils.isBlank(course.getCourseNo())) {
            throw new CustomException("课程代码不能为空");
        }
        if (StringUtils.isBlank(course.getCourseName())) {
            throw new CustomException("课程名称不能为空");
        }
        if (course.getCredit() == null || course.getCredit() < 0) {
            throw new CustomException("学分不能为空且必须为非负数");
        }
    }
}