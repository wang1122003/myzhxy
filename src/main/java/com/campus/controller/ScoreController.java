package com.campus.controller;

import com.campus.entity.Score;
import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.service.ScoreService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 成绩管理控制器
 * 提供成绩查询、录入和统计分析的相关API
 */
@Slf4j
@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private AuthService authService;

    /**
     * 获取成绩详情
     * 
     * @param id 成绩ID
     * @return 成绩详情信息
     */
    @GetMapping("/{id}")
    public Result getScoreById(@PathVariable Long id) {
        Score score = scoreService.getScoreById(id);
        if (score != null) {
            return Result.success("获取成功", score);
        } else {
            return Result.error("成绩不存在");
        }
    }

    /**
     * 获取所有成绩
     * 
     * @return 成绩列表
     */
    @GetMapping
    public Result getAllScores() {
        List<Score> scores = scoreService.getAllScores();
        return Result.success("获取成功", scores);
    }

    /**
     * 根据学生ID获取成绩
     * 
     * @param studentId 学生ID
     * @return 学生的成绩列表
     */
    @GetMapping("/student/{studentId}")
    public Result getScoresByStudentId(@PathVariable Long studentId) {
        List<Score> scores = scoreService.getScoresByStudentId(studentId);
        return Result.success("获取成功", scores);
    }

    /**
     * 根据课程ID获取成绩
     * 
     * @param courseId 课程ID
     * @return 课程的成绩列表
     */
    @GetMapping("/course/{courseId}")
    public Result getScoresByCourseId(@PathVariable Long courseId) {
        List<Score> scores = scoreService.getScoresByCourseId(courseId);
        return Result.success("获取成功", scores);
    }

    /**
     * 根据教师ID获取成绩
     * 
     * @param teacherId 教师ID
     * @return 教师所教课程的成绩列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result getScoresByTeacherId(@PathVariable Long teacherId) {
        List<Score> scores = scoreService.getScoresByTeacherId(teacherId);
        return Result.success("获取成功", scores);
    }

    /**
     * 根据学期ID获取成绩
     * 
     * @param termId 学期ID
     * @return 学期的成绩列表
     */
    @GetMapping("/term/{termId}")
    public Result getScoresByTermId(@PathVariable Long termId) {
        List<Score> scores = scoreService.getScoresByTermId(termId);
        return Result.success("获取成功", scores);
    }

    /**
     * 根据学生ID和学期ID获取成绩
     * 
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return 指定学生在指定学期的成绩列表
     */
    @GetMapping("/student/{studentId}/term/{termId}")
    public Result getScoresByStudentIdAndTermId(
            @PathVariable Long studentId, @PathVariable Long termId) {
        List<Score> scores = scoreService.getScoresByStudentIdAndTermId(studentId, termId);
        return Result.success("获取成功", scores);
    }

    /**
     * 根据课程ID和学期ID获取成绩
     * 
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 指定课程在指定学期的成绩列表
     */
    @GetMapping("/course/{courseId}/term/{termId}")
    public Result getScoresByCourseIdAndTermId(
            @PathVariable Long courseId, @PathVariable Long termId) {
        List<Score> scores = scoreService.getScoresByCourseIdAndTermId(courseId, termId);
        return Result.success("获取成功", scores);
    }

    /**
     * 添加成绩
     * 
     * @param score 成绩信息
     * @return 添加结果
     */
    @PostMapping
    public Result addScore(@RequestBody Score score) {
        boolean result = scoreService.addScore(score);
        if (result) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    /**
     * 更新成绩
     * 
     * @param id 成绩ID
     * @param score 成绩信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result updateScore(@PathVariable Long id, @RequestBody Score score) {
        score.setId(id);
        boolean result = scoreService.updateScore(score);
        if (result) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 删除成绩
     * 
     * @param id 成绩ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteScore(@PathVariable Long id) {
        boolean result = scoreService.deleteScore(id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 批量删除成绩
     * 
     * @param ids 成绩ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result batchDeleteScores(@RequestBody Long[] ids) {
        boolean result = scoreService.batchDeleteScores(ids);
        if (result) {
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }

    /**
     * 批量添加成绩
     * 
     * @param scores 成绩列表
     * @return 添加结果
     */
    @PostMapping("/batch")
    public Result batchAddScores(@RequestBody List<Score> scores) {
        boolean result = scoreService.batchAddScores(scores);
        if (result) {
            return Result.success("批量添加成功");
        } else {
            return Result.error("批量添加失败");
        }
    }

    /**
     * 获取成绩统计信息
     * 
     * @return 成绩统计信息
     */
    @GetMapping("/stats")
    public Result getScoreStats() {
        Map<String, Object> stats = scoreService.getScoreStats();
        return Result.success("获取成功", stats);
    }

    /**
     * 获取课程平均分统计
     * 
     * @return 课程平均分统计
     */
    @GetMapping("/stats/course-averages")
    public Result getCourseAverageStats() {
        List<Map<String, Object>> stats = scoreService.getCourseAverageStats();
        return Result.success("获取成功", stats);
    }

    /**
     * 获取学生平均分统计
     * 
     * @return 学生平均分统计
     */
    @GetMapping("/stats/student-averages")
    public Result getStudentAverageStats() {
        List<Map<String, Object>> stats = scoreService.getStudentAverageStats();
        return Result.success("获取成功", stats);
    }

    /**
     * 获取分数段分布统计
     * 
     * @return 分数段分布统计
     */
    @GetMapping("/stats/distribution")
    public Result getScoreDistribution() {
        List<Map<String, Object>> distribution = scoreService.getScoreDistribution();
        return Result.success("获取成功", distribution);
    }

    /**
     * 获取学期成绩趋势统计
     * 
     * @param studentId 学生ID
     * @return 学期成绩趋势
     */
    @GetMapping("/trends/term/{studentId}")
    public Result getTermScoreTrend(@PathVariable Long studentId) {
        List<Map<String, Object>> trend = scoreService.getTermScoreTrend(studentId);
        return Result.success("获取成功", trend);
    }

    /**
     * 获取学生成绩排名
     * 
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 成绩排名列表
     */
    @GetMapping("/rankings/course/{courseId}/term/{termId}")
    public Result getStudentRankings(
            @PathVariable Long courseId, @PathVariable Long termId) {
        List<Map<String, Object>> rankings = scoreService.getStudentRankings(courseId, termId);
        return Result.success("获取成功", rankings);
    }

    /**
     * 计算学生GPA
     * 
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return GPA值
     */
    @GetMapping("/gpa/student/{studentId}/term/{termId}")
    public Result calculateStudentGPA(
            @PathVariable Long studentId, @PathVariable Long termId) {
        float gpa = scoreService.calculateStudentGPA(studentId, termId);
        Map<String, Object> result = Map.of("studentId", studentId, "termId", termId, "gpa", gpa);
        return Result.success("获取成功", result);
    }

    /**
     * 计算学生加权平均分
     * 
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return 加权平均分
     */
    @GetMapping("/weighted-average/student/{studentId}/term/{termId}")
    public Result calculateWeightedAverage(
            @PathVariable Long studentId, @PathVariable Long termId) {
        float average = scoreService.calculateWeightedAverage(studentId, termId);
        Map<String, Object> result = Map.of("studentId", studentId, "termId", termId, "weightedAverage", average);
        return Result.success("获取成功", result);
    }

    /**
     * 导出成绩
     * 
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 导出数据
     */
    @GetMapping("/export/course/{courseId}/term/{termId}")
    public Result exportScores(
            @PathVariable Long courseId, @PathVariable Long termId) {
        List<Map<String, Object>> exportData = scoreService.exportScores(courseId, termId);
        return Result.success("获取成功", exportData);
    }

    /**
     * 获取及格率统计
     * 
     * @return 及格率统计
     */
    @GetMapping("/stats/pass-rate")
    public Result getPassRateStats() {
        List<Map<String, Object>> stats = scoreService.getPassRateStats();
        return Result.success("获取成功", stats);
    }

    /**
     * 获取不及格学生名单
     * 
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 不及格学生列表
     */
    @GetMapping("/failed-students/course/{courseId}/term/{termId}")
    public Result getFailedStudents(
            @PathVariable Long courseId, @PathVariable Long termId) {
        List<Map<String, Object>> failedStudents = scoreService.getFailedStudents(courseId, termId);
        return Result.success("获取成功", failedStudents);
    }

    /**
     * 获取学生课程成绩分析
     * 
     * @param studentId 学生ID
     * @return 学生课程成绩分析
     */
    @GetMapping("/analysis/student/{studentId}")
    public Result analyzeStudentScores(@PathVariable Long studentId) {
        Map<String, Object> analysis = scoreService.analyzeStudentScores(studentId);
        return Result.success("获取成功", analysis);
    }

    /**
     * 获取课程成绩分析
     * 
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 课程成绩分析
     */
    @GetMapping("/analysis/course/{courseId}/term/{termId}")
    public Result analyzeCourseScores(
            @PathVariable Long courseId, @PathVariable Long termId) {
        Map<String, Object> analysis = scoreService.analyzeCourseScores(courseId, termId);
        return Result.success("获取成功", analysis);
    }

    /**
     * 获取专业课程排名
     * 
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 专业课程排名
     */
    @GetMapping("/rankings/major/{majorId}/term/{termId}/courses")
    public Result getMajorCourseRankings(
            @PathVariable Long majorId, @PathVariable Long termId) {
        List<Map<String, Object>> rankings = scoreService.getMajorCourseRankings(majorId, termId);
        return Result.success("获取成功", rankings);
    }

    /**
     * 获取专业GPA排名
     * 
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 专业GPA排名
     */
    @GetMapping("/rankings/major/{majorId}/term/{termId}/gpa")
    public Result getMajorGpaRankings(
            @PathVariable Long majorId, @PathVariable Long termId) {
        List<Map<String, Object>> rankings = scoreService.getMajorGpaRankings(majorId, termId);
        return Result.success("获取成功", rankings);
    }

    /**
     * 获取班级GPA排名
     * 
     * @param classId 班级ID
     * @param termId 学期ID
     * @return 班级GPA排名
     */
    @GetMapping("/rankings/class/{classId}/term/{termId}/gpa")
    public Result getClassGpaRankings(
            @PathVariable Long classId, @PathVariable Long termId) {
        List<Map<String, Object>> rankings = scoreService.getClassGpaRankings(classId, termId);
        return Result.success("获取成功", rankings);
    }

    /**
     * 获取班级平均成绩
     * 
     * @param classId 班级ID
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 班级平均成绩
     */
    @GetMapping("/average/class/{classId}/course/{courseId}/term/{termId}")
    public Result getClassAverageScores(
            @PathVariable Long classId, @PathVariable Long courseId, @PathVariable Long termId) {
        Map<String, Object> averageScores = scoreService.getClassAverageScores(classId, courseId, termId);
        return Result.success("获取成功", averageScores);
    }

    /**
     * 获取学生成绩趋势
     * 
     * @param studentId 学生ID
     * @return 成绩趋势
     */
    @GetMapping("/trends/student/{studentId}")
    public Result getStudentScoreTrends(@PathVariable Long studentId) {
        List<Map<String, Object>> trends = scoreService.getStudentScoreTrends(studentId);
        return Result.success("获取成功", trends);
    }

    /**
     * 获取当前登录学生的成绩
     *
     * @param request  HTTP请求
     * @param semester 学期 (可选, 格式如 2023-2024-1)
     * @return 学生的成绩列表
     */
    @GetMapping("/me")
    public Result getMyScores(HttpServletRequest request, @RequestParam(required = false) String semester) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            log.warn("getMyScores: 未找到当前登录用户");
            return Result.error("未登录或无法获取用户信息");
        }

        Long studentId = currentUser.getId();
        log.info("getMyScores: 开始获取学生ID {} 在学期 '{}' 的成绩", studentId, semester);

        try {
            List<Score> scores = scoreService.getStudentScores(studentId, semester);
            log.info("getMyScores: 成功获取学生ID {} 在学期 '{}' 的成绩 {} 条", studentId, semester, scores == null ? 0 : scores.size());
            return Result.success("获取成功", scores);
        } catch (Exception e) {
            log.error("getMyScores: 获取学生ID {} 在学期 '{}' 的成绩时发生错误", studentId, semester, e);
            // 这里返回更通用的错误信息给前端，避免暴露内部细节
            return Result.error("获取成绩失败，请稍后重试或联系管理员");
        }
    }
}