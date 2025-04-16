package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 * 定义用户相关的业务逻辑操作
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
     * 查询所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();
    
    /**
     * 根据用户类型查询用户
     * @param userType 用户类型
     * @return 用户列表
     */
    List<User> getUsersByType(Integer userType);
    
    /**
     * 添加用户
     * @param user 用户对象
     * @return 是否成功
     */
    boolean addUser(User user);
    
    /**
     * 更新用户
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
     * 批量删除用户
     * @param ids 用户ID数组
     * @return 是否成功
     */
    boolean batchDeleteUsers(Long[] ids);
    
    /**
     * 修改用户状态
     * @param id 用户ID
     * @param status 状态值
     * @return 是否成功
     */
    boolean updateUserStatus(Long id, Integer status);
    
    /**
     * 修改密码
     * @param id 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean updatePassword(Long id, String oldPassword, String newPassword);
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户对象，登录失败返回null
     */
    User login(String username, String password);
    
    /**
     * 搜索用户
     * @param keyword 关键词
     * @return 匹配的用户列表
     */
    List<User> searchUsers(String keyword);
    
    /**
     * 重置用户密码
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    long getUserCount();
    
    /**
     * 根据用户类型获取用户数量
     * @param userType 用户类型
     * @return 用户数量
     */
    long getUserCountByType(int userType);
    
    /**
     * 根据状态获取用户数量
     * @param status 状态
     * @return 用户数量
     */
    long getUserCountByStatus(int status);

    /**
     * 获取所有学生用户
     *
     * @return 学生用户列表
     */
    List<User> getAllStudents();

    /**
     * 获取所有教师用户
     *
     * @return 教师用户列表
     */
    List<User> getAllTeachers();

    /**
     * 获取用户统计信息
     *
     * @return 用户统计数据
     */
    Map<String, Object> getUserStats();

    /**
     * 更新用户个人资料
     *
     * @param user 用户对象
     * @return 是否成功
     */
    boolean updateProfile(User user);

    /**
     * 上传用户头像
     *
     * @param id        用户ID
     * @param avatarUrl 头像URL
     * @return 是否成功
     */
    boolean uploadAvatar(Long id, String avatarUrl);

    /**
     * 批量导入用户
     *
     * @param file     Excel文件
     * @param userType 用户类型
     * @return 导入结果
     */
    Map<String, Object> importUsers(MultipartFile file, Integer userType);

    /**
     * 批量导入用户
     *
     * @param users 用户列表
     * @return 导入结果
     */
    Map<String, Object> batchImportUsers(List<User> users);

    /**
     * 生成用户导入模板
     *
     * @return 模板文件URL
     */
    String generateImportTemplate();
}