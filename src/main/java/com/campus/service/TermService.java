package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Term;

import java.util.List;

/**
 * 学期服务接口
 */
public interface TermService extends IService<Term> {

    /**
     * 获取所有学期列表
     * @param orderByCreateTimeDesc 是否按创建时间降序排序
     * @return 学期列表
     */
    List<Term> getAllTerms(boolean orderByCreateTimeDesc);

    /**
     * 获取当前学期
     * @return 当前学期对象，如果未设置则返回null
     */
    Term getCurrentTerm();

    /**
     * 根据学期代码获取学期
     * @param code 学期代码 (e.g., "2023-2024-1")
     * @return 对应的学期对象，如果找不到则返回null
     */
    Term getByCode(String code);

    // 未来可以添加更多方法，例如按状态查询、设置当前学期等
} 