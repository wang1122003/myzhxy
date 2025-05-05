package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.TermDao;
import com.campus.entity.Term;
import com.campus.exception.CustomException;
import com.campus.service.TermService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.campus.dao.CourseDao;
import com.campus.dao.CourseSelectionDao;
import com.campus.dao.ScheduleDao;
import com.campus.entity.Course;
import com.campus.entity.CourseSelection;
import com.campus.entity.Schedule;

import java.util.ArrayList;

/**
 * 学期服务实现类
 */
@Service
public class TermServiceImpl extends ServiceImpl<TermDao, Term> implements TermService {

    private static final Logger log = LoggerFactory.getLogger(TermServiceImpl.class);

    @Autowired
    private TermDao termDao;

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private CourseSelectionDao courseSelectionDao;

    @Autowired
    private ScheduleDao scheduleDao;

    @Override
    public List<Term> getAllTerms() {
        return list(Wrappers.<Term>lambdaQuery().orderByDesc(Term::getCode));
    }

    @Override
    public List<Term> getAllActiveTerms() {
        return termDao.getAllActiveTerm();
    }

    @Override
    public Term getCurrentTerm() {
        return termDao.getCurrentTerm();
    }

    @Override
    public Term getTermById(Long id) {
        return getById(id);
    }

    @Override
    public Term getTermByCode(String code) {
        return termDao.getTermByCode(code);
    }

    @Override
    @Transactional
    public boolean addTerm(Term term) {
        if (term == null) {
            log.error("添加学期失败：学期对象为空");
            return false;
        }

        // 检查学期代码是否已存在
        Term existingTerm = termDao.getTermByCode(term.getCode());
        if (existingTerm != null) {
            log.error("添加学期失败：学期代码 {} 已存在", term.getCode());
            throw new CustomException("学期代码已存在，请使用其他代码");
        }

        // 设置默认值
        if (term.getStatus() == null) {
            term.setStatus(1);
        }
        if (term.getIsCurrent() == null) {
            term.setIsCurrent(0);
        }

        // 如果设置为当前学期，需要将其他学期的当前标志设为0
        if (term.getIsCurrent() == 1) {
            update(Wrappers.<Term>lambdaUpdate().set(Term::getIsCurrent, 0));
        }

        // 设置创建和更新时间
        Date now = new Date();
        term.setCreationTime(now);
        term.setLastUpdateTime(now);

        return save(term);
    }

    @Override
    @Transactional
    public boolean updateTerm(Term term) {
        if (term == null || term.getId() == null) {
            log.error("更新学期失败：学期对象为空或ID为空");
            return false;
        }

        // 检查学期是否存在
        Term existingTerm = getById(term.getId());
        if (existingTerm == null) {
            log.error("更新学期失败：学期ID {} 不存在", term.getId());
            throw new CustomException("学期不存在");
        }

        // 检查学期代码是否已被其他学期使用
        Term codeExistingTerm = termDao.getTermByCode(term.getCode());
        if (codeExistingTerm != null && !codeExistingTerm.getId().equals(term.getId())) {
            log.error("更新学期失败：学期代码 {} 已被其他学期使用", term.getCode());
            throw new CustomException("学期代码已被其他学期使用，请使用其他代码");
        }

        // 如果设置为当前学期，需要将其他学期的当前标志设为0
        if (term.getIsCurrent() != null && term.getIsCurrent() == 1) {
            update(Wrappers.<Term>lambdaUpdate()
                    .ne(Term::getId, term.getId())
                    .set(Term::getIsCurrent, 0));
        }

        // 设置更新时间
        term.setLastUpdateTime(new Date());

        return updateById(term);
    }

    @Override
    @Transactional
    public boolean deleteTerm(Long id) {
        if (id == null) {
            log.error("删除学期失败：学期ID为空");
            return false;
        }

        // 检查学期是否存在
        Term existingTerm = getById(id);
        if (existingTerm == null) {
            log.warn("删除学期：学期ID {} 不存在，视为删除成功", id);
            return true;
        }

        // 检查是否为当前学期
        if (existingTerm.getIsCurrent() != null && existingTerm.getIsCurrent() == 1) {
            log.error("删除学期失败：不能删除当前学期 {}", existingTerm.getName());
            throw new CustomException("不能删除当前学期，请先设置其他学期为当前学期");
        }

        // 检查学期是否有关联数据（如课程、选课记录等），如果有则不允许删除或提示用户
        String termInfo = existingTerm.getCode();

        // 检查是否有关联的课程
        LambdaQueryWrapper<Course> courseQuery = new LambdaQueryWrapper<>();
        courseQuery.eq(Course::getTermInfo, termInfo);
        long courseCount = courseDao.selectCount(courseQuery);

        // 检查是否有关联的选课记录
        List<CourseSelection> courseSelections = courseSelectionDao.findByUserIdAndTerm(null, termInfo);
        long selectionCount = courseSelections != null ? courseSelections.size() : 0;

        // 检查是否有关联的课表记录
        List<Schedule> schedules = new ArrayList<>();
        List<Course> courses = courseDao.selectList(courseQuery);
        if (courses != null && !courses.isEmpty()) {
            List<Long> courseIds = new ArrayList<>();
            for (Course course : courses) {
                courseIds.add(course.getId());
            }

            if (!courseIds.isEmpty()) {
                schedules = scheduleDao.findSchedulesByCourseIdsAndTerm(courseIds, termInfo);
            }
        }
        long scheduleCount = schedules != null ? schedules.size() : 0;

        // 如果有关联数据，则不允许删除，抛出异常提示用户
        if (courseCount > 0 || selectionCount > 0 || scheduleCount > 0) {
            StringBuilder message = new StringBuilder("无法删除学期，存在关联数据：");
            if (courseCount > 0) {
                message.append(courseCount).append("门课程");
            }
            if (selectionCount > 0) {
                if (courseCount > 0) message.append("、");
                message.append(selectionCount).append("条选课记录");
            }
            if (scheduleCount > 0) {
                if (courseCount > 0 || selectionCount > 0) message.append("、");
                message.append(scheduleCount).append("条排课记录");
            }
            message.append("。请先删除相关数据再尝试删除学期。");

            log.error("删除学期失败：{} - {}", existingTerm.getName(), message);
            throw new CustomException(message.toString());
        }

        return removeById(id);
    }

    @Override
    @Transactional
    public boolean setCurrentTerm(Long id) {
        if (id == null) {
            log.error("设置当前学期失败：学期ID为空");
            return false;
        }

        // 检查学期是否存在
        Term term = getById(id);
        if (term == null) {
            log.error("设置当前学期失败：学期ID {} 不存在", id);
            throw new CustomException("学期不存在");
        }

        // 将所有学期的当前标志设为0
        update(Wrappers.<Term>lambdaUpdate().set(Term::getIsCurrent, 0));

        // 将指定学期的当前标志设为1
        return update(Wrappers.<Term>lambdaUpdate()
                .eq(Term::getId, id)
                .set(Term::getIsCurrent, 1));
    }

    @Override
    public Map<String, Object> getTermPage(int page, int size) {
        Page<Term> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Term> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Term::getCode);

        IPage<Term> resultPage = page(pageRequest, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("rows", resultPage.getRecords());
        return result;
    }

    @Override
    public List<Term> getTermsByAcademicYear(String academicYear) {
        return termDao.getTermsByAcademicYear(academicYear);
    }
} 