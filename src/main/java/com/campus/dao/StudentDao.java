package com.campus.dao;

import org.apache.ibatis.annotations.Param;

import com.campus.entity.Student;

import java.util.List;

/**
 * 学生数据访问接口
 */
public interface StudentDao {
    /**
     * 根据ID查询学生
     * @param id 学生ID
     * @return 学生对象
     */
    Student findById(Long id);
    
    /**
     * 查询所有学生
     * @return 学生列表
     */
    List<Student> findAll();
    
    /**
     * 根据学号查询学生
     * @param studentNo 学号
     * @return 学生对象
     */
    Student findByStudentNo(String studentNo);
    
    /**
     * 根据班级查询学生
     * @param className 班级名称
     * @return 学生列表
     */
    List<Student> findByClassName(String className);
    
    /**
     * 根据专业查询学生
     * @param major 专业
     * @return 学生列表
     */
    List<Student> findByMajor(String major);
    
    /**
     * 根据年级查询学生
     * @param grade 年级
     * @return 学生列表
     */
    List<Student> findByGrade(String grade);
    
    /**
     * 分页查询学生
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 学生列表
     */
    List<Student> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取学生总数
     * @return 学生数量
     */
    int getCount();
    
    /**
     * 添加学生
     * @param student 学生对象
     * @return 影响行数
     */
    int insert(Student student);
    
    /**
     * 更新学生
     * @param student 学生对象
     * @return 影响行数
     */
    int update(Student student);
    
    /**
     * 删除学生
     * @param id 学生ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 批量删除学生
     * @param ids 学生ID数组
     * @return 影响行数
     */
    int batchDelete(Long[] ids);
    
    /**
     * 更新学生状态
     * @param id 学生ID
     * @param status 学生状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 搜索学生
     * @param keyword 关键词
     * @return 学生列表
     */
    List<Student> searchStudents(@Param("keyword") String keyword);
    
    /**
     * 获取班级学生数
     * @param className 班级名称
     * @return 学生数量
     */
    int getClassNameStudentCount(String className);
    
    /**
     * 获取专业学生数
     * @param major 专业
     * @return 学生数量
     */
    int getMajorStudentCount(String major);
    
    /**
     * 获取年级学生数
     * @param grade 年级
     * @return 学生数量
     */
    int getGradeStudentCount(String grade);
    
    /**
     * 检查学号是否存在
     * @param studentNo 学号
     * @return 是否存在
     */
    boolean isStudentNoExists(String studentNo);
    
    /**
     * 检查身份证号是否存在
     * @param idCard 身份证号
     * @return 是否存在
     */
    boolean isIdCardExists(String idCard);
}