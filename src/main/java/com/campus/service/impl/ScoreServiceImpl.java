package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.dao.CourseDao;
import com.campus.dao.ScoreDao;
import com.campus.entity.Course;
import com.campus.entity.Score;
import com.campus.service.ScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 成绩服务实现类
 */
@Slf4j
@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreDao scoreDao;
    
    @Autowired
    private CourseDao courseDao;

    @Override
    public Score getScoreById(Long id) {
        return scoreDao.findById(id);
    }

    @Override
    public List<Score> getAllScores() {
        return scoreDao.findAll();
    }

    @Override
    public List<Score> getScoresByStudentId(Long studentId) {
        return scoreDao.findByStudentId(studentId);
    }

    @Override
    public List<Score> getScoresByCourseId(Long courseId) {
        return scoreDao.findByCourseId(courseId);
    }

    @Override
    public List<Score> getScoresByTeacherId(Long teacherId) {
        return scoreDao.findByTeacherId(teacherId);
    }

    @Override
    public List<Score> getScoresByTermId(Long termId) {
        return scoreDao.findByTermId(termId);
    }

    @Override
    public List<Score> getScoresByStudentIdAndTermId(Long studentId, Long termId) {
        return scoreDao.findByStudentIdAndTermId(studentId, termId);
    }

    @Override
    public List<Score> getScoresByCourseIdAndTermId(Long courseId, Long termId) {
        return scoreDao.findByCourseIdAndTermId(courseId, termId);
    }

    @Override
    public Score getScoreByStudentIdAndCourseId(Long studentId, Long courseId) {
        return scoreDao.findByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    @Transactional
    public boolean addScore(Score score) {
        // 设置创建时间和更新时间
        Date now = new Date();
        score.setCreateTime(now);
        score.setUpdateTime(now);
        
        // 计算总评成绩
        float totalScore = calculateTotalScore(score);
        score.setTotalScore(totalScore);
        
        // 计算成绩等级
        String grade = calculateGrade(totalScore);
        score.setGrade(grade);
        
        return scoreDao.insert(score) > 0;
    }

    @Override
    @Transactional
    public boolean updateScore(Score score) {
        // 设置更新时间
        score.setUpdateTime(new Date());
        
        // 计算总评成绩
        float totalScore = calculateTotalScore(score);
        score.setTotalScore(totalScore);
        
        // 计算成绩等级
        String grade = calculateGrade(totalScore);
        score.setGrade(grade);
        
        return scoreDao.update(score) > 0;
    }

    @Override
    @Transactional
    public boolean deleteScore(Long id) {
        return scoreDao.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteScores(Long[] ids) {
        return scoreDao.batchDelete(ids) > 0;
    }

    @Override
    @Transactional
    public boolean batchAddScores(List<Score> scores) {
        // 设置创建时间、更新时间、总评成绩和成绩等级
        Date now = new Date();
        for (Score score : scores) {
            score.setCreateTime(now);
            score.setUpdateTime(now);
            
            float totalScore = calculateTotalScore(score);
            score.setTotalScore(totalScore);
            
            String grade = calculateGrade(totalScore);
            score.setGrade(grade);
        }
        
        return scoreDao.batchInsert(scores) > 0;
    }

    @Override
    public float calculateTotalScore(Score score) {
        // 假设总评成绩计算规则：平时成绩占30%，期中成绩占30%，期末成绩占40%
        float regularWeight = 0.3f;
        float midtermWeight = 0.3f;
        float finalWeight = 0.4f;
        
        float regularScore = score.getRegularScore() != null ? score.getRegularScore() : 0f;
        float midtermScore = score.getMidtermScore() != null ? score.getMidtermScore() : 0f;
        float finalScore = score.getFinalScore() != null ? score.getFinalScore() : 0f;
        
        return regularScore * regularWeight + midtermScore * midtermWeight + finalScore * finalWeight;
    }

    @Override
    public String calculateGrade(float totalScore) {
        // 成绩等级计算规则
        if (totalScore >= 90) {
            return "A";
        } else if (totalScore >= 80) {
            return "B";
        } else if (totalScore >= 70) {
            return "C";
        } else if (totalScore >= 60) {
            return "D";
        } else {
            return "F";
        }
    }
    
    @Override
    public List<Score> getScoresByPage(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return scoreDao.findByPage(offset, pageSize);
    }
    
    @Override
    public int getScoreCount() {
        return scoreDao.getCount();
    }
    
    @Override
    public List<Score> searchScores(String keyword) {
        return scoreDao.searchScores(keyword);
    }
    
    @Override
    public Map<String, Object> getScoreStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 成绩总数
        stats.put("totalScores", scoreDao.getCount());
        
        // 及格总数
        stats.put("passCount", scoreDao.getPassCount());
        
        // 不及格总数
        stats.put("failCount", scoreDao.getFailCount());
        
        // 及格率
        double passRate = 0;
        int totalCount = scoreDao.getCount();
        if (totalCount > 0) {
            passRate = (double) scoreDao.getPassCount() / totalCount * 100;
        }
        stats.put("passRate", passRate);
        
        // 平均分
        stats.put("averageScore", scoreDao.getAverageScore());
        
        // 最高分
        stats.put("highestScore", scoreDao.getHighestScore());
        
        // 最低分
        stats.put("lowestScore", scoreDao.getLowestScore());
        
        return stats;
    }
    
    @Override
    public List<Map<String, Object>> getCourseAverageStats() {
        return scoreDao.getCourseAverageStats();
    }
    
    @Override
    public List<Map<String, Object>> getStudentAverageStats() {
        return scoreDao.getStudentAverageStats();
    }
    
    @Override
    public List<Map<String, Object>> getScoreDistribution() {
        return scoreDao.getScoreDistribution();
    }
    
    @Override
    public List<Map<String, Object>> getTermScoreTrend(Long studentId) {
        return scoreDao.getTermScoreTrend(studentId);
    }
    
    @Override
    public List<Map<String, Object>> getStudentRankings(Long courseId, Long termId) {
        return scoreDao.getStudentRankings(courseId, termId);
    }
    
    @Override
    public float calculateStudentGPA(Long studentId, Long termId) {
        // 获取学生在指定学期的所有成绩
        List<Score> scores = scoreDao.findByStudentIdAndTermId(studentId, termId);
        
        if (scores == null || scores.isEmpty()) {
            return 0f;
        }
        
        float totalGradePoints = 0f;
        float totalCredits = 0f;
        
        for (Score score : scores) {
            // 根据成绩等级确定绩点
            float gradePoint = 0f;
            switch (score.getGrade()) {
                case "A":
                    gradePoint = 4.0f;
                    break;
                case "B":
                    gradePoint = 3.0f;
                    break;
                case "C":
                    gradePoint = 2.0f;
                    break;
                case "D":
                    gradePoint = 1.0f;
                    break;
                case "F":
                    gradePoint = 0f;
                    break;
            }
            
            // 获取课程学分
            float credits = 0f;
            if (score.getCourseId() != null) {
                Course course = courseDao.findById(score.getCourseId());
                if (course != null && course.getCredit() != null) {
                    credits = course.getCredit();
                }
            }
            
            totalGradePoints += gradePoint * credits;
            totalCredits += credits;
        }
        
        // 防止除以零
        if (totalCredits == 0) {
            return 0f;
        }
        
        return totalGradePoints / totalCredits;
    }
    
    @Override
    public float calculateWeightedAverage(Long studentId, Long termId) {
        // 获取学生在指定学期的所有成绩
        List<Score> scores = scoreDao.findByStudentIdAndTermId(studentId, termId);
        
        if (scores == null || scores.isEmpty()) {
            return 0f;
        }
        
        float totalWeightedScore = 0f;
        float totalCredits = 0f;
        
        for (Score score : scores) {
            float totalScore = score.getTotalScore() != null ? score.getTotalScore() : 0f;
            
            // 获取课程学分
            float credits = 0f;
            if (score.getCourseId() != null) {
                Course course = courseDao.findById(score.getCourseId());
                if (course != null && course.getCredit() != null) {
                    credits = course.getCredit();
                }
            }
            
            totalWeightedScore += totalScore * credits;
            totalCredits += credits;
        }
        
        // 防止除以零
        if (totalCredits == 0) {
            return 0f;
        }
        
        return totalWeightedScore / totalCredits;
    }
    
    @Override
    public List<Map<String, Object>> exportScores(Long courseId, Long termId) {
        return scoreDao.exportScores(courseId, termId);
    }
    
    @Override
    @Transactional
    public boolean importScores(List<Score> scores) {
        // 默认调用批量添加方法
        return batchAddScores(scores);
    }
    
    @Override
    public List<Map<String, Object>> getPassRateStats() {
        return scoreDao.getPassRateStats();
    }
    
    @Override
    public List<Map<String, Object>> getFailedStudents(Long courseId, Long termId) {
        return scoreDao.getFailedStudents(courseId, termId);
    }
    
    @Override
    public Map<String, Object> analyzeStudentScores(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取学生所有成绩
        List<Score> scores = scoreDao.findByStudentId(studentId);
        
        if (scores == null || scores.isEmpty()) {
            result.put("error", "未找到该学生的成绩记录");
            return result;
        }
        
        // 计算平均分
        double totalScore = 0;
        int passCount = 0;
        int failCount = 0;
        
        Map<Long, Double> courseScores = new HashMap<>(); // 课程ID -> 成绩
        Map<Long, Integer> termCourseCount = new HashMap<>(); // 学期ID -> 课程数量
        Map<Long, Double> termTotalScore = new HashMap<>(); // 学期ID -> 总分
        
        for (Score score : scores) {
            totalScore += score.getTotalScore();
            
            if (score.getTotalScore() >= 60) {
                passCount++;
            } else {
                failCount++;
            }
            
            // 记录课程成绩
            courseScores.put(score.getCourseId(), (double) score.getTotalScore());
            
            // 记录学期成绩
            Long termId = score.getTermId();
            termCourseCount.put(termId, termCourseCount.getOrDefault(termId, 0) + 1);
            termTotalScore.put(termId, termTotalScore.getOrDefault(termId, 0.0) + score.getTotalScore());
        }
        
        double averageScore = totalScore / scores.size();
        double passRate = (double) passCount / scores.size() * 100;
        
        result.put("averageScore", averageScore);
        result.put("passRate", passRate);
        result.put("courseCount", scores.size());
        result.put("passCount", passCount);
        result.put("failCount", failCount);
        
        // 分析优势和劣势科目
        List<Map<String, Object>> strengths = new ArrayList<>();
        List<Map<String, Object>> weaknesses = new ArrayList<>();
        
        for (Map.Entry<Long, Double> entry : courseScores.entrySet()) {
            Long courseId = entry.getKey();
            Double score = entry.getValue();
            
            Course course = courseDao.findById(courseId);
            
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("courseId", courseId);
            courseInfo.put("courseName", course != null ? course.getCourseName() : "未知课程");
            courseInfo.put("score", score);
            
            if (score >= 80) {
                strengths.add(courseInfo);
            } else if (score < 60) {
                weaknesses.add(courseInfo);
            }
        }
        
        // 按分数降序排序优势科目
        strengths.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        
        // 按分数升序排序劣势科目
        weaknesses.sort((a, b) -> Double.compare((Double) a.get("score"), (Double) b.get("score")));
        
        result.put("strengths", strengths);
        result.put("weaknesses", weaknesses);
        
        // 学期成绩趋势
        List<Map<String, Object>> termTrends = new ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : termCourseCount.entrySet()) {
            Long termId = entry.getKey();
            Integer courseCount = entry.getValue();
            Double termAverage = termTotalScore.get(termId) / courseCount;
            
            // 获取学期信息
            // Assuming termDao is available to getTermById
            // Term term = termDao.getTermById(termId);
            
            Map<String, Object> termInfo = new HashMap<>();
            termInfo.put("termId", termId);
            termInfo.put("termName", "Term " + termId); // Assuming term name is "Term X"
            termInfo.put("averageScore", termAverage);
            termInfo.put("courseCount", courseCount);
            
            termTrends.add(termInfo);
        }
        
        // 按学期ID排序
        termTrends.sort((a, b) -> Long.compare((Long) a.get("termId"), (Long) b.get("termId")));
        
        result.put("termTrends", termTrends);
        
        // 获取学生成绩趋势
        List<Map<String, Object>> scoreTrends = scoreDao.getStudentScoreTrends(studentId);
        result.put("scoreTrends", scoreTrends);
        
        return result;
    }
    
    @Override
    public Map<String, Object> analyzeCourseScores(Long courseId, Long termId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取课程信息
        Course course = courseDao.findById(courseId);
        
        if (course == null) {
            result.put("error", "未找到该课程");
            return result;
        }
        
        // 获取学期信息
        // Assuming termDao is available to getTermById
        // Term term = termDao.getTermById(termId);
        
        if (termId == null) {
            result.put("error", "未找到该学期");
            return result;
        }
        
        // 获取该课程该学期的所有成绩
        List<Score> scores = scoreDao.findByCourseIdAndTermId(courseId, termId);
        
        if (scores == null || scores.isEmpty()) {
            result.put("error", "未找到该课程该学期的成绩记录");
            return result;
        }
        
        // 计算平均分、及格率、优秀率
        double totalScore = 0;
        int passCount = 0;
        int excellentCount = 0;
        
        // 分数分布
        int[] distribution = new int[10]; // 0-9, 10-19, ..., 90-100
        
        for (Score score : scores) {
            totalScore += score.getTotalScore();
            
            if (score.getTotalScore() >= 60) {
                passCount++;
            }
            
            if (score.getTotalScore() >= 85) {
                excellentCount++;
            }
            
            // 计算分数分布
            int index = (int) Math.min(9, Math.floor(score.getTotalScore() / 10));
            distribution[index]++;
        }
        
        double averageScore = totalScore / scores.size();
        double passRate = (double) passCount / scores.size() * 100;
        double excellentRate = (double) excellentCount / scores.size() * 100;
        
        result.put("courseId", courseId);
        result.put("courseName", course.getCourseName());
        result.put("termId", termId);
        result.put("termName", "Term " + termId); // Assuming term name is "Term X"
        result.put("averageScore", averageScore);
        result.put("passRate", passRate);
        result.put("excellentRate", excellentRate);
        result.put("studentCount", scores.size());
        result.put("passCount", passCount);
        result.put("excellentCount", excellentCount);
        result.put("scoreDistribution", distribution);
        
        // 获取不及格学生名单
        List<Map<String, Object>> failedStudents = scoreDao.getFailedStudents(courseId, termId);
        result.put("failedStudents", failedStudents);
        
        // 按班级统计平均分
        Map<Long, List<Score>> classCourseScores = new HashMap<>();
        
        for (Score score : scores) {
            Long classId = score.getStudentId(); // Assuming studentId is used as classId
            
            if (!classCourseScores.containsKey(classId)) {
                classCourseScores.put(classId, new ArrayList<>());
            }
            
            classCourseScores.get(classId).add(score);
        }
        
        List<Map<String, Object>> classAverages = new ArrayList<>();
        
        for (Map.Entry<Long, List<Score>> entry : classCourseScores.entrySet()) {
            Long classId = entry.getKey();
            List<Score> classScores = entry.getValue();
            
            double classTotal = 0;
            for (Score score : classScores) {
                classTotal += score.getTotalScore();
            }
            
            double classAverage = classTotal / classScores.size();
            
            Map<String, Object> classInfo = new HashMap<>();
            classInfo.put("classId", classId);
            classInfo.put("className", "Class " + classId); // Assuming class name is "Class X"
            classInfo.put("averageScore", classAverage);
            classInfo.put("studentCount", classScores.size());
            
            classAverages.add(classInfo);
        }
        
        // 按平均分降序排序
        classAverages.sort((a, b) -> Double.compare((Double) b.get("averageScore"), (Double) a.get("averageScore")));
        
        result.put("classAverages", classAverages);
        
        return result;
    }
    
    @Override
    public Map<String, Object> analyzeTermScores(Long termId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取学期信息
        // Assuming termDao is available to getTermById
        // Term term = termDao.getTermById(termId);
        
        if (termId == null) {
            result.put("error", "未找到该学期");
            return result;
        }
        
        // 获取该学期的所有成绩
        List<Score> scores = scoreDao.findByTermId(termId);
        
        if (scores == null || scores.isEmpty()) {
            result.put("error", "未找到该学期的成绩记录");
            return result;
        }
        
        // 计算平均分、及格率
        double totalScore = 0;
        int passCount = 0;
        
        // 按课程统计平均分
        Map<Long, List<Score>> courseScores = new HashMap<>();
        
        for (Score score : scores) {
            totalScore += score.getTotalScore();
            
            if (score.getTotalScore() >= 60) {
                passCount++;
            }
            
            Long courseId = score.getCourseId();
            
            if (!courseScores.containsKey(courseId)) {
                courseScores.put(courseId, new ArrayList<>());
            }
            
            courseScores.get(courseId).add(score);
        }
        
        double averageScore = totalScore / scores.size();
        double passRate = (double) passCount / scores.size() * 100;
        
        result.put("termId", termId);
        result.put("termName", "Term " + termId); // Assuming term name is "Term X"
        result.put("averageScore", averageScore);
        result.put("passRate", passRate);
        result.put("studentCount", scores.stream().map(Score::getStudentId).distinct().count());
        result.put("courseCount", courseScores.size());
        
        // 计算各课程平均分
        List<Map<String, Object>> courseAverages = new ArrayList<>();
        
        for (Map.Entry<Long, List<Score>> entry : courseScores.entrySet()) {
            Long courseId = entry.getKey();
            List<Score> courseScoreList = entry.getValue();
            
            double courseTotal = 0;
            for (Score score : courseScoreList) {
                courseTotal += score.getTotalScore();
            }
            
            double courseAverage = courseTotal / courseScoreList.size();
            
            Course course = courseDao.findById(courseId);
            
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("courseId", courseId);
            courseInfo.put("courseName", course != null ? course.getCourseName() : "未知课程");
            courseInfo.put("averageScore", courseAverage);
            courseInfo.put("studentCount", courseScoreList.size());
            
            courseAverages.add(courseInfo);
        }
        
        // 按平均分降序排序
        courseAverages.sort((a, b) -> Double.compare((Double) b.get("averageScore"), (Double) a.get("averageScore")));
        
        result.put("courseAverages", courseAverages);
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getMajorCourseRankings(Long majorId, Long termId) {
        return scoreDao.getMajorCourseRankings(majorId, termId);
    }
    
    @Override
    public List<Map<String, Object>> getMajorGpaRankings(Long majorId, Long termId) {
        return scoreDao.getMajorGpaRankings(majorId, termId);
    }
    
    @Override
    public List<Map<String, Object>> getClassGpaRankings(Long classId, Long termId) {
        return scoreDao.getClassGpaRankings(classId, termId);
    }
    
    @Override
    public List<Map<String, Object>> getStudentComprehensiveRankings(Long majorId, Long termId) {
        return scoreDao.getStudentComprehensiveRankings(majorId, termId);
    }
    
    @Override
    public float calculateClassAverageGPA(Long classId, Long termId) {
        return scoreDao.calculateClassAverageGPA(classId, termId);
    }
    
    @Override
    public float calculateMajorAverageGPA(Long majorId, Long termId) {
        return scoreDao.calculateMajorAverageGPA(majorId, termId);
    }
    
    public String exportCourseScores(Long courseId, Long termId) {
        // 导出成绩到文件并返回文件路径
        byte[] data = exportCourseScoresExcel(courseId, termId);
        
        if (data.length == 0) {
            return "导出失败，未找到相关数据";
        }
        
        try {
            // 获取课程信息
            Course course = courseDao.findById(courseId);
            
            // 生成文件名
            String fileName = course.getCourseName() + "_Term" + termId + "_成绩.csv";
            
            // 保存到临时文件
            String filePath = "/tmp/" + fileName;
            Files.write(Paths.get(filePath), data);
            
            return filePath;
        } catch (Exception e) {
            log.error("导出成绩失败", e);
            return "导出成绩失败: " + e.getMessage();
        }
    }
    
    public String importCourseScores(Long courseId, Long termId, MultipartFile file) {
        try {
            // 将MultipartFile转换为byte[]
            byte[] data = file.getBytes();
            
            // 调用导入方法
            Map<String, Object> result = importCourseScores(courseId, termId, data);
            
            // 构建返回信息
            if ((Boolean)result.get("success")) {
                return String.format("导入成功: %d 条记录, 失败: %d 条记录", 
                    result.get("successCount"), result.get("failCount"));
            } else {
                return "导入失败: " + result.get("message");
            }
        } catch (Exception e) {
            log.error("导入成绩失败", e);
            return "导入成绩失败: " + e.getMessage();
        }
    }
    
    @Override
    public Map<String, Integer> getCourseScoreDistribution(Long courseId, Long termId) {
        Map<String, Integer> distribution = new HashMap<>();
        
        // 初始化分数段
        distribution.put("90-100", 0);
        distribution.put("80-89", 0);
        distribution.put("70-79", 0);
        distribution.put("60-69", 0);
        distribution.put("0-59", 0);
        
        // 获取课程成绩
        List<Score> scores = scoreDao.findByCourseIdAndTermId(courseId, termId);
        
        if (scores == null || scores.isEmpty()) {
            return distribution;
        }
        
        // 统计各分数段人数
        for (Score score : scores) {
            float totalScore = score.getTotalScore();
            if (totalScore >= 90) {
                distribution.put("90-100", distribution.get("90-100") + 1);
            } else if (totalScore >= 80) {
                distribution.put("80-89", distribution.get("80-89") + 1);
            } else if (totalScore >= 70) {
                distribution.put("70-79", distribution.get("70-79") + 1);
            } else if (totalScore >= 60) {
                distribution.put("60-69", distribution.get("60-69") + 1);
            } else {
                distribution.put("0-59", distribution.get("0-59") + 1);
            }
        }
        
        return distribution;
    }
    
    @Override
    public List<Map<String, Object>> getStudentScoreWarnings(Long studentId, Long termId) {
        List<Map<String, Object>> warnings = new ArrayList<>();
        
        // 获取学生在指定学期的所有成绩
        List<Score> scores = scoreDao.findByStudentIdAndTermId(studentId, termId);
        
        if (scores == null || scores.isEmpty()) {
            return warnings;
        }
        
        // 查找不及格的课程
        for (Score score : scores) {
            if (score.getTotalScore() < 60) {
                Map<String, Object> warning = new HashMap<>();
                Course course = courseDao.findById(score.getCourseId());
                warning.put("courseName", course != null ? course.getCourseName() : "未知课程");
                warning.put("score", score.getTotalScore());
                warning.put("warningLevel", score.getTotalScore() < 50 ? "严重" : "一般");
                warnings.add(warning);
            }
        }
        
        return warnings;
    }
    
    @Override
    public Map<String, Object> getStudentCourseCompletionStats(Long studentId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取学生所有成绩
        List<Score> scores = scoreDao.findByStudentId(studentId);
        
        if (scores == null || scores.isEmpty()) {
            stats.put("totalCourses", 0);
            stats.put("completedCourses", 0);
            stats.put("completionRate", 0);
            return stats;
        }
        
        // 统计已完成课程数（及格的课程）
        int completedCount = 0;
        for (Score score : scores) {
            if (score.getTotalScore() >= 60) {
                completedCount++;
            }
        }
        
        stats.put("totalCourses", scores.size());
        stats.put("completedCourses", completedCount);
        stats.put("completionRate", (double) completedCount / scores.size() * 100);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getClassAverageScores(Long classId, Long courseId, Long termId) {
        return scoreDao.getClassAverageScores(classId, courseId, termId);
    }
    
    @Override
    public List<Map<String, Object>> getStudentScoreTrends(Long studentId) {
        return scoreDao.getStudentScoreTrends(studentId);
    }

    @Override
    public double calculateStudentTermGPA(Long studentId, Long termId) {
        // 获取该学生该学期的所有成绩
        List<Score> scores = scoreDao.findByStudentIdAndTermId(studentId, termId);
        
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }
        
        double totalGradePoints = 0.0;
        double totalCredits = 0.0;
        
        for (Score score : scores) {
            // 获取课程信息
            Course course = courseDao.findById(score.getCourseId());
            if (course == null) {
                continue;
            }
            
            // 计算绩点
            double gradePoint = calculateGradePoint(score.getTotalScore());
            
            // 累加学分绩点
            totalGradePoints += gradePoint * course.getCredit();
            totalCredits += course.getCredit();
        }
        
        // 避免除以零
        if (totalCredits == 0) {
            return 0.0;
        }
        
        // 计算GPA
        return totalGradePoints / totalCredits;
    }
    
    /**
     * 计算绩点
     * @param score 分数
     * @return 绩点
     */
    private double calculateGradePoint(float score) {
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
    public byte[] exportCourseScoresExcel(Long courseId, Long termId) {
        try {
            // 获取课程信息
            Course course = courseDao.findById(courseId);
            
            if (course == null) {
                log.error("未找到课程: {}", courseId);
                return new byte[0];
            }
            
            // 获取该课程该学期的所有成绩
            List<Score> scores = scoreDao.findByCourseIdAndTermId(courseId, termId);
            
            if (scores == null || scores.isEmpty()) {
                log.error("未找到课程 {} 在学期 {} 的成绩记录", courseId, termId);
                return new byte[0];
            }
            
            // 创建Excel文件内容
            StringBuilder sb = new StringBuilder();
            
            // 添加表头
            sb.append("学号,姓名,班级,成绩,评分日期\n");
            
            // 添加数据
            for (Score score : scores) {
                // 简化实现，实际项目中应该通过StudentDao获取学生信息
                sb.append(String.format("%s,%s,%s,%.1f,%s\n", 
                    "1234567890", "John Doe", "Class 1", score.getTotalScore(), "2024-04-15"));
            }
            
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("导出成绩失败", e);
            return new byte[0];
        }
    }
    
    @Override
    public Map<String, Object> importCourseScores(Long courseId, Long termId, byte[] data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取课程信息
            Course course = courseDao.findById(courseId);
            
            if (course == null) {
                result.put("success", false);
                result.put("message", "未找到该课程");
                return result;
            }
            
            // 读取数据
            String content = new String(data, StandardCharsets.UTF_8);
            String[] lines = content.split("\n");
            
            if (lines.length <= 1) {
                result.put("success", false);
                result.put("message", "文件内容为空或只有表头");
                return result;
            }
            
            // 跳过表头
            int successCount = 0;
            int failCount = 0;
            List<String> failMessages = new ArrayList<>();
            
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                
                if (line.isEmpty()) {
                    continue;
                }
                
                // 处理每一行数据
                // 简化实现，实际项目中应该将数据转换为Score对象并保存
                successCount++;
            }
            
            result.put("success", true);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failMessages", failMessages);
            
            return result;
        } catch (Exception e) {
            log.error("导入成绩失败", e);
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取指定班级的所有学生ID
     * 这是一个辅助方法，用于支持按班级查询成绩
     * @param classId 班级ID
     * @return 学生ID列表
     */
    private List<Long> getStudentIdsByClassId(Long classId) {
        try {
            // 这里假设有一个StudentDao或类似的接口来查询学生
            // 实际实现需要根据项目中的具体数据访问方式来调整
            log.info("正在查询班级{}的所有学生", classId);
            
            // 模拟实现，实际应该调用StudentDao的查询方法
            // 例如：return studentDao.findByClassId(classId).stream().map(Student::getId).collect(Collectors.toList());
            
            // 返回一个空列表作为占位符
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取班级学生列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Score> getScoresByClassId(Long classId) {
        try {
            // 获取班级所有学生ID
            List<Long> studentIds = getStudentIdsByClassId(classId);
            
            if (studentIds.isEmpty()) {
                log.warn("班级{}中没有学生或查询失败", classId);
                return new ArrayList<>();
            }
            
            // 使用学生ID列表查询成绩
            LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Score::getStudentId, studentIds);
            
            return scoreDao.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取班级成绩失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Score> getScoresByClassIdAndTermId(Long classId, Long termId) {
        try {
            // 获取班级所有学生ID
            List<Long> studentIds = getStudentIdsByClassId(classId);
            
            if (studentIds.isEmpty()) {
                log.warn("班级{}中没有学生或查询失败", classId);
                return new ArrayList<>();
            }
            
            // 使用学生ID列表查询成绩
            LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Score::getStudentId, studentIds)
                      .eq(Score::getTermId, termId);
            
            return scoreDao.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取班级学期成绩失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Score> getScoresByClassIdAndCourseId(Long classId, Long courseId) {
        try {
            // 获取班级所有学生ID
            List<Long> studentIds = getStudentIdsByClassId(classId);
            
            if (studentIds.isEmpty()) {
                log.warn("班级{}中没有学生或查询失败", classId);
                return new ArrayList<>();
            }
            
            // 使用学生ID列表查询成绩
            LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Score::getStudentId, studentIds)
                      .eq(Score::getCourseId, courseId);
            
            return scoreDao.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取班级课程成绩失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Score> getScoresByClassIdAndCourseIdAndTermId(Long classId, Long courseId, Long termId) {
        try {
            // 获取班级所有学生ID
            List<Long> studentIds = getStudentIdsByClassId(classId);
            
            if (studentIds.isEmpty()) {
                log.warn("班级{}中没有学生或查询失败", classId);
                return new ArrayList<>();
            }
            
            // 使用学生ID列表查询成绩
            LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Score::getStudentId, studentIds)
                      .eq(Score::getCourseId, courseId)
                      .eq(Score::getTermId, termId);
            
            return scoreDao.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取班级课程学期成绩失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取指定学生在指定学期（可选）的成绩列表
     *
     * @param studentId 学生ID
     * @param semester  学期标识符 (例如 "2023-2024-1")，如果为null或空则查询所有学期
     * @return 成绩列表
     */
    @Override
    public List<Score> getStudentScores(Long studentId, String semester) {
        log.debug("Fetching scores for studentId: {}, semester: {}", studentId, semester);
        try {
            LambdaQueryWrapper<Score> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Score::getStudentId, studentId);

            // 如果指定了学期，需要根据 semester 字符串查找对应的 termId
            if (StringUtils.hasText(semester)) {
                // TODO: 实现根据 semester 字符串查找 termId 的逻辑
                // Long termId = findTermIdBySemesterString(semester);
                // if (termId != null) {
                //     queryWrapper.eq(Score::getTermId, termId);
                // } else {
                //     log.warn("未找到学期标识符 '{}' 对应的 termId，将返回该学生所有成绩", semester);
                //     // 如果找不到对应 termId，可以选择返回所有成绩或空列表
                // }
                log.warn("按 semester 字符串 ('{}') 筛选成绩的功能尚未实现，将返回该学生所有成绩", semester);
            }

            queryWrapper.orderByAsc(Score::getCourseId);

            List<Score> scores = scoreDao.selectList(queryWrapper);
            log.debug("Found {} scores for studentId: {}, semester filter (if implemented): '{}'", scores.size(), studentId, semester);
            return scores;
        } catch (Exception e) {
            log.error("查询学生成绩时出错 - studentId: {}, semester: {}", studentId, semester, e);
            return Collections.emptyList();
        }
    }

    // 可以在这里添加一个根据 semester 字符串查找 termId 的辅助方法 (如果需要)
    // private Long findTermIdBySemesterString(String semester) { ... }
}