package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ActivityParticipant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动参与者 DAO 接口
 */
@Mapper
public interface ActivityParticipantDao extends BaseMapper<ActivityParticipant> {

    // 如果需要自定义 SQL 查询，可以在这里添加方法，并在对应的 Mapper XML 中实现
    // 例如：根据用户ID查询参与的活动ID列表
    // List<Long> findActivityIdsByUserId(@Param("userId") Long userId);
} 