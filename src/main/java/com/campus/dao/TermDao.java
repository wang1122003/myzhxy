package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Term;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学期数据访问接口
 */
@Mapper
public interface TermDao extends BaseMapper<Term> {

    /**
     * 获取当前学期
     *
     * @return 当前学期对象
     */
    @Select("SELECT * FROM term WHERE is_current = 1 AND status = 1 LIMIT 1")
    Term getCurrentTerm();

    /**
     * 根据学期代码获取学期
     *
     * @param code 学期代码
     * @return 学期对象
     */
    @Select("SELECT * FROM term WHERE code = #{code}")
    Term getTermByCode(@Param("code") String code);

    /**
     * 获取所有启用的学期列表
     *
     * @return 学期列表
     */
    @Select("SELECT * FROM term WHERE status = 1 ORDER BY code DESC")
    List<Term> getAllActiveTerm();

    /**
     * 获取特定学年的学期列表
     *
     * @param academicYear 学年，如 2023-2024
     * @return 学期列表
     */
    @Select("SELECT * FROM term WHERE academic_year = #{academicYear} AND status = 1 ORDER BY term_number ASC")
    List<Term> getTermsByAcademicYear(@Param("academicYear") String academicYear);
} 