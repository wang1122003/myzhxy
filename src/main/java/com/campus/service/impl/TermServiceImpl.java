package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.TermDao;
import com.campus.entity.Term;
import com.campus.service.TermService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学期服务实现类
 */
@Service
public class TermServiceImpl extends ServiceImpl<TermDao, Term> implements TermService {

    @Override
    public List<Term> getAllTerms(boolean orderByCreateTimeDesc) {
        LambdaQueryWrapper<Term> wrapper = new LambdaQueryWrapper<>();
        if (orderByCreateTimeDesc) {
            wrapper.orderByDesc(Term::getCreateTime);
        } else {
            // 默认可以按 start_date 或 code 排序
            wrapper.orderByAsc(Term::getStartDate, Term::getCode);
        }
        return list(wrapper);
    }

    @Override
    public Term getCurrentTerm() {
        LambdaQueryWrapper<Term> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Term::getCurrent, 1); // 假设 1 表示当前学期
        wrapper.last("LIMIT 1"); // 理论上只有一个当前学期，加个限制
        return getOne(wrapper);
    }

    /**
     * 根据学期代码获取学期
     * @param code 学期代码
     * @return 学期对象或null
     */
    @Override
    public Term getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Term> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Term::getCode, code);
        wrapper.last("LIMIT 1"); // 学期代码应该是唯一的
        return getOne(wrapper);
    }
} 