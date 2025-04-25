package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.CourseDao;
import com.campus.dao.ScoreDao;
import com.campus.dao.UserDao;
import com.campus.entity.Course;
import com.campus.entity.Score;
import com.campus.entity.User;
import com.campus.service.ScoreService;
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
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreDao, Score> implements ScoreService {

    @Autowired
    private ScoreDao scoreDao;
    
    @Autowired
    private CourseDao courseDao;

    @Autowired
    private UserDao userDao;

    @Override
    @Transactional
    public boolean recordScore(Score score) {
        // 计算总分
        if (score.getRegularScore() != null && score.getMidtermScore() != null && score.getFinalScore() != null) {
            // 假设比例为：平时成绩30%，期中成绩30%，期末成绩40%
            BigDecimal regularWeight = new BigDecimal("0.3");
            BigDecimal midtermWeight = new BigDecimal("0.3");
            BigDecimal finalWeight = new BigDecimal("0.4");

            BigDecimal totalScore = score.getRegularScore().multiply(regularWeight)
                    .add(score.getMidtermScore().multiply(midtermWeight))
                    .add(score.getFinalScore().multiply(finalWeight));

            // 保留2位小数
            totalScore = totalScore.setScale(2, RoundingMode.HALF_UP);
            score.setTotalScore(totalScore);
        }

        return this.save(score);
    }

    @Override
    @Transactional
    public boolean updateScore(Score score) {
        Score existingScore = this.getById(score.getId());
        if (existingScore == null) {
            return false;
        }

        // 计算总分
        if (score.getRegularScore() != null && score.getMidtermScore() != null && score.getFinalScore() != null) {
            // 假设比例为：平时成绩30%，期中成绩30%，期末成绩40%
            BigDecimal regularWeight = new BigDecimal("0.3");
            BigDecimal midtermWeight = new BigDecimal("0.3");
            BigDecimal finalWeight = new BigDecimal("0.4");

            BigDecimal totalScore = score.getRegularScore().multiply(regularWeight)
                    .add(score.getMidtermScore().multiply(midtermWeight))
                    .add(score.getFinalScore().multiply(finalWeight));

            // 保留2位小数
            totalScore = totalScore.setScale(2, RoundingMode.HALF_UP);
            score.setTotalScore(totalScore);
        }

        return this.updateById(score);
    }

    @Override
    @Transactional
    public boolean deleteScore(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean batchDeleteScores(Long[] ids) {
        List<Long> idList = Arrays.asList(ids);
        return this.removeByIds(idList);
    }

    @Override
    public Score getStudentCourseScore(Long studentId, Long courseId, String termInfo) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getStudentId, studentId)
                .eq(Score::getCourseId, courseId);

        if (termInfo != null && !termInfo.isEmpty()) {
            queryWrapper.eq(Score::getTermInfo, termInfo);
        }

        return scoreDao.selectOne(queryWrapper);
    }

    @Override
    public Score getScoreBySelectionId(Long selectionId) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getSelectionId, selectionId);
        return scoreDao.selectOne(queryWrapper);
    }

    @Override
    public List<Score> getStudentScores(Long studentId) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getStudentId, studentId);
        List<Score> scoreList = scoreDao.selectList(queryWrapper);

        if (scoreList == null || scoreList.isEmpty()) {
            return new ArrayList<>(); // 如果没有找到成绩，返回空列表
        }

        // 获取学生信息
        User student = userDao.selectById(studentId);
        String studentName = student != null ? student.getUsername() : "";

        // 提取课程 ID
        List<Long> courseIds = scoreList.stream()
                .map(Score::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 如果存在课程 ID，则获取对应的课程信息
        final Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courseList = courseDao.selectBatchIds(courseIds);
            courseMap.putAll(courseList.stream()
                    .collect(Collectors.toMap(Course::getId, course -> course)));
        }

        // 处理成绩对象，填充额外信息
        for (Score score : scoreList) {
            // 设置学生名称
            score.setStudentName(studentName);

            // 设置课程名称
            Course course = courseMap.get(score.getCourseId());
            if (course != null) {
                score.setCourseName(course.getCourseName());
            }

            // 生成评论信息
            String comment = "";
            if (score.getRegularScore() != null) {
                comment += "平时成绩: " + score.getRegularScore() + ", ";
            }
            if (score.getMidtermScore() != null) {
                comment += "期中成绩: " + score.getMidtermScore() + ", ";
            }
            if (score.getFinalScore() != null) {
                comment += "期末成绩: " + score.getFinalScore();
            }
            score.setComment(comment);
        }

        return scoreList;
    }

    @Override
    public List<Score> getStudentTermScores(Long studentId, String termInfo) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getStudentId, studentId)
                .eq(Score::getTermInfo, termInfo);
        List<Score> scoreList = scoreDao.selectList(queryWrapper);

        if (scoreList == null || scoreList.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取学生信息
        User student = userDao.selectById(studentId);
        String studentName = student != null ? student.getUsername() : "";

        // 提取课程 ID
        List<Long> courseIds = scoreList.stream()
                .map(Score::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 获取课程信息
        final Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courseList = courseDao.selectBatchIds(courseIds);
            courseMap.putAll(courseList.stream()
                    .collect(Collectors.toMap(Course::getId, course -> course)));
        }

        // 处理成绩对象，填充额外信息
        for (Score score : scoreList) {
            // 设置学生名称
            score.setStudentName(studentName);

            // 设置课程名称
            Course course = courseMap.get(score.getCourseId());
            if (course != null) {
                score.setCourseName(course.getCourseName());
            }

            // 生成评论信息
            String comment = "";
            if (score.getRegularScore() != null) {
                comment += "平时成绩: " + score.getRegularScore() + ", ";
            }
            if (score.getMidtermScore() != null) {
                comment += "期中成绩: " + score.getMidtermScore() + ", ";
            }
            if (score.getFinalScore() != null) {
                comment += "期末成绩: " + score.getFinalScore();
            }
            score.setComment(comment);
        }

        return scoreList;
    }

    @Override
    public List<Score> getCourseScores(Long courseId) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getCourseId, courseId);
        return scoreDao.selectList(queryWrapper);
    }

    @Override
    public List<Score> getCourseTermScores(Long courseId, String termInfo) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getCourseId, courseId)
                .eq(Score::getTermInfo, termInfo);
        return scoreDao.selectList(queryWrapper);
    }

    @Override
    public Map<String, Object> getScoreStatistics(Long courseId, String termInfo) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getCourseId, courseId);

        if (termInfo != null && !termInfo.isEmpty()) {
            queryWrapper.eq(Score::getTermInfo, termInfo);
        }

        List<Score> scores = scoreDao.selectList(queryWrapper);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("count", scores.size());

        // 过滤掉没有总分的记录
        List<BigDecimal> validScores = scores.stream()
                .filter(score -> score.getTotalScore() != null)
                .map(Score::getTotalScore)
                .collect(Collectors.toList());

        statistics.put("validCount", validScores.size());

        if (!validScores.isEmpty()) {
        BigDecimal sum = BigDecimal.ZERO;
            BigDecimal max = validScores.get(0);
            BigDecimal min = validScores.get(0);

            for (BigDecimal score : validScores) {
                sum = sum.add(score);
                if (score.compareTo(max) > 0) {
                    max = score;
                }
                if (score.compareTo(min) < 0) {
                    min = score;
                }
            }

            // 计算平均分，保留2位小数
            BigDecimal average = sum.divide(new BigDecimal(validScores.size()), 2, RoundingMode.HALF_UP);

            statistics.put("average", average);
            statistics.put("max", max);
            statistics.put("min", min);

            // 计算及格率
            long passCount = validScores.stream()
                    .filter(score -> score.compareTo(new BigDecimal("60")) >= 0)
                    .count();

            BigDecimal passRate = new BigDecimal(passCount)
                    .divide(new BigDecimal(validScores.size()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            statistics.put("passRate", passRate);
        } else {
            statistics.put("average", BigDecimal.ZERO);
            statistics.put("max", BigDecimal.ZERO);
            statistics.put("min", BigDecimal.ZERO);
            statistics.put("passRate", BigDecimal.ZERO);
        }

        return statistics;
    }

    @Override
    public double calculateStudentGPA(Long studentId, String termInfo) {
        LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Score::getStudentId, studentId);

        if (termInfo != null && !termInfo.isEmpty()) {
            queryWrapper.eq(Score::getTermInfo, termInfo);
        }

        List<Score> scores = scoreDao.selectList(queryWrapper);

        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }

        // 过滤掉没有总分的记录
        List<Score> validScores = scores.stream()
                .filter(score -> score.getTotalScore() != null)
                .collect(Collectors.toList());

        if (validScores.isEmpty()) {
            return 0.0;
        }

        double totalGradePoints = 0.0;
        int totalCredits = 0;

        for (Score score : validScores) {
            // 获取课程学分
            Course course = courseDao.selectById(score.getCourseId());
            if (course == null || course.getCredit() == null) {
                continue;
            }

            int credit = course.getCredit().intValue();
            double gradePoint = calculateGradePoint(score.getTotalScore());

            totalGradePoints += gradePoint * credit;
            totalCredits += credit;
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        // 保留2位小数
        return Math.round((totalGradePoints / totalCredits) * 100.0) / 100.0;
    }

    /**
     * 根据分数计算绩点
     * 分数 -> 绩点对应关系：
     * 90-100 -> 4.0
     * 85-89 -> 3.7
     * 80-84 -> 3.3
     * 75-79 -> 3.0
     * 70-74 -> 2.7
     * 65-69 -> 2.3
     * 60-64 -> 2.0
     * <60 -> 0
     */
    private double calculateGradePoint(BigDecimal scoreValue) {
        if (scoreValue == null) {
            return 0.0;
        }

        double score = scoreValue.doubleValue();

        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 80) return 3.3;
        if (score >= 75) return 3.0;
        if (score >= 70) return 2.7;
        if (score >= 65) return 2.3;
        if (score >= 60) return 2.0;
        return 0.0;
    }

    @Override
    @Transactional
    public boolean batchRecordScores(List<Score> scores) {
        if (scores == null || scores.isEmpty()) {
            return false;
        }

        for (Score score : scores) {
            // 计算总分
        if (score.getRegularScore() != null && score.getMidtermScore() != null && score.getFinalScore() != null) {
            // 假设比例为：平时成绩30%，期中成绩30%，期末成绩40%
            BigDecimal regularWeight = new BigDecimal("0.3");
            BigDecimal midtermWeight = new BigDecimal("0.3");
            BigDecimal finalWeight = new BigDecimal("0.4");

            BigDecimal totalScore = score.getRegularScore().multiply(regularWeight)
                    .add(score.getMidtermScore().multiply(midtermWeight))
                    .add(score.getFinalScore().multiply(finalWeight));

            // 保留2位小数
            totalScore = totalScore.setScale(2, RoundingMode.HALF_UP);
            score.setTotalScore(totalScore);
        }
        }

        return this.saveBatch(scores);
    }
}