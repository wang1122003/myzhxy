package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.CourseDao;
import com.campus.entity.Course;
import com.campus.exception.CustomException;
import com.campus.service.CourseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    public Course getCourseByCourseCode(String courseCode) {
        if (!StringUtils.isNotBlank(courseCode)) return null;
        return getOne(Wrappers.<Course>lambdaQuery().eq(Course::getCourseCode, courseCode));
    }

    @Override
    public List<Course> getCoursesByCourseType(Integer courseType) {
        if (courseType == null) return list();
        return this.list(Wrappers.<Course>lambdaQuery().eq(Course::getCourseType, courseType));
    }

    @Override
    @Transactional
    public boolean addCourse(Course course) {
        validateCourse(course);

        if (baseMapper.countByCourseCode(course.getCourseCode()) > 0) {
            throw new CustomException("课程代码 '" + course.getCourseCode() + "' 已存在");
        }

        Date now = new Date();
        course.setCreateTime(now);
        course.setUpdateTime(now);
        course.setStatus(StringUtils.isBlank(course.getStatus()) ? "1" : course.getStatus());

        return this.save(course);
    }

    @Override
    @Transactional
    public boolean updateCourse(Course course) {
        if (course == null || course.getId() == null) {
            throw new CustomException("更新课程时必须提供课程ID");
        }
        validateCourse(course);

        LambdaQueryWrapper<Course> wrapper = Wrappers.<Course>lambdaQuery()
                .eq(Course::getCourseCode, course.getCourseCode())
                .ne(Course::getId, course.getId());
        if (this.count(wrapper) > 0) {
            throw new CustomException("课程代码 '" + course.getCourseCode() + "' 已被其他课程使用");
        }

        course.setUpdateTime(new Date());
        if (!StringUtils.isNotBlank(course.getStatus())) {
            Course existing = getById(course.getId());
            course.setStatus(existing != null ? existing.getStatus() : "1");
        }

        return this.updateById(course);
    }

    @Override
    @Transactional
    public boolean deleteCourse(Long id) {
        if (id == null) return false;
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteCourses(Long[] ids) {
        if (ids == null || ids.length == 0) return true;
        return this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public IPage<Course> getCoursesByPage(int pageNum, int pageSize) {
        IPage<Course> page = new Page<>(pageNum, pageSize);
        return this.page(page, Wrappers.<Course>lambdaQuery().orderByAsc(Course::getCourseCode));
    }

    @Override
    public IPage<Course> getAllCourses(int pageNum, int pageSize) {
        return getCoursesByPage(pageNum, pageSize);
    }

    @Override
    public int getCourseCount() {
        return (int) this.count();
    }

    @Override
    public List<Course> searchCourses(String keyword) {
        if (!StringUtils.isNotBlank(keyword)) {
            return list(Wrappers.<Course>lambdaQuery().orderByAsc(Course::getCourseCode));
        }
        return baseMapper.searchCoursesByKeyword(keyword);
    }

    @Override
    public boolean checkCourseCodeExists(String courseCode) {
        if (!StringUtils.isNotBlank(courseCode)) return false;
        return baseMapper.countByCourseCode(courseCode) > 0;
    }

    @Override
    @Transactional
    public boolean updateCourseStatus(Long id, String status) {
        if (id == null || !StringUtils.isNotBlank(status)) {
            throw new IllegalArgumentException("课程ID和状态不能为空");
        }

        LambdaUpdateWrapper<Course> updateWrapper = Wrappers.<Course>lambdaUpdate()
                .eq(Course::getId, id)
                .set(Course::getStatus, status)
                .set(Course::getUpdateTime, new Date());
        return this.update(updateWrapper);
    }

    private void validateCourse(Course course) {
        if (course == null) {
            throw new CustomException("课程信息不能为空");
        }
        if (StringUtils.isBlank(course.getCourseCode())) {
            throw new CustomException("课程代码不能为空");
        }
        if (StringUtils.isBlank(course.getCourseName())) {
            throw new CustomException("课程名称不能为空");
        }
        if (course.getCredit() == null || course.getCredit().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("学分不能为空且必须为非负数");
        }
    }
}