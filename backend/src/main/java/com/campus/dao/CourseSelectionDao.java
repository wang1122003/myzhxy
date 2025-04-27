package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.CourseSelection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选课信息 数据访问层
 */
@Mapper
public interface CourseSelectionDao extends BaseMapper<CourseSelection> {

} 