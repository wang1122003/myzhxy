package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Score;
import com.campus.vo.ScoreVO;

import java.util.List;
import java.util.Map;

/**
 * 成绩服务接口 (已清理)
 */
public interface ScoreService extends IService<Score> {

    /**
     * 记录或更新成绩
     * @param score 成绩对象
     * @return 是否成功
     */
    boolean recordScore(Score score);

    /**
     * 更新成绩
     * @param score 成绩对象
     * @return 是否成功
     */
    boolean updateScore(Score score);

    /**
     * 删除成绩
     * @param id 成绩ID
     * @return 是否成功
     */
    boolean deleteScore(Long id);

    /**
     * 批量删除成绩
     * @param ids 成绩ID数组
     * @return 是否成功
     */
    boolean batchDeleteScores(Long[] ids);

    /**
     * 获取指定学生指定课程的成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩对象，如果不存在则返回null
     */
    Score getStudentCourseScore(Long studentId, Long courseId);

    /**
     * 获取指定学生的所有成绩
     * @param studentId 学生ID
     * @return 成绩视图对象列表 (包含课程信息)
     */
    List<ScoreVO> getStudentScores(Long studentId);

    /**
     * 获取指定课程的所有成绩
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<Score> getCourseScores(Long courseId);

    /**
     * 获取指定课程的成绩统计信息
     *
     * @param courseId 课程ID
     * @return 包含 average, max, min, count, validCount 的 Map
     */
    Map<String, Object> getScoreStatistics(Long courseId);

    // 移除了大量不再需要的接口方法定义
}