package com.campus.dao;

import org.apache.ibatis.annotations.Param;

import com.campus.entity.Teacher;

import java.util.List;

/**
 * 教师数据访问接口
 */
public interface TeacherDao {
    
    /**
     * 根据ID查询教师
     * @param id 教师ID
     * @return 教师对象
     */
    Teacher findById(Long id);
    
    /**
     * 根据工号查询教师
     * @param teacherNo 工号
     * @return 教师对象
     */
    Teacher findByTeacherNo(String teacherNo);
    
    /**
     * 查询所有教师
     * @return 教师列表
     */
    List<Teacher> findAll();
    
    /**
     * 根据部门ID查询教师
     * @param departmentId 部门ID
     * @return 教师列表
     */
    List<Teacher> findByDepartmentId(Long departmentId);
    
    /**
     * 根据职称ID查询教师
     * @param titleId 职称ID
     * @return 教师列表
     */
    List<Teacher> findByTitleId(Long titleId);
    
    /**
     * 分页查询教师
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 教师列表
     */
    List<Teacher> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 获取教师总数
     * @return 教师数量
     */
    int getCount();
    
    /**
     * 插入教师
     * @param teacher 教师对象
     * @return 影响行数
     */
    int insert(Teacher teacher);
    
    /**
     * 更新教师
     * @param teacher 教师对象
     * @return 影响行数
     */
    int update(Teacher teacher);
    
    /**
     * 删除教师
     * @param id 教师ID
     * @return 影响行数
     */
    int delete(Long id);
    
    /**
     * 批量删除教师
     * @param ids 教师ID数组
     * @return 影响行数
     */
    int batchDelete(@Param("ids") Long[] ids);
    
    /**
     * 更新教师状态
     * @param id 教师ID
     * @param status 教师状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 搜索教师
     * @param keyword 关键词
     * @return 教师列表
     */
    List<Teacher> searchTeachers(@Param("keyword") String keyword);
    
    /**
     * 获取部门教师数
     * @param departmentId 部门ID
     * @return 教师数量
     */
    int getDepartmentTeacherCount(Long departmentId);
    
    /**
     * 获取职称教师数
     * @param titleId 职称ID
     * @return 教师数量
     */
    int getTitleTeacherCount(Long titleId);
    
    /**
     * 检查工号是否存在
     * @param teacherNo 工号
     * @return 是否存在
     */
    boolean isTeacherNoExists(String teacherNo);
    
    /**
     * 检查身份证号是否存在
     * @param idCard 身份证号
     * @return 是否存在
     */
    boolean isIdCardExists(String idCard);
}