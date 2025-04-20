package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
     * @param userType 用户类型 (使用 String: "Admin", "Student", "Teacher")
     * @return 用户列表
     */
    List<User> getUsersByType(String userType);
    
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
     * 批量删除用户
     * @param ids 用户ID数组
     * @return 是否成功
     */
    boolean batchDeleteUsers(Long[] ids);
    
    /**
     * 修改用户状态
     * @param id 用户ID
     * @param status 状态字符串 ("Active", "Inactive")
     * @return 是否成功
     */
    boolean updateUserStatus(Long id, String status);
    
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
     * 搜索用户 (旧接口，可能被 findUsersPage 替代)
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
     * @param userType 用户类型 (String)
     * @return 用户数量
     */
    long getUserCountByType(String userType);
    
    /**
     * 根据状态获取用户数量
     * @param status 状态 (String)
     * @return 用户数量
     */
    long getUserCountByStatus(String status);

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
     * 更新用户个人资料 (由用户自己操作)
     *
     * @param user 用户对象 (只包含允许用户修改的字段)
     * @return 是否成功
     */
    boolean updateUserProfile(User user);

    /**
     * 上传用户头像
     *
     * @param id        用户ID
     * @param avatarUrl 头像URL
     * @return 是否成功
     */
    boolean uploadAvatar(Long id, String avatarUrl);

    /**
     * 分页查询用户列表
     * @param page 当前页码
     * @param size 每页数量
     * @param keyword 搜索关键词 (可选, 搜索用户名、真实姓名或邮箱)
     * @param userType 用户类型 (可选, 0: Admin, 1: Student, 2: Teacher)
     * @return 分页结果对象
     */
    IPage<User> findUsersPage(int page, int size, String keyword, Integer userType);

    /**
     * 解析用户导入文件
     * @param file 上传的Excel文件
     * @param userType 要导入的用户类型 (0, 1, 2)
     * @return 解析出的用户列表
     * @throws Exception 文件解析错误
     */
    List<User> parseUserImportFile(MultipartFile file, Integer userType) throws Exception;

    /**
     * 批量添加用户 (用于导入)
     * @param users 用户列表
     * @return 是否成功
     */
    boolean batchAddUsers(List<User> users);

    /**
     * 检查管理员账号是否存在
     * @return 是否存在
     */
    boolean checkAdminExists();

    /**
     * 添加管理员用户 (特殊处理，如密码加密)
     *
     * @param admin 管理员用户对象
     * @return 是否成功
     */
    boolean addAdminUser(User admin);
}