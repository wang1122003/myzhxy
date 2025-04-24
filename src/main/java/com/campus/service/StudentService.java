package com.campus.service;

import com.campus.entity.Student;

import java.util.List;

public interface StudentService {
    List<Student> getAllStudents();

    List<Student> getActiveStudents();

    boolean validateStudentStatus(Long studentId);
}