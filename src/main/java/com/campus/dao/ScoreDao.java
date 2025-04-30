package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学生成绩数据访问接口
 */
@Mapper
@Repository
public interface ScoreDao extends BaseMapper<Score> {
    /**
     * 根据选课ID查询成绩
     *
     * @param selectionId 选课ID
     * @return 成绩对象
     */
    Score findBySelectionId(Long selectionId);

    /**
     * 根据学生ID查询成绩
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<Score> findByStudentId(Long studentId);

    /**
     * 根据课程ID查询成绩
     *
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<Score> findByCourseId(Long courseId);

    /**
     * 根据学期信息查询成绩
     *
     * @param termInfo 学期信息
     * @return 成绩列表
     */
    List<Score> findByTermInfo(String termInfo);

    /**
     * 根据学生ID和学期信息查询成绩
     *
     * @param studentId 学生ID
     * @param termInfo  学期信息
     * @return 成绩列表
     */
    List<Score> findByStudentIdAndTermInfo(@Param("studentId") Long studentId,
                                           @Param("termInfo") String termInfo);

    /**
     * 根据课程ID和学期信息查询成绩
     *
     * @param courseId 课程ID
     * @param termInfo 学期信息
     * @return 成绩列表
     */
    List<Score> findByCourseIdAndTermInfo(@Param("courseId") Long courseId,
                                          @Param("termInfo") String termInfo);

    /**
     * 根据学生ID、课程ID和学期信息查询成绩
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @param termInfo  学期信息
     * @return 成绩对象
     */
    Score findByStudentIdAndCourseIdAndTermInfo(@Param("studentId") Long studentId,
                                                @Param("courseId") Long courseId,
                                                @Param("termInfo") String termInfo);

    /**
     * 批量导入成绩
     *
     * @param scores 成绩列表
     * @return 影响行数
     */
    int batchInsert(List<Score> scores);
}