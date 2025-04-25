package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.CourseSelectionDao;
import com.campus.entity.CourseSelection;
import com.campus.service.CourseSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 选课信息服务实现类
 * </p>
 */
@Service
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionDao, CourseSelection> implements CourseSelectionService {

    @Autowired
    private CourseSelectionDao courseSelectionDao;

    @Override
    public List<CourseSelection> getByStudentId(Long userId) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, userId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getByStudentIdMaps(Long userId) {
        QueryWrapper<CourseSelection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return this.listMaps(queryWrapper);
    }

    @Override
    public List<CourseSelection> getByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getCourseId, courseId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getByCourseIdMaps(Long courseId) {
        QueryWrapper<CourseSelection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        return this.listMaps(queryWrapper);
    }

    @Override
    @Transactional
    public boolean selectCourse(CourseSelection courseSelection) {
        // 检查是否已选过该课程
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, courseSelection.getUserId())
                .eq(CourseSelection::getCourseId, courseSelection.getCourseId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            return false; // 已选过该课程
        }

        // 设置选课时间和状态
        courseSelection.setSelectionTime(new Date());
        courseSelection.setStatus("1"); // Assuming "1" represents Selected
        courseSelection.setCreateTime(new Date());
        courseSelection.setUpdateTime(new Date());

        return this.save(courseSelection);
    }

    @Override
    @Transactional
    public boolean selectCourseByMap(Map<String, Object> courseSelectionMap) {
        CourseSelection courseSelection = new CourseSelection();
        courseSelection.setUserId(Long.parseLong(courseSelectionMap.get("userId").toString()));
        courseSelection.setCourseId(Long.parseLong(courseSelectionMap.get("courseId").toString()));
        courseSelection.setTermInfo(courseSelectionMap.get("termInfo").toString());

        return this.selectCourse(courseSelection);
    }

    @Override
    @Transactional
    public boolean dropCourse(Long userId, Long courseId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, userId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getTermInfo, termInfo);
        return this.remove(queryWrapper);
    }

    @Override
    @Transactional
    public boolean updateScore(CourseSelection courseSelection) {
        // 获取原始数据
        CourseSelection original = this.getById(courseSelection.getId());
        if (original == null) {
            return false;
        }

        // 更新成绩相关字段
        original.setRegularScore(courseSelection.getRegularScore());
        original.setMidtermScore(courseSelection.getMidtermScore());
        original.setFinalExamScore(courseSelection.getFinalExamScore());
        original.setScoreValue(courseSelection.getScoreValue());
        original.setGrade(courseSelection.getGrade());
        original.setGpa(courseSelection.getGpa());
        original.setComment(courseSelection.getComment());
        original.setEvaluationDate(courseSelection.getEvaluationDate() != null ?
                courseSelection.getEvaluationDate() : new Date());
        original.setUpdateTime(new Date());

        return this.updateById(original);
    }

    @Override
    @Transactional
    public boolean updateScoreByMap(Map<String, Object> scoreMap) {
        Long id = Long.parseLong(scoreMap.get("id").toString());
        CourseSelection courseSelection = this.getById(id);
        if (courseSelection == null) {
            return false;
        }

        if (scoreMap.containsKey("regularScore")) {
            courseSelection.setRegularScore(new BigDecimal(scoreMap.get("regularScore").toString()));
        }
        if (scoreMap.containsKey("midtermScore")) {
            courseSelection.setMidtermScore(new BigDecimal(scoreMap.get("midtermScore").toString()));
        }
        if (scoreMap.containsKey("finalExamScore")) {
            courseSelection.setFinalExamScore(new BigDecimal(scoreMap.get("finalExamScore").toString()));
        }
        if (scoreMap.containsKey("scoreValue")) {
            courseSelection.setScoreValue(new BigDecimal(scoreMap.get("scoreValue").toString()));
        }
        if (scoreMap.containsKey("grade")) {
            courseSelection.setGrade(scoreMap.get("grade").toString());
        }
        if (scoreMap.containsKey("gpa")) {
            courseSelection.setGpa(new BigDecimal(scoreMap.get("gpa").toString()));
        }
        if (scoreMap.containsKey("comment")) {
            courseSelection.setComment(scoreMap.get("comment").toString());
        }
        if (scoreMap.containsKey("evaluationDate")) {
            // 根据输入格式转换日期
            courseSelection.setEvaluationDate(new Date()); // 简化处理，实际应解析日期字符串
        } else {
            courseSelection.setEvaluationDate(new Date());
        }
        courseSelection.setUpdateTime(new Date());

        return this.updateById(courseSelection);
    }

    @Override
    public Map<String, Object> pageQuery(Long current, Long size, Map<String, Object> params) {
        Page<CourseSelection> page = new Page<>(current, size);
        QueryWrapper<CourseSelection> queryWrapper = new QueryWrapper<>();

        // 添加查询条件
        if (params.containsKey("userId")) {
            queryWrapper.eq("user_id", params.get("userId"));
        }
        if (params.containsKey("courseId")) {
            queryWrapper.eq("course_id", params.get("courseId"));
        }
        if (params.containsKey("termInfo")) {
            queryWrapper.eq("term_info", params.get("termInfo"));
        }
        if (params.containsKey("status")) {
            queryWrapper.eq("status", params.get("status"));
        }

        // 排序
        queryWrapper.orderByDesc("update_time");

        // 执行分页查询
        IPage<CourseSelection> pageResult = this.page(page, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        result.put("pages", pageResult.getPages());

        return result;
    }

    @Override
    public Map<String, Object> getMapById(Long id) {
        return this.getMap(new QueryWrapper<CourseSelection>().eq("id", id));
    }

    @Override
    @Transactional
    public boolean saveByMap(Map<String, Object> courseSelectionMap) {
        CourseSelection courseSelection = new CourseSelection();

        if (courseSelectionMap.containsKey("userId")) {
            courseSelection.setUserId(Long.parseLong(courseSelectionMap.get("userId").toString()));
        }
        if (courseSelectionMap.containsKey("courseId")) {
            courseSelection.setCourseId(Long.parseLong(courseSelectionMap.get("courseId").toString()));
        }
        if (courseSelectionMap.containsKey("termInfo")) {
            courseSelection.setTermInfo(courseSelectionMap.get("termInfo").toString());
        }
        if (courseSelectionMap.containsKey("status")) {
            courseSelection.setStatus(courseSelectionMap.get("status").toString());
        } else {
            courseSelection.setStatus("1"); // 默认状态 "1"
        }

        courseSelection.setSelectionTime(new Date());
        courseSelection.setCreateTime(new Date());
        courseSelection.setUpdateTime(new Date());

        return this.save(courseSelection);
    }

    @Override
    @Transactional
    public boolean updateByMap(Map<String, Object> courseSelectionMap) {
        if (!courseSelectionMap.containsKey("id")) {
            return false;
        }

        Long id = Long.parseLong(courseSelectionMap.get("id").toString());
        CourseSelection courseSelection = this.getById(id);
        if (courseSelection == null) {
            return false;
        }

        if (courseSelectionMap.containsKey("userId")) {
            courseSelection.setUserId(Long.parseLong(courseSelectionMap.get("userId").toString()));
        }
        if (courseSelectionMap.containsKey("courseId")) {
            courseSelection.setCourseId(Long.parseLong(courseSelectionMap.get("courseId").toString()));
        }
        if (courseSelectionMap.containsKey("termInfo")) {
            courseSelection.setTermInfo(courseSelectionMap.get("termInfo").toString());
        }
        if (courseSelectionMap.containsKey("status")) {
            courseSelection.setStatus(courseSelectionMap.get("status").toString());
        }
        if (courseSelectionMap.containsKey("regularScore")) {
            courseSelection.setRegularScore(new BigDecimal(courseSelectionMap.get("regularScore").toString()));
        }
        if (courseSelectionMap.containsKey("midtermScore")) {
            courseSelection.setMidtermScore(new BigDecimal(courseSelectionMap.get("midtermScore").toString()));
        }
        if (courseSelectionMap.containsKey("finalExamScore")) {
            courseSelection.setFinalExamScore(new BigDecimal(courseSelectionMap.get("finalExamScore").toString()));
        }
        if (courseSelectionMap.containsKey("scoreValue")) {
            courseSelection.setScoreValue(new BigDecimal(courseSelectionMap.get("scoreValue").toString()));
        }
        if (courseSelectionMap.containsKey("grade")) {
            courseSelection.setGrade(courseSelectionMap.get("grade").toString());
        }
        if (courseSelectionMap.containsKey("gpa")) {
            courseSelection.setGpa(new BigDecimal(courseSelectionMap.get("gpa").toString()));
        }

        courseSelection.setUpdateTime(new Date());

        return this.updateById(courseSelection);
    }

    @Override
    public boolean isCourseTaken(Long userId, Long courseId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, userId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getTermInfo, termInfo)
                .eq(CourseSelection::getStatus, "1"); // Assuming "1" represents Selected
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<CourseSelection> getByStudentIdAndTerm(Long userId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getUserId, userId)
                .eq(CourseSelection::getTermInfo, termInfo);
        return this.list(queryWrapper);
    }

    @Override
    public List<CourseSelection> getByCourseIdAndTerm(Long courseId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getTermInfo, termInfo);
        return this.list(queryWrapper);
    }

    @Override
    public int getCourseSelectionCount(Long courseId, String termInfo) {
        LambdaQueryWrapper<CourseSelection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getTermInfo, termInfo)
                .eq(CourseSelection::getStatus, "1"); // Assuming "1" represents Selected
        return Math.toIntExact(this.count(queryWrapper));
    }
} 