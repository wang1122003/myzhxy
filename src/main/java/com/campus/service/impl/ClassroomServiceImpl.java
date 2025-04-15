package com.campus.service.impl;

import com.campus.dao.ClassroomDao;
import com.campus.entity.Classroom;
import com.campus.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 教室服务实现类
 */
@Service
public class ClassroomServiceImpl implements ClassroomService {

    @Autowired
    private ClassroomDao classroomDao;

    @Override
    public Classroom getClassroomById(Long id) {
        return classroomDao.findById(id);
    }

    @Override
    public Classroom getClassroomByRoomNo(String roomNo) {
        return classroomDao.findByRoomNo(roomNo);
    }

    @Override
    public List<Classroom> getAllClassrooms() {
        return classroomDao.findAll();
    }

    @Override
    public List<Classroom> getClassroomsByBuilding(String building) {
        // 假设这里需要根据教学楼名称查询，而DAO层是根据教学楼ID查询
        // 实际实现可能需要先查询教学楼ID，然后再查询教室
        // 这里简化处理，直接返回空列表
        return List.of();
    }

    @Override
    public List<Classroom> getClassroomsByRoomType(Integer roomType) {
        return classroomDao.findByRoomType(roomType);
    }

    @Override
    public List<Classroom> getClassroomsByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        return classroomDao.findByCapacityRange(minCapacity, maxCapacity);
    }

    @Override
    public List<Classroom> getAvailableClassrooms() {
        // 默认返回当天第一节课可用的教室，星期几参数设为1（周一），时间段参数设为1（第一节课）
        return classroomDao.findAvailable(1, 1);
    }

    @Override
    @Transactional
    public boolean addClassroom(Classroom classroom) {
        // 设置创建时间和更新时间
        Date now = new Date();
        classroom.setCreateTime(now);
        classroom.setUpdateTime(now);
        
        // 如果没有设置状态，默认为可用
        if (classroom.getStatus() == null) {
            classroom.setStatus(1);
        }
        
        return classroomDao.insert(classroom) > 0;
    }

    @Override
    @Transactional
    public boolean updateClassroom(Classroom classroom) {
        // 设置更新时间
        classroom.setUpdateTime(new Date());
        
        return classroomDao.update(classroom) > 0;
    }

    @Override
    @Transactional
    public boolean deleteClassroom(Long id) {
        return classroomDao.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteClassrooms(Long[] ids) {
        return classroomDao.batchDelete(ids) > 0;
    }

    @Override
    @Transactional
    public boolean updateClassroomStatus(Long id, Integer status) {
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setStatus(status);
        classroom.setUpdateTime(new Date());
        
        return classroomDao.update(classroom) > 0;
    }
}