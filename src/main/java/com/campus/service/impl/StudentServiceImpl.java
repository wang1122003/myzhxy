package com.campus.service.impl;

import com.campus.enums.StatusEnum;
import com.campus.mapper.StudentMapper;
import com.campus.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.List;

import com.campus.entity.Student;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentMapper.selectList(null);
    }

    @Override
    public List<Student> getActiveStudents() {
        return studentMapper.selectStudentsByStatus(StatusEnum.STUDENT_ACTIVE.getCode());
    }

    @Override
    public boolean validateStudentStatus(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        return student != null && student.getStatus() == StatusEnum.STUDENT_ACTIVE.getCode();
    }
}