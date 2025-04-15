package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Score;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 学生成绩数据访问接口
 */
@Repository
public interface ScoreDao extends BaseMapper<Score> {
    /**
     * 根据ID查询成绩
     * @param id 成绩ID
     * @return 成绩对象
     */
    Score findById(Long id);
    
    /**
     * 查询所有成绩
     * @return 成绩列表
     */
    List<Score> findAll();
    
    /**
     * 根据学生ID查询成绩
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<Score> findByStudentId(Long studentId);
    
    /**
     * 根据课程ID查询成绩
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<Score> findByCourseId(Long courseId);
    
    /**
     * 根据教师ID查询成绩
     * @param teacherId 教师ID
     * @return 成绩列表
     */
    List<Score> findByTeacherId(Long teacherId);
    
    /**
     * 根据学期ID查询成绩
     * @param termId 学期ID
     * @return 成绩列表
     */
    List<Score> findByTermId(Long termId);
    
    /**
     * 根据学生ID和学期ID查询成绩
     * @param studentId 学生ID
     * @param termId 学期ID
     * @return 成绩列表
     */
    List<Score> findByStudentIdAndTermId(@Param("studentId") Long studentId, 
                                       @Param("termId") Long termId);
    
    /**
     * 根据课程ID和学期ID查询成绩
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 成绩列表
     */
    List<Score> findByCourseIdAndTermId(@Param("courseId") Long courseId, 
                                       @Param("termId") Long termId);
    
    /**
     * 根据学生ID和课程ID查询成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩对象
     */
    Score findByStudentIdAndCourseId(@Param("studentId") Long studentId, 
                                   @Param("courseId") Long courseId);
    
    /**
     * 分页查询成绩
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 成绩列表
     */
    List<Score> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取成绩总数
     * @return 成绩数量
     */
    int getCount();
    
    /**
     * 添加成绩
     * @param score 成绩对象
     * @return 影响行数
     */
    int insert(Score score);
    
    /**
     * 更新成绩
     * @param score 成绩对象
     * @return 影响行数
     */
    int update(Score score);
    
    /**
     * 删除成绩
     * @param id 成绩ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 批量删除成绩
     * @param ids 成绩ID数组
     * @return 影响行数
     */
    int batchDelete(Long[] ids);
    
    /**
     * 批量导入成绩
     * @param scores 成绩列表
     * @return 影响行数
     */
    int batchInsert(List<Score> scores);
    
    /**
     * 更新成绩状态
     * @param id 成绩ID
     * @param status 成绩状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 搜索成绩
     * @param keyword 关键词
     * @return 成绩列表
     */
    List<Score> searchScores(@Param("keyword") String keyword);
    
    /**
     * 获取课程平均分
     * @param courseId 课程ID
     * @return 平均分
     */
    Double getCourseAverageScore(Long courseId);
    
    /**
     * 获取学生平均分
     * @param studentId 学生ID
     * @return 平均分
     */
    Double getStudentAverageScore(Long studentId);
    
    /**
     * 获取课程最高分
     * @param courseId 课程ID
     * @return 最高分
     */
    Double getCourseMaxScore(Long courseId);
    
    /**
     * 获取课程最低分
     * @param courseId 课程ID
     * @return 最低分
     */
    Double getCourseMinScore(Long courseId);
    
    /**
     * 获取及格成绩数量
     * @return 及格数量
     */
    int getPassCount();
    
    /**
     * 获取不及格成绩数量
     * @return 不及格数量
     */
    int getFailCount();
    
    /**
     * 获取平均分
     * @return 平均分
     */
    Double getAverageScore();
    
    /**
     * 获取最高分
     * @return 最高分
     */
    Double getHighestScore();
    
    /**
     * 获取最低分
     * @return 最低分
     */
    Double getLowestScore();
    
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
     * 获取成绩分布统计
     * @return 成绩分布统计
     */
    List<Map<String, Object>> getScoreDistribution();
    
    /**
     * 获取学期成绩趋势
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
    List<Map<String, Object>> getStudentRankings(@Param("courseId") Long courseId, 
                                               @Param("termId") Long termId);
    
    /**
     * 导出成绩
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 导出数据
     */
    List<Map<String, Object>> exportScores(@Param("courseId") Long courseId, 
                                         @Param("termId") Long termId);
    
    /**
     * 获取及格率统计
     * @return 及格率统计
     */
    List<Map<String, Object>> getPassRateStats();
    
    /**
     * 获取不及格学生名单
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 不及格学生列表
     */
    List<Map<String, Object>> getFailedStudents(@Param("courseId") Long courseId, 
                                              @Param("termId") Long termId);
    
    /**
     * 获取专业课程排名
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 专业课程排名
     */
    List<Map<String, Object>> getMajorCourseRankings(@Param("majorId") Long majorId, 
                                                   @Param("termId") Long termId);
                                                   
    /**
     * 获取专业GPA排名
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 专业GPA排名
     */
    List<Map<String, Object>> getMajorGpaRankings(@Param("majorId") Long majorId, 
                                                @Param("termId") Long termId);
                                                
    /**
     * 获取班级GPA排名
     * @param classId 班级ID
     * @param termId 学期ID
     * @return 班级GPA排名
     */
    List<Map<String, Object>> getClassGpaRankings(@Param("classId") Long classId, 
                                                @Param("termId") Long termId);
                                                
    /**
     * 获取学生综合排名
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 学生综合排名
     */
    List<Map<String, Object>> getStudentComprehensiveRankings(@Param("majorId") Long majorId, 
                                                            @Param("termId") Long termId);
                                                            
    /**
     * 计算班级平均GPA
     * @param classId 班级ID
     * @param termId 学期ID
     * @return 班级平均GPA
     */
    float calculateClassAverageGPA(@Param("classId") Long classId, 
                                 @Param("termId") Long termId);
                                 
    /**
     * 计算专业平均GPA
     * @param majorId 专业ID
     * @param termId 学期ID
     * @return 专业平均GPA
     */
    float calculateMajorAverageGPA(@Param("majorId") Long majorId, 
                                  @Param("termId") Long termId);
                                  
    /**
     * 获取班级平均分
     * @param classId 班级ID
     * @param courseId 课程ID
     * @param termId 学期ID
     * @return 班级平均分
     */
    Map<String, Object> getClassAverageScores(@Param("classId") Long classId, 
                                            @Param("courseId") Long courseId, 
                                            @Param("termId") Long termId);
                                            
    /**
     * 获取学生成绩趋势
     * @param studentId 学生ID
     * @return 学生成绩趋势
     */
    List<Map<String, Object>> getStudentScoreTrends(Long studentId);
}