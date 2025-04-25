package com.campus.service.impl;

import com.campus.dao.UserDao;
import com.campus.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserDao userDao; // 注入 UserDao 来查询用户

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 使用 UserDao 查询用户
        User user = userDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // 因为 User 类已经实现了 UserDetails，可以直接返回
        return user;
    }
} 