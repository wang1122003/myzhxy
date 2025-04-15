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
     * 根据角色查询用户
     * @param role 角色
     * @return 用户列表
     */
    List<User> findByRole(String role);
    
    /**
     * 根据用户类型查询用户
     * @param userType 用户类型
     * @return 用户列表
     */
    List<User> findByUserType(Integer userType);
    
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
     * 获取角色用户数
     * @param role 角色
     * @return 用户数量
     */
    int getRoleUserCount(String role);
    
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
}