package com.campus.service;

import com.campus.entity.Term;

import java.util.List;
import java.util.Map;

/**
 * 学期服务接口
 */
public interface TermService {

    /**
     * 获取所有学期列表
     *
     * @return 学期列表
     */
    List<Term> getAllTerms();

    /**
     * 获取所有启用的学期列表
     *
     * @return 学期列表
     */
    List<Term> getAllActiveTerms();

    /**
     * 获取当前学期
     *
     * @return 当前学期
     */
    Term getCurrentTerm();

    /**
     * 根据ID获取学期
     *
     * @param id 学期ID
     * @return 学期对象
     */
    Term getTermById(Long id);

    /**
     * 根据学期代码获取学期
     *
     * @param code 学期代码
     * @return 学期对象
     */
    Term getTermByCode(String code);

    /**
     * 添加学期
     *
     * @param term 学期对象
     * @return 是否成功
     */
    boolean addTerm(Term term);

    /**
     * 更新学期
     *
     * @param term 学期对象
     * @return 是否成功
     */
    boolean updateTerm(Term term);

    /**
     * 删除学期
     *
     * @param id 学期ID
     * @return 是否成功
     */
    boolean deleteTerm(Long id);

    /**
     * 设置当前学期
     *
     * @param id 学期ID
     * @return 是否成功
     */
    boolean setCurrentTerm(Long id);

    /**
     * 获取学期分页列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Map<String, Object> getTermPage(int page, int size);

    /**
     * 获取特定学年的学期列表
     *
     * @param academicYear 学年
     * @return 学期列表
     */
    List<Term> getTermsByAcademicYear(String academicYear);
} 