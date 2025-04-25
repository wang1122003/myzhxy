package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.ClassroomDao;
import com.campus.entity.Classroom;
import com.campus.enums.ClassroomStatus;
import com.campus.exception.CustomException;
import com.campus.service.ClassroomService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 教室服务实现类
 */
@Service
public class ClassroomServiceImpl extends ServiceImpl<ClassroomDao, Classroom> implements ClassroomService {

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
    public IPage<Classroom> getClassroomsByPage(int page, int size, String keyword, String building, Integer status, String roomType) {
        Page<Classroom> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<Classroom> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(Classroom::getName, keyword)
                    .or()
                    .like(Classroom::getBuilding, keyword));
        }
        if (StringUtils.isNotBlank(building)) {
            queryWrapper.eq(Classroom::getBuilding, building);
        }
        if (status != null) {
            ClassroomStatus classroomStatus = ClassroomStatus.fromCode(status);
            if (classroomStatus != null) {
                queryWrapper.eq(Classroom::getStatus, classroomStatus);
            } else {
                log.warn("Invalid status code provided for classroom search: " + status);
            }
        }
        if (StringUtils.isNotBlank(roomType)) {
            try {
                Integer roomTypeCode = Integer.parseInt(roomType);
                queryWrapper.eq(Classroom::getRoomType, roomTypeCode);
            } catch (NumberFormatException e) {
                log.warn("Invalid roomType provided for classroom search: " + roomType);
            }
        }
        queryWrapper.orderByAsc(Classroom::getName);

        return this.page(pageRequest, queryWrapper);
    }

    @Override
    public List<Classroom> getAvailableClassrooms() {
        LambdaQueryWrapper<Classroom> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Classroom::getStatus, ClassroomStatus.NORMAL);
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
    public boolean checkClassroomAvailability(Long classroomId, Date startTime, Date endTime, String dayOfWeek) {
        // checkClassroomAvailability 方法尚未完全实现，总是返回 true
        log.warn("checkClassroomAvailability is not fully implemented.");
        return true;
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        ClassroomStatus classroomStatus = ClassroomStatus.fromCode(status);
        if (classroomStatus == null) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        if (ids != null && !ids.isEmpty()) {
            LambdaUpdateWrapper<Classroom> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.in(Classroom::getId, ids)
                    .set(Classroom::getStatus, classroomStatus);
            update(updateWrapper);
        }
    }

    @Override
    @Transactional
    public boolean batchDeleteClassrooms(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true; // 或者根据业务返回 false 或抛异常
        }
        // TODO: 检查这些教室是否被排课占用，如果占用则不允许删除或抛异常
        // boolean isScheduled = scheduleService.isAnyClassroomScheduled(ids);
        // if (isScheduled) { throw new CustomException("部分教室已被排课使用，无法删除"); }
        return this.removeByIds(ids);
    }
}