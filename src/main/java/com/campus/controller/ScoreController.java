package com.campus.controller;

// import com.campus.dto.ScoreDTO; // 移除未使用
import com.campus.entity.Score;
import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.service.ScoreService;
import com.campus.utils.Result;
import com.campus.vo.ScoreVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 成绩管理控制器 (已重构)
 * 提供简化后的成绩查询和管理 API。
 */
@Slf4j
@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private AuthService authService;

    // --- 核心 CRUD 操作 --- 

    /**
     * 记录或更新成绩 (通过RequestBody)
     * 如果成绩对象包含ID，则更新；否则创建。
     * @param score 成绩信息 (必须包含 studentId, courseId, score)
     * @return 操作结果
     */
    @PostMapping // 处理创建
    public Result recordOrUpdateScore(@RequestBody Score score) {
        // 在调用Service前进行基本验证
        if (score.getStudentId() == null || score.getCourseId() == null || score.getTotalScore() == null) {
            return Result.error("请求参数错误：缺少 studentId, courseId 或 totalScore");
        }
        try {
            boolean result = scoreService.recordScore(score);
            return result ? Result.success("操作成功") : Result.error("操作失败");
        } catch (Exception e) {
            log.error("记录/更新成绩出错: {}", e.getMessage(), e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 删除成绩
     * @param id 成绩ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteScore(@PathVariable Long id) {
        try {
            boolean result = scoreService.deleteScore(id);
            return result ? Result.success("删除成功") : Result.error("删除失败或成绩不存在");
        } catch (Exception e) {
            log.error("删除成绩 ID {} 出错: {}", id, e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除成绩
     * @param ids 成绩ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result batchDeleteScores(@RequestBody Long[] ids) {
        try {
            boolean result = scoreService.batchDeleteScores(ids);
            return result ? Result.success("批量删除成功") : Result.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除成绩出错: {}", e.getMessage(), e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    // --- 查询操作 --- 

    /**
     * 获取指定学生指定课程的成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩对象
     */
    @GetMapping("/student/{studentId}/course/{courseId}")
    public Result getStudentCourseScore(@PathVariable Long studentId, @PathVariable Long courseId) {
        Score score = scoreService.getStudentCourseScore(studentId, courseId);
        return score != null ? Result.success(score) : Result.error("未找到成绩记录");
    }

    /**
     * 获取指定学生的所有成绩
     * @param studentId 学生ID
     * @return 学生的成绩列表
     */
    @GetMapping("/student/{studentId}")
    public Result getStudentScores(@PathVariable Long studentId) {
        List<ScoreVO> scores = scoreService.getStudentScores(studentId);
        return Result.success(scores);
    }

    /**
     * 获取指定课程的所有成绩
     * @param courseId 课程ID
     * @return 课程的成绩列表
     */
    @GetMapping("/course/{courseId}")
    public Result getCourseScores(@PathVariable Long courseId) {
        List<Score> scores = scoreService.getCourseScores(courseId);
        return Result.success(scores);
    }

    /**
     * 获取当前登录学生的成绩
     *
     * @param request HTTP请求
     * @return 学生的成绩列表
     */
    @GetMapping("/me")
    public Result getMyScores(HttpServletRequest request) {
        User currentUser = authService.getCurrentUserFromRequest(request); // 使用修正后的方法名
        if (currentUser == null) {
            return Result.error(401, "用户未认证");
        }
        // 假设学生有特定的 userType 或根据登录用户ID检索
        // if (currentUser.getUserType() != 3) { ... return Result.error(403, ...); }

        List<ScoreVO> scores = scoreService.getStudentScores(currentUser.getId());
        return Result.success(scores);
    }

    /**
     * 获取指定课程的成绩统计信息
     *
     * @param courseId 课程ID
     * @return 统计信息Map
     */
    @GetMapping("/stats/course/{courseId}")
    public Result getCourseScoreStatistics(@PathVariable Long courseId) {
        Map<String, Object> stats = scoreService.getScoreStatistics(courseId);
        return Result.success(stats);
    }

    // --- 已移除的过时端点 --- 
    // 已移除: getScoreById (使用 getStudentCourseScore), getAllScores (范围过大, 使用具体查询),
    // getScoresByTeacherId, getScoresByTermId, getScoresByStudentIdAndTermId, getScoresByCourseIdAndTermId (可在客户端过滤或添加特定Service方法),
    // addScore (由 recordOrUpdateScore 覆盖), updateScore (由 recordOrUpdateScore 覆盖),
    // batchAddScores (如果需要，使用循环调用 recordOrUpdateScore),
    // getScoreStats (通用统计), getCourseAverageStats, getStudentAverageStats, getScoreDistribution,
    // getTermScoreTrend, getStudentRankings, calculateStudentGPA, calculateWeightedAverage,
    // exportScores, getPassRateStats, getFailedStudents, analyzeStudentScores, analyzeCourseScores,
    // getMajorCourseRankings, getMajorGpaRankings, getClassGpaRankings, getClassAverageScores,
    // getStudentScoreTrends, 等等。
}