package com.campus.dao;

import org.apache.ibatis.annotations.Param;

import com.campus.entity.Department;

import java.util.List;
import java.util.Map;

/**
 * 部门数据访问接口
 */
public interface DepartmentDao {
    /**
     * 获取所有部门
     * @return 部门列表
     */
    List<Department> getAllDepartments();
    
    /**
     * 根据ID获取部门
     * @param id 部门ID
     * @return 部门对象
     */
    Department getDepartmentById(Integer id);
    
    /**
     * 根据编码获取部门
     * @param code 部门编码
     * @return 部门对象
     */
    Department getDepartmentByCode(String code);
    
    /**
     * 根据父部门ID获取子部门
     * @param parentId 父部门ID
     * @return 部门列表
     */
    List<Department> getDepartmentsByParentId(Integer parentId);
    
    /**
     * 根据层级获取部门
     * @param level 层级
     * @return 部门列表
     */
    List<Department> getDepartmentsByLevel(Integer level);
    
    /**
     * 根据负责人ID获取部门
     * @param leaderId 负责人ID
     * @return 部门列表
     */
    List<Department> getDepartmentsByLeaderId(Integer leaderId);
    
    /**
     * 根据状态获取部门
     * @param status 状态
     * @return 部门列表
     */
    List<Department> getDepartmentsByStatus(Integer status);
    
    /**
     * 搜索部门
     * @param keyword 关键词
     * @return 部门列表
     */
    List<Department> searchDepartments(String keyword);
    
    /**
     * 添加部门
     * @param department 部门对象
     * @return 影响行数
     */
    int addDepartment(Department department);
    
    /**
     * 更新部门
     * @param department 部门对象
     * @return 影响行数
     */
    int updateDepartment(Department department);
    
    /**
     * 删除部门
     * @param id 部门ID
     * @return 影响行数
     */
    int deleteDepartment(Integer id);
    
    /**
     * 更新部门状态
     * @param id 部门ID
     * @param status 状态
     * @return 影响行数
     */
    int updateDepartmentStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    /**
     * 检查部门编码是否存在
     * @param code 部门编码
     * @param id 部门ID（可选）
     * @return 是否存在
     */
    boolean isDepartmentCodeExists(@Param("code") String code, @Param("id") Integer id);
    
    /**
     * 获取部门统计信息
     * @return 统计信息Map
     */
    Map<String, Object> getDepartmentStats();
    
    /**
     * 获取部门层级统计信息
     * @return 统计信息Map列表
     */
    List<Map<String, Object>> getDepartmentLevelStats();
    
    /**
     * 获取负责人统计信息
     * @return 统计信息Map列表
     */
    List<Map<String, Object>> getLeaderStats();
    
    /**
     * 获取部门树结构
     * @return 部门列表（树结构）
     */
    List<Department> getDepartmentTree();
    
    /**
     * 获取部门及其所有子部门
     * @param id 部门ID
     * @return 部门列表
     */
    List<Department> getDepartmentWithChildren(Integer id);
} 