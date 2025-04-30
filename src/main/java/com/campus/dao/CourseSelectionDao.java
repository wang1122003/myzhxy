package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.CourseSelection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 选课信息 数据访问层
 */
@Mapper
@Repository
public interface CourseSelectionDao extends BaseMapper<CourseSelection> {
    /**
     * 根据用户ID和学期信息查询选课记录 (只查询有效状态的选课)
     *
     * @param userId   用户ID
     * @param termInfo 学期信息
     * @return 选课记录列表
     */
    List<CourseSelection> findByUserIdAndTerm(@Param("userId") Long userId, @Param("termInfo") String termInfo);
} 