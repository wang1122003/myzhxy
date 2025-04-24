package com.campus.service.impl;

import com.campus.enums.StatusEnum;
import com.campus.entity.Teacher;
import com.campus.mapper.TeacherMapper;
import com.campus.service.TeacherService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {
    private final TeacherMapper teacherMapper;

    public TeacherServiceImpl(TeacherMapper teacherMapper) {
        this.teacherMapper = teacherMapper;
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherMapper.selectList(null);
    }

    @Override
    public List<Teacher> getActiveTeachers() {
        return teacherMapper.selectTeachersByStatus(StatusEnum.TEACHER_ACTIVE.getCode());
    }

    @Override
    public boolean validateTeacherStatus(Long teacherId) {
        Teacher teacher = teacherMapper.selectById(teacherId);
        return teacher != null && teacher.getStatus() == StatusEnum.TEACHER_ACTIVE.getCode();
    }
}