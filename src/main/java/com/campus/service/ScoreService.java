package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Score;

import java.util.List;
import java.util.Map;

/**
 * 成绩服务接口
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
     * @param termInfo 学期信息
     * @return 成绩对象，如果不存在则返回null
     */
    Score getStudentCourseScore(Long studentId, Long courseId, String termInfo);

    /**
     * 根据选课ID获取成绩
     *
     * @param selectionId 选课ID
     * @return 成绩对象
     */
    Score getScoreBySelectionId(Long selectionId);

    /**
     * 获取指定学生的所有成绩
     * @param studentId 学生ID
     * @return 成绩列表 (包含课程信息)
     */
    List<Score> getStudentScores(Long studentId);

    /**
     * 获取指定学生在指定学期的所有成绩
     * @param studentId 学生ID
     * @param termInfo 学期信息
     * @return 成绩列表
     */
    List<Score> getStudentTermScores(Long studentId, String termInfo);

    /**
     * 获取指定课程的所有成绩
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<Score> getCourseScores(Long courseId);

    /**
     * 获取指定课程在指定学期的所有成绩
     *
     * @param courseId 课程ID
     * @param termInfo 学期信息
     * @return 成绩列表
     */
    List<Score> getCourseTermScores(Long courseId, String termInfo);

    /**
     * 获取指定课程的成绩统计信息
     * @param courseId 课程ID
     * @param termInfo 学期信息
     * @return 包含 average, max, min, count, validCount 的 Map
     */
    Map<String, Object> getScoreStatistics(Long courseId, String termInfo);

    /**
     * 计算学生GPA
     *
     * @param studentId 学生ID
     * @param termInfo  学期信息，如果为null则计算所有学期
     * @return GPA值
     */
    double calculateStudentGPA(Long studentId, String termInfo);

    /**
     * 批量录入成绩
     *
     * @param scores 成绩列表
     * @return 是否成功
     */
    boolean batchRecordScores(List<Score> scores);
}