package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.CourseDao;
import com.campus.dao.ScoreDao;
import com.campus.entity.Course;
import com.campus.entity.Score;
import com.campus.service.ScoreService;
import com.campus.vo.ScoreVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成绩服务实现类 (重构后)
 */
@Slf4j
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreDao, Score> implements ScoreService {

    @Autowired
    private ScoreDao scoreDao;
    
    @Autowired
    private CourseDao courseDao;

    @Override
    @Transactional
    public boolean recordScore(Score score) {
        // 检查该学生和课程的成绩是否已存在
        Score existingScore = getStudentCourseScore(score.getStudentId(), score.getCourseId());
        if (existingScore != null) {
            // 更新已存在的成绩
            score.setId(existingScore.getId());
            return updateScore(score);
        }
        // 如果提供了组成部分，则计算总分 (示例逻辑)
        calculateAndSetTotalScore(score);
        score.setCreateTime(new Date());
        score.setUpdateTime(new Date());
        return save(score);
    }

    @Override
    @Transactional
    public boolean updateScore(Score score) {
        // 确保设置了更新时间
        score.setUpdateTime(new Date());
        // 更新前重新计算总分
        calculateAndSetTotalScore(score);
        return updateById(score);
    }

    @Override
    @Transactional
    public boolean deleteScore(Long id) {
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteScores(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    public Score getStudentCourseScore(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            return null;
        }
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getStudentId, studentId)
                .eq(Score::getCourseId, courseId)
                .last("LIMIT 1"); // 确保只返回一条记录
        return getOne(queryWrapper);
    }

    @Override
    public List<ScoreVO> getStudentScores(Long studentId) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getStudentId, studentId);
        List<Score> scoreList = scoreDao.selectList(queryWrapper);

        if (scoreList == null || scoreList.isEmpty()) {
            return new ArrayList<>(); // 如果没有找到成绩，返回空列表
        }

        // 提取课程 ID
        List<Long> courseIds = scoreList.stream()
                .map(Score::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 如果存在课程 ID，则获取对应的课程信息
        Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courseList = courseDao.selectBatchIds(courseIds);
            courseMap = courseList.stream()
                    .collect(Collectors.toMap(Course::getId, course -> course));
        }

        // 创建 ScoreVO 列表
        List<ScoreVO> scoreVOList = new ArrayList<>();
        for (Score score : scoreList) {
            Course course = courseMap.get(score.getCourseId());
            ScoreVO scoreVO = new ScoreVO(score, course); // 使用构造函数创建 VO
            scoreVOList.add(scoreVO);
        }

        return scoreVOList;
    }

    @Override
    public List<Score> getCourseScores(Long courseId) {
        if (courseId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getCourseId, courseId);
        // 可以考虑添加排序，例如按学生 ID 或分数排序
        // queryWrapper.orderByDesc(Score::getScore);
        return list(queryWrapper);
    }

    @Override
    public Map<String, Object> getScoreStatistics(Long courseId) {
        List<Score> scores = getCourseScores(courseId);
        Map<String, Object> stats = new HashMap<>();
        if (scores.isEmpty()) {
            stats.put("average", 0);
            stats.put("max", 0);
            stats.put("min", 0);
            stats.put("count", 0);
            return stats;
        }

        // 使用 BigDecimal 进行计算
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = scores.get(0).getTotalScore() != null ? scores.get(0).getTotalScore() : BigDecimal.ZERO;
        BigDecimal min = scores.get(0).getTotalScore() != null ? scores.get(0).getTotalScore() : BigDecimal.ZERO;

        for (Score s : scores) {
            BigDecimal currentScore = s.getTotalScore() != null ? s.getTotalScore() : BigDecimal.ZERO;
            sum = sum.add(currentScore);
            if (currentScore.compareTo(max) > 0) {
                max = currentScore;
            }
            if (currentScore.compareTo(min) < 0) {
                min = currentScore;
            }
        }

        BigDecimal average = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP); // 保留两位小数

        stats.put("average", average);
        stats.put("max", max);
        stats.put("min", min);
        stats.put("count", scores.size());
        return stats;
    }

    // 辅助方法：根据组成部分计算总分
    private void calculateAndSetTotalScore(Score score) {
        if (score.getRegularScore() != null && score.getMidtermScore() != null && score.getFinalScore() != null) {
            // 示例权重: 平时 30%, 期中 30%, 期末 40%
            BigDecimal regularWeight = new BigDecimal("0.3");
            BigDecimal midtermWeight = new BigDecimal("0.3");
            BigDecimal finalWeight = new BigDecimal("0.4");

            BigDecimal total = score.getRegularScore().multiply(regularWeight)
                    .add(score.getMidtermScore().multiply(midtermWeight))
                    .add(score.getFinalScore().multiply(finalWeight));
            score.setTotalScore(total.setScale(2, RoundingMode.HALF_UP)); // 保留两位小数
        } else if (score.getTotalScore() == null) {
            // 如果组成部分缺失且总分为 null，则设置默认值或作为错误处理
            score.setTotalScore(BigDecimal.ZERO); // 或者抛出异常，或者根据需求保持 null
        }
        // 如果 totalScore 已设置但组成部分缺失，则保持 totalScore 不变。
    }

    // 辅助方法占位符 (如果统计需要)
    // private long countStudentsInCourse(Long courseId) { ... }
}