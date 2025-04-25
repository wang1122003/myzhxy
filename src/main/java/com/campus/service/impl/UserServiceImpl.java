package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.UserDao;
import com.campus.entity.User;
import com.campus.enums.UserStatus;
import com.campus.enums.UserType;
import com.campus.exception.CustomException;
import com.campus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 用户服务实现类 (使用明文密码)
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    @Override
    public User getUserById(Long id) {
        User user = getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
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
            throw new CustomException("用户名已存在");
        }
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }
        if (user.getUserType() == null) {
            throw new CustomException("用户类型不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
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
     * 修改用户状态 (使用 Enum)
     */
    @Override
    @Transactional
    public boolean updateUserStatus(Long id, UserStatus status) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getStatus, status)
                .set(User::getUpdateTime, new Date());
        return update(updateWrapper);
    }

    /**
     * 修改密码 
     */
    @Override
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        User user = getById(id);
        if (user == null) {
            return false;
        }
        if (!Objects.equals(oldPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(newPassword);
        user.setUpdateTime(new Date());
        boolean success = updateById(user);
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
            return null;
        }
        if (!Objects.equals(password, user.getPassword())) {
            return null;
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
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
            return false;
        }
        user.setPassword(newPassword);
        user.setUpdateTime(new Date());
        boolean success = updateById(user);
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
     * 根据用户类型获取用户数量 (使用 Enum)
     */
    @Override
    public long getUserCountByType(UserType userType) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (userType != null) {
            queryWrapper.eq(User::getUserType, userType);
        }
        return count(queryWrapper);
    }
    
    /**
     * 根据状态获取用户数量 (使用 Enum)
     */
    @Override
    public long getUserCountByStatus(UserStatus status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        return count(queryWrapper);
    }

    /**
     * 根据用户类型查询用户 (使用 Enum)
     */
    @Override
    public List<User> getUsersByType(UserType userType) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (userType != null) {
            queryWrapper.eq(User::getUserType, userType);
        }
        return list(queryWrapper);
    }

    @Override
    public List<User> getAllStudents() {
        return getUsersByType(UserType.STUDENT);
    }

    @Override
    public List<User> getAllTeachers() {
        return getUsersByType(UserType.TEACHER);
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", getUserCount());
        stats.put("admins", getUserCountByType(UserType.ADMIN));
        stats.put("students", getUserCountByType(UserType.STUDENT));
        stats.put("teachers", getUserCountByType(UserType.TEACHER));
        stats.put("active", getUserCountByStatus(UserStatus.ACTIVE));
        stats.put("inactive", getUserCountByStatus(UserStatus.INACTIVE));
        return stats;
    }

    /**
     * 更新用户个人资料 (由用户自己操作)
     */
    @Override
    @Transactional
    public boolean updateUserProfile(User user) {
        if (user == null || user.getId() == null) {
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
        // AvatarURL field does not exist, removed related logic

        // 总是更新 updateTime
        updateWrapper.set(User::getUpdateTime, new Date());

        return update(updateWrapper);
    }

    /**
     * 分页查询用户列表 (Use Enum for userType filter)
     */
    @Override
    public IPage<User> findUsersPage(int page, int size, String keyword, UserType userType) {
        Page<User> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();

        // Use Enum userType directly
        if (userType != null) {
            queryWrapper.eq(User::getUserType, userType);
        }

        // 处理关键词搜索 (Assuming these fields exist)
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
     * 检查管理员账号是否存在 (Use Enum)
     */
    @Override
    public boolean checkAdminExists() {
        return count(Wrappers.<User>lambdaQuery().eq(User::getUserType, UserType.ADMIN)) > 0;
    }

    /**
     * 添加管理员用户 (Check using Enum)
     */
    @Override
    @Transactional
    public boolean addAdminUser(User admin) {
        // 校验管理员类型
        if (admin.getUserType() != UserType.ADMIN) {
            throw new CustomException("只能添加管理员类型的用户");
        }
        // 检查用户名是否已存在
        if (getUserByUsername(admin.getUsername()) != null) {
            throw new CustomException("用户名 " + admin.getUsername() + " 已存在");
        }
        // 检查是否已有管理员存在
        if (checkAdminExists()) {
            throw new CustomException("系统已存在管理员");
        }

        // 设置默认值
        Date now = new Date();
        admin.setCreateTime(now);
        admin.setUpdateTime(now);
        if (admin.getStatus() == null) {
            admin.setStatus(UserStatus.ACTIVE);
        }
        if (admin.getPassword() == null || admin.getPassword().isEmpty()) {
            throw new CustomException("管理员密码不能为空");
        }
        // 保存管理员用户
        return save(admin);
    }

    @Override
    public List<User> getUsersByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, ids);
        return list(queryWrapper);
    }

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体，或null如果找不到
     */
    @Override
    public User findByUsername(String username) {
        return getUserByUsername(username);
    }

    /**
     * 验证并重置密码 (示例性，具体逻辑可能需要调整)
     *
     * @param username    用户名
     * @param newPassword 新密码
     */
    public void verifyPasswordReset(String username, String newPassword) {
        User user = findByUsername(username);
        if (user == null) {
            throw new CustomException("用户不存在");
        }
        // 这里可以添加更多的验证逻辑，例如验证 token 等
        resetPassword(user.getId(), newPassword);
    }

    @Override
    public User getUserByUserNo(String userNo) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserNo, userNo);
        return getOne(queryWrapper);
    }
}