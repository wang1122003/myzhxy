package com.campus.service;

import java.util.List;
import java.util.Map;

import com.campus.entity.Score;

/**
 * 成绩服务接口
 */
public interface ScoreService {
    
    /**
     * 根据ID查询成绩
     * @param id 成绩ID
     * @return 成绩对象
     */
    Score getScoreById(Long id);
    
    /**
     * 查询所有成绩
     * @return 成绩列表
     */
    List<Score> getAllScores();
    
    /**
     * 根据学生ID查询成绩
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<Score> getScoresByStudentId(Long studentId);
    
    /**
     * 根据课程ID查询成绩
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<Score> getScoresByCourseId(Long courseId);
    
    /**
     * 根据教师ID查询成绩
     * @param teacherId 教师ID
     * @return 成绩列表
     */
    List<Score> getScoresByTeacherId(Long teacherId);
    
    /**
     * 根据学期ID查询成绩
     * @param termId 学期ID
     * @return 成绩列表
     */
    List<Score> getScoresByTermId(Long termId);
    
    /**
     * 根据学生ID和学期ID查询成绩
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return 成绩列表
     */
    List<Score> getScoresByStudentIdAndTermId(Long studentId, Long termId);
    
    /**
     * 根据课程ID和学期ID查询成绩
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 成绩列表
     */
    List<Score> getScoresByCourseIdAndTermId(Long courseId, Long termId);
    
    /**
     * 根据学生ID和课程ID查询成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩对象
     */
    Score getScoreByStudentIdAndCourseId(Long studentId, Long courseId);
    
    /**
     * 根据班级ID查询成绩
     * @param classId 班级ID
     * @return 成绩列表
     */
    List<Score> getScoresByClassId(Long classId);
    
    /**
     * 根据班级ID和学期ID查询成绩
     * @param classId 班级ID
     * @param termId 学期ID
     * @return 成绩列表
     */
    List<Score> getScoresByClassIdAndTermId(Long classId, Long termId);
    
    /**
     * 根据班级ID和课程ID查询成绩
     * @param classId 班级ID
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<Score> getScoresByClassIdAndCourseId(Long classId, Long courseId);
    
    /**
     * 根据班级ID、课程ID和学期ID查询成绩
     * @param classId 班级ID
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 成绩列表
     */
    List<Score> getScoresByClassIdAndCourseIdAndTermId(Long classId, Long courseId, Long termId);
    
    /**
     * 添加成绩
     * @param score 成绩对象
     * @return 是否成功
     */
    boolean addScore(Score score);
    
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
     * 批量添加成绩
     * @param scores 成绩列表
     * @return 是否成功
     */
    boolean batchAddScores(List<Score> scores);
    
    /**
     * 计算总评成绩
     * @param score 成绩对象
     * @return 总评成绩
     */
    float calculateTotalScore(Score score);
    
    /**
     * 根据总评成绩计算等级
     * @param totalScore 总评成绩
     * @return 成绩等级
     */
    String calculateGrade(float totalScore);
    
    /**
     * 分页查询成绩
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 成绩列表
     */
    List<Score> getScoresByPage(int pageNum, int pageSize);
    
    /**
     * 获取成绩总数
     * @return 成绩总数
     */
    int getScoreCount();
    
    /**
     * 搜索成绩
     * @param keyword 关键词
     * @return 成绩列表
     */
    List<Score> searchScores(String keyword);
    
    /**
     * 获取成绩统计信息
     * @return 统计信息
     */
    Map<String, Object> getScoreStats();
    
    /**
     * 获取课程平均分统计
     * @return 课程平均分统计
     */
    List<Map<String, Object>> getCourseAverageStats();
    
    /**
     * 获取学生平均分统计
     * @return 学生平均分统计
     */
    List<Map<String, Object>> getStudentAverageStats();
    
    /**
     * 获取分数段分布统计
     * @return 分数段分布统计
     */
    List<Map<String, Object>> getScoreDistribution();
    
    /**
     * 获取学期成绩趋势统计
     * @param studentId 学生ID
     * @return 学期成绩趋势
     */
    List<Map<String, Object>> getTermScoreTrend(Long studentId);
    
    /**
     * 获取学生成绩排名
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 成绩排名列表
     */
    List<Map<String, Object>> getStudentRankings(Long courseId, Long termId);
    
    /**
     * 计算学生GPA
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return GPA值
     */
    float calculateStudentGPA(Long studentId, Long termId);
    
    /**
     * 计算学生加权平均分
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return 加权平均分
     */
    float calculateWeightedAverage(Long studentId, Long termId);
    
    /**
     * 导出成绩
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 导出数据
     */
    List<Map<String, Object>> exportScores(Long courseId, Long termId);
    
    /**
     * 批量导入成绩
     * @param scores 成绩列表
     * @return 是否成功
     */
    boolean importScores(List<Score> scores);
    
    /**
     * 获取学生及格率统计
     * @return 及格率统计
     */
    List<Map<String, Object>> getPassRateStats();
    
    /**
     * 获取不及格学生名单
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 不及格学生列表
     */
    List<Map<String, Object>> getFailedStudents(Long courseId, Long termId);
    
    /**
     * 获取学生课程成绩分析
     * @param studentId 学生ID
     * @return 学生课程成绩分析
     */
    Map<String, Object> analyzeStudentScores(Long studentId);
    
    /**
     * 获取课程成绩分析
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 课程成绩分析
     */
    Map<String, Object> analyzeCourseScores(Long courseId, Long termId);
    
    /**
     * 获取学期成绩分析
     * @param termId 学期ID
     * @return 学期成绩分析
     */
    Map<String, Object> analyzeTermScores(Long termId);
    
    /**
     * 获取专业课程排名
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 专业课程排名
     */
    List<Map<String, Object>> getMajorCourseRankings(Long majorId, Long termId);
    
    /**
     * 获取专业GPA排名
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 专业GPA排名
     */
    List<Map<String, Object>> getMajorGpaRankings(Long majorId, Long termId);
    
    /**
     * 获取班级GPA排名
     * @param classId 班级ID
     * @param termId 学期ID
     * @return 班级GPA排名
     */
    List<Map<String, Object>> getClassGpaRankings(Long classId, Long termId);
    
    /**
     * 获取学生综合成绩排名
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 学生综合成绩排名
     */
    List<Map<String, Object>> getStudentComprehensiveRankings(Long majorId, Long termId);
    
    /**
     * 计算班级平均GPA
     * @param classId 班级ID
     * @param termId 学期ID
     * @return 班级平均GPA
     */
    float calculateClassAverageGPA(Long classId, Long termId);
    
    /**
     * 计算专业平均GPA
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 专业平均GPA
     */
    float calculateMajorAverageGPA(Long majorId, Long termId);
    
    /**
     * 导出课程成绩明细
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 课程成绩明细
     */
    byte[] exportCourseScoresExcel(Long courseId, Long termId);
    
    /**
     * 导入课程成绩
     * @param courseId 课程ID
     * @param termId 学期ID
     * @param data 成绩数据
     * @return 导入结果
     */
    Map<String, Object> importCourseScores(Long courseId, Long termId, byte[] data);
    
    /**
     * 获取课程成绩分布
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 课程成绩分布
     */
    Map<String, Integer> getCourseScoreDistribution(Long courseId, Long termId);
    
    /**
     * 获取学生成绩预警
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return 成绩预警信息
     */
    List<Map<String, Object>> getStudentScoreWarnings(Long studentId, Long termId);
    
    /**
     * 获取学生课程完成统计
     * @param studentId 学生ID
     * @return 课程完成统计
     */
    Map<String, Object> getStudentCourseCompletionStats(Long studentId);
    
    /**
     * 获取班级平均成绩
     * @param classId 班级ID
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 班级平均成绩
     */
    Map<String, Object> getClassAverageScores(Long classId, Long courseId, Long termId);
    
    /**
     * 获取学生成绩趋势
     * @param studentId 学生ID
     * @return 成绩趋势
     */
    List<Map<String, Object>> getStudentScoreTrends(Long studentId);
    
    /**
     * 计算学生学期GPA
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return GPA值
     */
    double calculateStudentTermGPA(Long studentId, Long termId);
}