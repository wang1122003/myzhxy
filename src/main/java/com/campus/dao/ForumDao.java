package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Forum;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛板块 DAO 接口
 */
@Mapper
public interface ForumDao extends BaseMapper<Forum> {
    // 继承 BaseMapper 已拥有基础 CRUD
} 