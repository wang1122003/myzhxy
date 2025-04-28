package com.campus.service.impl;

import com.campus.dao.UserDao;
import com.campus.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

        // 创建授权列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 根据用户类型添加权限
        if (user.getUserType() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));
        }

        // 返回Spring Security的UserDetails对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1, // 账户是否启用
                true, // 账户未过期
                true, // 凭证未过期
                true, // 账户未锁定
                authorities
        );
    }
} 