package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.UserDao;
import com.campus.entity.User;
import com.campus.exception.CustomException;
import com.campus.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类 (使用明文密码)
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    @Override
    public User getUserById(Long id) {
        return getById(id);
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return getOne(queryWrapper);
    }

    /**
     * 查询所有用户
     * @return 用户列表
     */
    @Override
    public List<User> getAllUsers() {
        return list();
    }

    /**
     * 添加用户
     */
    @Override
    @Transactional
    public boolean addUser(User user) {
        if (getUserByUsername(user.getUsername()) != null) {
            log.error("尝试添加已存在的用户名: {}", user.getUsername());
            throw new CustomException("用户名已存在");
        }
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (user.getStatus() == null) {
            user.setStatus("Active");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("尝试添加用户 {} 时密码为空", user.getUsername());
            throw new CustomException("密码不能为空");
        }
        return save(user);
    }

    /**
     * 更新用户 (保存明文密码，如果提供了)
     */
    @Override
    public boolean updateUser(User user) {
        user.setUpdateTime(new Date());
        if (user.getPassword() != null && user.getPassword().isEmpty()) {
            user.setPassword(null);
        }
        return updateById(user);
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否成功
     */
    @Override
    public boolean deleteUser(Long id) {
        return removeById(id);
    }

    /**
     * 批量删除用户
     * @param ids 用户ID数组
     * @return 是否成功
     */
    @Override
    public boolean batchDeleteUsers(Long[] ids) {
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 修改用户状态 (使用 String)
     */
    @Override
    @Transactional
    public boolean updateUserStatus(Long id, String status) {
        if (!"Active".equalsIgnoreCase(status) && !"Inactive".equalsIgnoreCase(status)) {
            log.warn("无效的用户状态值: {}", status);
            return false; // Or throw exception
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getStatus, status)
                .set(User::getUpdateTime, new Date()); // Also update updateTime
        return update(updateWrapper);
    }

    /**
     * 修改密码 
     */
    @Override
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        User user = getById(id);
        if (user == null) {
            log.error("更新密码失败: 用户 {} 不存在", id);
            return false;
        }
        if (!Objects.equals(oldPassword, user.getPassword())) {
            log.warn("用户 {} 尝试更新密码失败: 旧密码不匹配", id);
            return false;
        }
        user.setPassword(newPassword);
        user.setUpdateTime(new Date());
        boolean success = updateById(user);
        if (!success) {
            log.error("更新用户 {} 密码到数据库失败", id);
        }
        return success;
    }

    /**
     * 用户登录
     */
    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = getOne(queryWrapper);

        if (user == null) {
            log.warn("登录失败：用户 {} 不存在", username);
            return null;
        }
        if (!Objects.equals(password, user.getPassword())) {
            log.warn("用户 {} 登录失败：密码不匹配", username);
            return null;
        }
        if (!"Active".equalsIgnoreCase(user.getStatus())) {
            log.warn("用户 {} 登录失败，账户状态为: {}", username, user.getStatus());
            return null;
        }
        return user;
    }
    
    /**
     * 搜索用户
     * @param keyword 关键词
     * @return 匹配的用户列表
     */
    @Override
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getRealName, keyword)
                   .or()
                   .like(User::getEmail, keyword)
                   .or()
                   .like(User::getPhone, keyword);
        
        return list(queryWrapper);
    }
    
    /**
     * 重置用户密码 
     */
    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            log.error("重置密码失败: 用户 {} 不存在", id);
            return false;
        }
        user.setPassword(newPassword);
        user.setUpdateTime(new Date());
        boolean success = updateById(user);
        if (!success) {
            log.error("重置用户 {} 密码到数据库失败", id);
        }
        return success;
    }
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    @Override
    public long getUserCount() {
        return count();
    }
    
    /**
     * 根据用户类型获取用户数量 (使用 String)
     */
    @Override
    public long getUserCountByType(String userType) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userType)) {
            queryWrapper.eq(User::getUserType, userType);
        }
        return count(queryWrapper);
    }
    
    /**
     * 根据状态获取用户数量 (使用 String)
     */
    @Override
    public long getUserCountByStatus(String status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(User::getStatus, status);
        }
        return count(queryWrapper);
    }

    /**
     * 根据用户类型查询用户 (使用 String)
     */
    @Override
    public List<User> getUsersByType(String userType) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(userType)) {
            queryWrapper.eq(User::getUserType, userType);
        }
        return list(queryWrapper);
    }

    @Override
    public List<User> getAllStudents() {
        return getUsersByType("Student"); // Use String version
    }

    @Override
    public List<User> getAllTeachers() {
        return getUsersByType("Teacher"); // Use String version
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", getUserCount());
        stats.put("admins", getUserCountByType("Admin")); // Use String version
        stats.put("students", getUserCountByType("Student")); // Use String version
        stats.put("teachers", getUserCountByType("Teacher")); // Use String version
        stats.put("active", getUserCountByStatus("Active")); // Use String version
        stats.put("inactive", getUserCountByStatus("Inactive")); // Use String version
        return stats;
    }

    /**
     * 更新用户个人资料 (由用户自己操作)
     */
    @Override
    @Transactional
    public boolean updateUserProfile(User user) {
        if (user == null || user.getId() == null) {
            log.warn("尝试更新个人资料时用户或用户ID为空");
            return false;
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId());

        // 只更新允许修改的字段
        if (user.getRealName() != null) {
            updateWrapper.set(User::getRealName, user.getRealName());
        }
        if (user.getGender() != null) {
            updateWrapper.set(User::getGender, user.getGender());
        }
        if (user.getPhone() != null) {
            updateWrapper.set(User::getPhone, user.getPhone());
        }
        if (user.getEmail() != null) {
            updateWrapper.set(User::getEmail, user.getEmail());
        }
        // AvatarURL is often handled by a separate upload endpoint + updateAvatar method

        // 总是更新 updateTime
        updateWrapper.set(User::getUpdateTime, new Date());

        return update(updateWrapper);
    }

    @Override
    @Transactional
    public boolean uploadAvatar(Long userId, String avatarUrl) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getAvatarUrl, avatarUrl)
                .set(User::getUpdateTime, new Date());
        return update(updateWrapper);
    }

    public Map<String, String> getUserAvatars(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = list(new LambdaQueryWrapper<User>()
                .in(User::getId, userIds)
                .select(User::getId, User::getAvatarUrl));
        return users.stream()
                .collect(Collectors.toMap(user -> String.valueOf(user.getId()),
                        u -> u.getAvatarUrl() != null ? u.getAvatarUrl() : ""));
    }

    /**
     * 分页查询用户列表
     */
    @Override
    public IPage<User> findUsersPage(int page, int size, String keyword, Integer userTypeCode) {
        Page<User> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();

        // 处理用户类型转换: Integer (0,1,2) to String ("Admin", "Student", "Teacher")
        String userTypeString = null;
        if (userTypeCode != null) {
            if (userTypeCode == 0) userTypeString = "Admin";
            else if (userTypeCode == 1) userTypeString = "Student";
            else if (userTypeCode == 2) userTypeString = "Teacher";
            else log.warn("无效的用户类型代码: {}", userTypeCode);
        }

        if (StringUtils.hasText(userTypeString)) {
            queryWrapper.eq(User::getUserType, userTypeString);
        }

        // 处理关键词搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(qw -> qw.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword));
        }

        // 添加排序（例如，按创建时间降序）
        queryWrapper.orderByDesc(User::getCreateTime);

        return this.page(pageRequest, queryWrapper);
    }

    /**
     * 解析用户导入文件
     */
    @Override
    public List<User> parseUserImportFile(MultipartFile file, Integer userType) throws Exception {
        log.warn("用户文件导入功能已移除");
        throw new UnsupportedOperationException("用户文件导入功能已移除");
    }

    /**
     * 批量添加用户 (用于导入)
     */
    @Override
    @Transactional
    public boolean batchAddUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            return true; // Nothing to add
        }
        Date now = new Date();
        users.forEach(user -> {
            user.setCreateTime(now);
            user.setUpdateTime(now);
            if (user.getStatus() == null) user.setStatus("Active");
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                log.warn("批量添加时，用户 {} 密码为空，操作可能失败或跳过", user.getUsername());
            }
        });
        return saveBatch(users);
    }

    /**
     * 检查管理员账号是否存在
     */
    @Override
    public boolean checkAdminExists() {
        return count(Wrappers.<User>lambdaQuery().eq(User::getUserType, "Admin")) > 0;
    }

    /**
     * 添加管理员用户
     */
    @Override
    @Transactional
    public boolean addAdminUser(User admin) {
        if (!"Admin".equals(admin.getUserType())) {
            log.error("尝试使用 addAdminUser 方法添加非管理员用户: {}", admin.getUsername());
            return false;
        }
        if (getUserByUsername(admin.getUsername()) != null) {
            log.error("尝试添加已存在的管理员用户名: {}", admin.getUsername());
            return false;
        }
        Date now = new Date();
        admin.setCreateTime(now);
        admin.setUpdateTime(now);
        if (admin.getStatus() == null) admin.setStatus("Active");
        if (admin.getPassword() == null || admin.getPassword().isEmpty()) {
            log.error("尝试添加管理员 {} 时密码为空", admin.getUsername());
            throw new CustomException("管理员密码不能为空");
        }
        return save(admin);
    }
}