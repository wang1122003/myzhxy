package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.User;
import com.campus.enums.UserStatus;
import com.campus.enums.UserType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户服务接口
 * 定义用户相关的业务逻辑操作，包含了学生和教师相关功能
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(Long id);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);

    /**
     * 根据学工号查询用户
     *
     * @param userNo 学工号
     * @return 用户对象
     */
    User getUserByUserNo(String userNo);
    
    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();
    
    /**
     * 根据用户类型查询用户
     * @param userType 用户类型 (Enum)
     * @return 用户列表
     */
    List<User> getUsersByType(UserType userType);
    
    /**
     * 添加用户 (通常指学生或教师)
     * @param user 用户对象
     * @return 是否成功
     */
    boolean addUser(User user);
    
    /**
     * 更新用户 (通用更新)
     * @param user 用户对象
     * @return 是否成功
     */
    boolean updateUser(User user);
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 根据ID批量删除用户
     */
    boolean batchDeleteUsers(Long[] ids);
    
    /**
     * 修改用户状态
     * @param id 用户ID
     * @param status 用户状态 (Enum)
     * @return 是否成功
     */
    boolean updateUserStatus(Long id, UserStatus status);
    
    /**
     * 修改密码
     */
    boolean updatePassword(Long id, String oldPassword, String newPassword);
    
    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 搜索用户
     */
    List<User> searchUsers(String keyword);
    
    /**
     * 重置用户密码
     */
    boolean resetPassword(Long id, String newPassword);
    
    /**
     * 获取用户总数
     */
    long getUserCount();

    /**
     * 根据类型获取用户数量
     * @param userType 用户类型 (Enum)
     * @return 用户数量
     */
    long getUserCountByType(UserType userType);
    
    /**
     * 根据状态获取用户数量
     * @param status 用户状态 (Enum)
     * @return 用户数量
     */
    long getUserCountByStatus(UserStatus status);

    /**
     * 获取所有学生
     */
    List<User> getAllStudents();

    /**
     * 获取所有教师
     */
    List<User> getAllTeachers();

    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStats();

    /**
     * 更新用户个人资料
     */
    boolean updateUserProfile(User user);

    /**
     * 分页查找用户
     * @param page 当前页码
     * @param size 每页数量
     * @param keyword 搜索关键词 (可选)
     * @param userType 用户类型 (Enum, 可选)
     * @return 分页结果对象
     */
    IPage<User> findUsersPage(int page, int size, String keyword, UserType userType);

    /**
     * 检查是否存在管理员
     */
    boolean checkAdminExists();

    /**
     * 添加管理员用户
     */
    boolean addAdminUser(User admin);

    /**
     * 根据ID列表获取用户
     */
    List<User> getUsersByIds(Set<Long> ids);

    /**
     * 根据用户名查找用户 (Spring Security需要)
     */
    User findByUsername(String username);
}