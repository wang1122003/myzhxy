package com.campus.service.impl;

import com.campus.dao.UserDao;
import com.campus.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService的自定义实现
 * 用于从数据库中加载用户信息和认证
 */
@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查找用户
        User user = userDao.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在: " + username);
        }

        // 直接返回从数据库获取的 User 对象，因为它已经实现了 UserDetails
        return user;
    }
} 