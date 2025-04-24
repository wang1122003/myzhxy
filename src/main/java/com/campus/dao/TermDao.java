package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Term;
import org.springframework.stereotype.Repository;

/**
 * 学期数据访问接口
 */
@Repository
public interface TermDao extends BaseMapper<Term> {
    // 暂时不需要自定义方法，使用 BaseMapper 的即可
    // 如果需要复杂查询，可以在这里添加方法并在 TermMapper.xml 中实现
} 