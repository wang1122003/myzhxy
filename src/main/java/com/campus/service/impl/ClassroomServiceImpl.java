package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ClassroomDao;
import com.campus.entity.Classroom;
import com.campus.exception.CustomException;
import com.campus.service.ClassroomService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 教室服务实现类
 */
@Slf4j
@Service
public class ClassroomServiceImpl extends ServiceImpl<ClassroomDao, Classroom> implements ClassroomService {

    @Autowired
    private ClassroomDao classroomDao;

    @Override
    public Classroom getClassroomById(Long id) {
        return getById(id);
    }

    @Override
    public Classroom getClassroomByName(String name) {
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Classroom::getName, name);
        return getOne(queryWrapper);
    }

    @Override
    public List<Classroom> getAllClassrooms() {
        return list();
    }

    @Override
    public IPage<Classroom> getClassroomsByPage(int page, int size, String keyword, String building, Integer status) {
        Page<Classroom> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(Classroom::getName, keyword)
                    .or()
                    .like(Classroom::getBuilding, keyword));
        }
        if (status != null) {
            queryWrapper.eq(Classroom::getStatus, status);
        }
        queryWrapper.orderByAsc(Classroom::getName);

        return this.page(pageRequest, queryWrapper);
    }

    @Override
    public List<Classroom> getAvailableClassrooms() {
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Classroom::getStatus, 1);
        queryWrapper.orderByAsc(Classroom::getName);
        return list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean addClassroom(Classroom classroom) {
        if (getClassroomByName(classroom.getName()) != null) {
            throw new CustomException("教室名称已存在");
        }
        return save(classroom);
    }

    @Override
    @Transactional
    public boolean updateClassroom(Classroom classroom) {
        Classroom existing = getClassroomByName(classroom.getName());
        if (existing != null && !existing.getId().equals(classroom.getId())) {
            throw new CustomException("教室名称已存在");
        }
        return updateById(classroom);
    }

    @Override
    @Transactional
    public boolean deleteClassroom(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkClassroomAvailability(Long classroomId, Date startTime, Date endTime, Integer weekDay) {
        log.warn("checkClassroomAvailability 方法尚未完全实现，总是返回 true");
        return true; 
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Classroom classroom = new Classroom();
        classroom.setStatus(status);
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Classroom::getId, ids);
        update(classroom, wrapper);
    }
}