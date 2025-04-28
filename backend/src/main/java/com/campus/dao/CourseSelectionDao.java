package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.CourseSelection;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 选课信息 数据访问层
 */
@Mapper
@Repository
public interface CourseSelectionDao extends BaseMapper<CourseSelection> {

} 