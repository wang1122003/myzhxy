package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.UserDao;
import com.campus.entity.User;
import com.campus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 用户服务实现类
 * 提供用户管理相关的业务逻辑实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

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
     * 根据用户类型查询用户
     * @param userType 用户类型
     * @return 用户列表
     */
    @Override
    public List<User> getUsersByType(Integer userType) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserType, userType);
        return list(queryWrapper);
    }

    /**
     * 添加用户
     * @param user 用户对象
     * @return 是否成功
     */
    @Override
    public boolean addUser(User user) {
        // 设置创建时间和更新时间
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        
        // 默认启用状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        // 对密码进行MD5加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encryptedPassword = encryptPassword(user.getPassword());
            user.setPassword(encryptedPassword);
        }
        
        return save(user);
    }

    /**
     * 更新用户
     * @param user 用户对象
     * @return 是否成功
     */
    @Override
    public boolean updateUser(User user) {
        // 设置更新时间
        user.setUpdateTime(new Date());
        
        // 如果密码不为空，则加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encryptedPassword = encryptPassword(user.getPassword());
            user.setPassword(encryptedPassword);
        } else {
            // 不更新密码
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
     * 修改用户状态
     * @param id 用户ID
     * @param status 状态值
     * @return 是否成功
     */
    @Override
    public boolean updateUserStatus(Long id, Integer status) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id).set(User::getStatus, status);
        return update(updateWrapper);
    }

    /**
     * 修改密码
     * @param id 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    @Override
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        // 验证旧密码是否正确
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        // 加密旧密码并验证
        String encryptedOldPassword = encryptPassword(oldPassword);
        if (encryptedOldPassword.equals(user.getPassword())) {
            // 加密新密码
            String encryptedNewPassword = encryptPassword(newPassword);
            
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, id).set(User::getPassword, encryptedNewPassword);
            return update(updateWrapper);
        }
        return false;
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户对象，登录失败返回null
     */
    @Override
    public User login(String username, String password) {
        // 加密密码
        String encryptedPassword = encryptPassword(password);
        
        // 根据用户名和加密后的密码查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username)
                    .eq(User::getPassword, encryptedPassword)
                    .eq(User::getStatus, 1);  // 只查询启用状态的用户
        return getOne(queryWrapper);
    }
    
    /**
     * 对密码进行MD5加密
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String encryptPassword(String password) {
        // 使用MD5加密
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
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
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id).set(User::getPassword, newPassword);
        return update(updateWrapper);
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
     * 根据用户类型获取用户数量
     * @param userType 用户类型
     * @return 用户数量
     */
    @Override
    public long getUserCountByType(int userType) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserType, userType);
        return count(queryWrapper);
    }
    
    /**
     * 根据用户状态获取用户数量
     * @param status 用户状态
     * @return 用户数量
     */
    @Override
    public long getUserCountByStatus(int status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, status);
        return count(queryWrapper);
    }
}