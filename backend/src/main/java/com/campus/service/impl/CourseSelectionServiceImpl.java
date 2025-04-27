package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.CourseSelectionDao;
import com.campus.dao.CourseDao;
import com.campus.dao.UserDao;
import com.campus.entity.CourseSelection;
import com.campus.entity.Course;
import com.campus.entity.User;
import com.campus.exception.CustomException;
import com.campus.service.CourseSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * <p>
 * 选课信息服务实现类
 * </p>
 */
@Service
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionDao, CourseSelection> implements CourseSelectionService {

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private UserDao userDao;

    @Override
    public List<CourseSelection> getByStudentId(Long userId) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, userId);
        List<CourseSelection> selections = this.list(queryWrapper);
        fillAssociationInfo(selections);
        return selections;
    }

    @Override
    public List<CourseSelection> getByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getCourseId, courseId);
        List<CourseSelection> selections = this.list(queryWrapper);
        fillAssociationInfo(selections);
        return selections;
    }

    @Override
    @Transactional
    public boolean selectCourse(CourseSelection courseSelection) {
        if (courseSelection == null || courseSelection.getUserId() == null || courseSelection.getCourseId() == null || courseSelection.getTermInfo() == null) {
            throw new IllegalArgumentException("学生ID、课程ID和学期信息不能为空");
        }
        if (isCourseTaken(courseSelection.getUserId(), courseSelection.getCourseId(), courseSelection.getTermInfo())) {
            throw new CustomException("您已选修过该学期的此门课程");
        }

        courseSelection.setSelectionTime(new Date());
        courseSelection.setStatus("1");
        courseSelection.setCreateTime(new Date());
        courseSelection.setUpdateTime(new Date());
        courseSelection.setRegularScore(null);
        courseSelection.setMidtermScore(null);
        courseSelection.setFinalExamScore(null);
        courseSelection.setScoreValue(null);
        courseSelection.setGrade(null);
        courseSelection.setGpa(null);
        courseSelection.setComment(null);
        courseSelection.setEvaluationDate(null);

        return this.save(courseSelection);
    }

    @Override
    @Transactional
    public boolean dropCourse(Long userId, Long courseId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, userId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getTermInfo, termInfo);
        if (this.count(queryWrapper) == 0) {
            throw new CustomException("未找到对应的选课记录");
        }
        CourseSelection selection = this.getOne(queryWrapper);
        if (selection.getScoreValue() != null || selection.getGrade() != null) {
            throw new CustomException("已录入成绩的课程不能退选");
        }
        return this.remove(queryWrapper);
    }

    @Override
    public IPage<CourseSelection> pageQuery(Long current, Long size, Map<String, Object> params) {
        Page<CourseSelection> page = new Page<>(current, size);
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();

        if (params.containsKey("userId")) {
            queryWrapper.eq(CourseSelection::getUserId, params.get("userId"));
        }
        if (params.containsKey("courseId")) {
            queryWrapper.eq(CourseSelection::getCourseId, params.get("courseId"));
        }
        if (params.containsKey("termInfo")) {
            queryWrapper.eq(CourseSelection::getTermInfo, params.get("termInfo"));
        }
        if (params.containsKey("status")) {
            queryWrapper.eq(CourseSelection::getStatus, params.get("status"));
        }

        queryWrapper.orderByDesc(CourseSelection::getUpdateTime);

        IPage<CourseSelection> pageResult = this.page(page, queryWrapper);
        fillAssociationInfo(pageResult.getRecords());
        return pageResult;
    }

    @Override
    public boolean isCourseTaken(Long userId, Long courseId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, userId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getTermInfo, termInfo);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<CourseSelection> getByStudentIdAndTerm(Long userId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, userId)
                .eq(CourseSelection::getTermInfo, termInfo);
        List<CourseSelection> selections = this.list(queryWrapper);
        fillAssociationInfo(selections);
        return selections;
    }

    @Override
    public List<CourseSelection> getByCourseIdAndTerm(Long courseId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getTermInfo, termInfo);
        List<CourseSelection> selections = this.list(queryWrapper);
        fillAssociationInfo(selections);
        return selections;
    }

    @Override
    public int getCourseSelectionCount(Long courseId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getTermInfo, termInfo);
        return (int) this.count(queryWrapper);
    }

    private void fillAssociationInfo(List<CourseSelection> selections) {
        if (selections == null || selections.isEmpty()) {
            return;
        }
        Set<Long> courseIds = selections.stream().map(CourseSelection::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = selections.stream().map(CourseSelection::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            courseMap = courseDao.selectBatchIds(courseIds).stream().collect(Collectors.toMap(Course::getId, c -> c));
        }
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = userDao.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        for (CourseSelection selection : selections) {
            if (selection.getCourseId() != null) {
                Course course = courseMap.get(selection.getCourseId());
                if (course != null) {
                    selection.setCourseName(course.getCourseName());
                }
            }
            if (selection.getUserId() != null) {
                User user = userMap.get(selection.getUserId());
                if (user != null) {
                    selection.setStudentName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }
        }
    }
} 