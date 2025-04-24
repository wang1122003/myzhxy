package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserDao extends BaseMapper<User> {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(String username);
    
    /**
     * 根据用户类型查询用户
     * @param userType 用户类型 (String, e.g., "Student", "Teacher")
     * @return 用户列表
     */
    List<User> findByUserType(String userType);
    
    /**
     * 根据状态查询用户
     * @param status 状态
     * @return 用户列表
     */
    List<User> findByStatus(Integer status);
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户对象
     */
    User login(@Param("username") String username, @Param("password") String password);
    
    /**
     * 搜索用户
     * @param keyword 关键词
     * @return 用户列表
     */
    List<User> searchUsers(@Param("keyword") String keyword);
    
    /**
     * 获取状态用户数
     * @param status 状态
     * @return 用户数量
     */
    int getStatusUserCount(Integer status);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean isEmailExists(String email);
    
    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean isPhoneExists(String phone);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 状态值
     * @return 影响行数
     */
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新密码
     *
     * @param id       用户ID
     * @param password 加密后的新密码
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 删除数量
     */
    int deleteBatchIds(@Param("ids") List<Long> ids);

    /**
     * 根据部门ID列表查询用户ID列表
     * (假设 User 表或关联表中有 department_id 字段)
     *
     * @param departmentIds 部门ID列表
     * @return 用户ID列表
     */
    List<Long> findUserIdsByDepartmentIds(@Param("departmentIds") List<Long> departmentIds);

    /**
     * 查询所有有效的用户ID列表
     * (可以根据需要添加过滤条件，例如只查询 status='Active' 的用户)
     *
     * @return 用户ID列表
     */
    List<Long> findAllUserIds();
}