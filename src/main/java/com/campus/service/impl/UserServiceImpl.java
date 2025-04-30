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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据ID查询用户 (清除密码)
     */
    @Override
    public User getUserById(Long id) {
        User user = getById(id);
        if (user != null) {
            user.setPassword(null); // Never return password hash
        }
        return user;
    }

    /**
     * 根据用户名查询用户 (内部使用，可能包含密码)
     */
    @Override
    public User getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) return null;
        // Explicitly select columns to avoid errors with removed fields
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(User::getUsername, username)
                .select(User.class, info -> !info.getColumn().equals("nickname") && !info.getColumn().equals("birth_date"));
        return getOne(queryWrapper);
    }

    /**
     * 查询所有用户 (清除密码)
     */
    @Override
    public List<User> getAllUsers() {
        List<User> users = list();
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    /**
     * 添加用户 (密码需处理)
     */
    @Override
    @Transactional
    public boolean addUser(User user) {
        // Validation
        if (user == null || !StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new CustomException("用户名和密码不能为空");
        }
        if (user.getUserType() == null) {
            throw new CustomException("用户类型不能为空");
        }
        // Check uniqueness using DAO count methods
        if (userDao.countByUsername(user.getUsername()) > 0) {
            throw new CustomException("用户名已存在: " + user.getUsername());
        }
        if (StringUtils.hasText(user.getEmail()) && userDao.countByEmail(user.getEmail()) > 0) {
            throw new CustomException("邮箱已存在: " + user.getEmail());
        }
        if (StringUtils.hasText(user.getPhone()) && userDao.countByPhone(user.getPhone()) > 0) {
            throw new CustomException("手机号已存在: " + user.getPhone());
        }
        if (StringUtils.hasText(user.getUserNo()) && userDao.countByUserNo(user.getUserNo()) > 0) {
            throw new CustomException("学工号已存在: " + user.getUserNo());
        }

        // Set defaults
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setStatus(user.getStatus() == null ? UserStatus.ACTIVE : user.getStatus());

        // TODO: [Security] Encode password before saving - MODIFIED for NoOpPasswordEncoder
        if (StringUtils.hasText(user.getPassword())) {
            // user.setPassword(passwordEncoder.encode(user.getPassword())); // Original BCrypt encode
            // No encoding needed for NoOpPasswordEncoder
            user.setPassword(user.getPassword()); // Keep the plain text password
        } else {
            // Handle case where password is required but not provided
            // Or set a default password? For now, assume password is provided.
            throw new CustomException("创建用户时必须提供密码");
        }

        return save(user);
    }

    /**
     * 更新用户 (密码可选更新，需处理)
     */
    @Override
    @Transactional
    public boolean updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new CustomException("更新用户信息失败：用户ID不能为空");
        }
        // Fetch existing user to prevent overwriting critical fields unintentionally
        User existingUser = getById(user.getId());
        if (existingUser == null) {
            throw new CustomException("更新用户信息失败：用户不存在 (ID: " + user.getId() + ")");
        }

        // Prevent changing username?
        user.setUsername(existingUser.getUsername());
        // Prevent changing createTime
        user.setCreateTime(existingUser.getCreateTime());

        // Set update time
        user.setUpdateTime(new Date());

        // Handle password update: only update if a non-empty password is provided
        if (StringUtils.hasText(user.getPassword())) {
            // TODO: [Security] Encode the new password - MODIFIED for NoOpPasswordEncoder
            // user.setPassword(passwordEncoder.encode(user.getPassword())); // Original BCrypt encode
            user.setPassword(user.getPassword()); // Save plain text password
        } else {
            // If password in request is null or empty, retain the existing password
            user.setPassword(existingUser.getPassword());
        }

        // Check uniqueness for fields that might change (email, phone, userNo)
        if (StringUtils.hasText(user.getEmail()) && !Objects.equals(user.getEmail(), existingUser.getEmail()) && userDao.countByEmail(user.getEmail()) > 0) {
            throw new CustomException("邮箱已存在: " + user.getEmail());
        }
        if (StringUtils.hasText(user.getPhone()) && !Objects.equals(user.getPhone(), existingUser.getPhone()) && userDao.countByPhone(user.getPhone()) > 0) {
            throw new CustomException("手机号已存在: " + user.getPhone());
        }
        if (StringUtils.hasText(user.getUserNo()) && !Objects.equals(user.getUserNo(), existingUser.getUserNo()) && userDao.countByUserNo(user.getUserNo()) > 0) {
            throw new CustomException("学工号已存在: " + user.getUserNo());
        }

        // Perform the update
        return updateById(user);
    }

    /**
     * 删除用户
     */
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        // TODO: Add checks? Can user be deleted? (e.g., if they have posts, courses etc.)
        if (id == null) return false;
        return removeById(id);
    }

    /**
     * 批量删除用户
     */
    @Override
    @Transactional
    public boolean batchDeleteUsers(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return true;
        }
        // TODO: Add checks before batch deleting?
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 修改用户状态 (使用 Enum)
     */
    @Override
    @Transactional
    public boolean updateUserStatus(Long id, UserStatus status) {
        if (id == null || status == null) {
            throw new IllegalArgumentException("用户ID和状态不能为空");
        }
        // Prevent setting invalid status? (Enum validation handles this)

        return update(Wrappers.<User>lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getStatus, status)
                .set(User::getUpdateTime, new Date()));
    }

    /**
     * 修改密码 (需要密码比对)
     */
    @Override
    @Transactional
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        if (id == null || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new CustomException("用户ID、旧密码和新密码不能为空");
        }
        User user = getById(id); // Fetch user with password hash
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // TODO: [Security] Compare oldPassword with stored hash using PasswordEncoder - MODIFIED for NoOpPasswordEncoder
        // if (!passwordEncoder.matches(oldPassword, user.getPassword())) { // Original BCrypt match
        if (!oldPassword.equals(user.getPassword())) { // Plain text comparison
            throw new CustomException("旧密码不正确");
        }

        // TODO: [Security] Encode the new password - MODIFIED for NoOpPasswordEncoder
        // String encodedNewPassword = passwordEncoder.encode(newPassword); // Original BCrypt encode
        String newPasswordPlainText = newPassword; // Use plain text

        // Update password and update time
        return update(Wrappers.<User>lambdaUpdate()
                .eq(User::getId, id)
                //.set(User::getPassword, encodedNewPassword)
                .set(User::getPassword, newPasswordPlainText) // Set plain text password
                .set(User::getUpdateTime, new Date()));
    }

    /**
     * 用户登录 (需要密码比对)
     */
    @Override
    public User login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return null; // Or throw exception
        }
        User user = getUserByUsername(username); // Fetch user with password hash

        if (user == null) {
            return null; // User not found
        }

        // TODO: [Security] Compare provided password with stored hash using PasswordEncoder - MODIFIED for NoOpPasswordEncoder
        // if (!passwordEncoder.matches(password, user.getPassword())) { // Original BCrypt match
        if (!password.equals(user.getPassword())) { // Plain text comparison
            return null; // Password mismatch
        }

        // Check user status
        if (user.getStatus() != UserStatus.ACTIVE) {
            // Corrected log.warn usage
            log.warn("用户登录失败，账户状态异常: username={}, status={}", username, user.getStatus() != null ? user.getStatus().getValue() : "null");
            // Depending on requirements, you might throw specific exceptions for different statuses
            // throw new CustomException("账户已被禁用");
            // throw new CustomException("账户待激活");
            return null; // Or throw specific exception
        }

        // Login successful, clear password before returning
        user.setPassword(null);
        return user;
    }

    /**
     * 搜索用户 (清除密码)
     */
    @Override
    public List<User> searchUsers(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllUsers(); // Return all users (passwords cleared) if keyword is empty
        }

        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .like(User::getUsername, keyword)
                .or().like(User::getRealName, keyword)
                .or().like(User::getEmail, keyword)
                .or().like(User::getPhone, keyword)
                .or().like(User::getUserNo, keyword); // Added userNo search

        List<User> users = list(queryWrapper);
        users.forEach(user -> user.setPassword(null)); // Clear passwords
        return users;
    }

    /**
     * 重置用户密码 (管理员操作，无需旧密码)
     */
    @Override
    @Transactional
    public boolean resetPassword(Long id, String newPassword) {
        if (id == null || !StringUtils.hasText(newPassword)) {
            throw new CustomException("用户ID和新密码不能为空");
        }
        // TODO: Add permission check - only admins should reset passwords

        // TODO: [Security] Encode the new password
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        return update(Wrappers.<User>lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getPassword, encodedNewPassword)
                .set(User::getUpdateTime, new Date()));
    }

    /**
     * 获取用户总数
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
        return count(Wrappers.<User>lambdaQuery().eq(userType != null, User::getUserType, userType));
    }

    /**
     * 根据状态获取用户数量 (使用 Enum)
     */
    @Override
    public long getUserCountByStatus(UserStatus status) {
        return count(Wrappers.<User>lambdaQuery().eq(status != null, User::getStatus, status));
    }

    /**
     * 根据类型获取用户列表 (清除密码)
     */
    @Override
    public List<User> getUsersByType(UserType userType) {
        List<User> users = list(Wrappers.<User>lambdaQuery().eq(userType != null, User::getUserType, userType));
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    /**
     * 获取所有学生 (清除密码)
     */
    @Override
    public List<User> getAllStudents() {
        return getUsersByType(UserType.STUDENT);
    }

    /**
     * 获取所有教师 (清除密码)
     */
    @Override
    public List<User> getAllTeachers() {
        return getUsersByType(UserType.TEACHER);
    }

    /**
     * 获取用户统计信息
     */
    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", getUserCount());
        stats.put("studentCount", getUserCountByType(UserType.STUDENT));
        stats.put("teacherCount", getUserCountByType(UserType.TEACHER));
        stats.put("adminCount", getUserCountByType(UserType.ADMIN));
        stats.put("activeUsers", getUserCountByStatus(UserStatus.ACTIVE));
        stats.put("inactiveUsers", getUserCountByStatus(UserStatus.INACTIVE));
        // stats.put("lockedUsers", getUserCountByStatus(UserStatus.LOCKED)); // Removed LOCKED status
        return stats;
    }

    /**
     * 更新用户个人资料 (不允许修改密码、用户名、状态、类型、创建时间)
     */
    @Override
    @Transactional
    public boolean updateUserProfile(User user) {
        if (user == null || user.getId() == null) {
            throw new CustomException("用户ID不能为空");
        }
        // TODO: Ensure the user ID matches the currently authenticated user
        // Long currentUserId = getCurrentUserId(); // Assuming such method exists
        // if (!user.getId().equals(currentUserId)) {
        //     throw new CustomException("无权修改他人资料");
        // }

        // Use LambdaUpdateWrapper to update only allowed fields
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.<User>lambdaUpdate()
                .eq(User::getId, user.getId());

        // Fields allowed to be updated in profile (Corrected usage of set)
        if (StringUtils.hasText(user.getRealName())) updateWrapper.set(User::getRealName, user.getRealName());
        // if (StringUtils.hasText(user.getNickname())) updateWrapper.set(User::getNickname, user.getNickname()); // 移除
        if (StringUtils.hasText(user.getEmail())) updateWrapper.set(User::getEmail, user.getEmail());
        if (user.getPhone() != null) updateWrapper.set(User::getPhone, user.getPhone());
        if (user.getGender() != null) updateWrapper.set(User::getGender, user.getGender());
        // 注释掉以下对已移除字段的引用 (约 415-423 行)
        /*
        if (StringUtils.hasText(user.getAvatarUrl())) updateWrapper.set(User::getAvatarUrl, user.getAvatarUrl());
        if (StringUtils.hasText(user.getAddress())) updateWrapper.set(User::getAddress, user.getAddress());
        // Assuming department is linked via departmentId, maybe update that?
        // if (user.getDepartmentId() != null) updateWrapper.set(User::getDepartmentId, user.getDepartmentId());
        if (StringUtils.hasText(user.getMajor())) updateWrapper.set(User::getMajor, user.getMajor());
        if (StringUtils.hasText(user.getClazz())) updateWrapper.set(User::getClazz, user.getClazz());
        if (StringUtils.hasText(user.getDescription())) updateWrapper.set(User::getDescription, user.getDescription());
        */

        // Always update the update time
        updateWrapper.set(User::getUpdateTime, new Date());

        // Check uniqueness before update
        User existingUser = getById(user.getId()); // Get current data for comparison
        if (existingUser == null) throw new CustomException("用户不存在");

        if (StringUtils.hasText(user.getEmail()) && !Objects.equals(user.getEmail(), existingUser.getEmail()) && userDao.countByEmail(user.getEmail()) > 0) {
            throw new CustomException("邮箱已存在: " + user.getEmail());
        }
        if (StringUtils.hasText(user.getPhone()) && !Objects.equals(user.getPhone(), existingUser.getPhone()) && userDao.countByPhone(user.getPhone()) > 0) {
            throw new CustomException("手机号已存在: " + user.getPhone());
        }

        return update(updateWrapper);
    }

    /**
     * 分页查找用户 (清除密码)
     */
    @Override
    public IPage<User> findUsersPage(int page, int size, String keyword, UserType userType) {
        Page<User> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();

        queryWrapper.eq(userType != null, User::getUserType, userType);
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(qw -> qw.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getUserNo, keyword));
        }

        queryWrapper.orderByAsc(User::getId); // Default order

        IPage<User> resultPage = page(pageRequest, queryWrapper);
        resultPage.getRecords().forEach(user -> user.setPassword(null)); // Clear passwords
        return resultPage;
    }

    /**
     * 检查是否存在管理员
     */
    @Override
    public boolean checkAdminExists() {
        return count(Wrappers.<User>lambdaQuery().eq(User::getUserType, UserType.ADMIN)) > 0;
    }

    /**
     * 添加管理员用户 (密码需处理)
     */
    @Override
    @Transactional
    public boolean addAdminUser(User admin) {
        if (admin == null) {
            throw new CustomException("管理员信息不能为空");
        }
        admin.setUserType(UserType.ADMIN); // Ensure type is ADMIN
        // Set other defaults if needed (e.g., status)
        admin.setStatus(admin.getStatus() == null ? UserStatus.ACTIVE : admin.getStatus());

        // Use the common addUser method for validation, default setting, and saving
        return addUser(admin);
    }

    /**
     * 根据ID列表获取用户 (清除密码)
     */
    @Override
    public List<User> getUsersByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> users = listByIds(ids);
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    /**
     * 根据用户名查找用户 (Spring Security 使用，保留密码)
     */
    @Override
    public User findByUsername(String username) {
        if (!StringUtils.hasText(username)) return null;
        // This method is often used by Spring Security, which needs the password hash.
        // Do NOT clear the password here.
        return getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
    }

    // Internal helper or example method - Not part of the interface
    public void verifyPasswordReset(String username, String newPassword) {
        // Example method - might be part of a password reset flow
        User user = findByUsername(username);
        if (user == null) {
            throw new CustomException("User not found");
        }
        // ... (generate token, send email, etc.)
    }

    /**
     * 根据学工号查询用户 (清除密码)
     */
    @Override
    public User getUserByUserNo(String userNo) {
        if (!StringUtils.hasText(userNo)) return null;
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getUserNo, userNo));
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
}