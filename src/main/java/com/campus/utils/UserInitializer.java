package com.campus.utils;

import com.campus.entity.User;
import com.campus.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 用户初始化工具
 * 用于创建初始管理员账号
 */
public class UserInitializer {
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("用法: UserInitializer <用户名> <密码> <真实姓名> [用户类型]");
            System.out.println("用户类型: 0-管理员, 1-学生, 2-教师 (默认为0)");
            return;
        }
        
        try {
            String username = args[0];
            String password = args[1];
            String realName = args[2];
            Integer userType = args.length > 3 ? Integer.parseInt(args[3]) : 0;
            
            // 加载Spring上下文
            ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
            UserService userService = context.getBean(UserService.class);
            
            // 检查用户名是否已存在
            User existingUser = userService.getUserByUsername(username);
            if (existingUser != null) {
                System.out.println("创建失败: 用户名 '" + username + "' 已存在!");
                return;
            }
            
            // 创建用户对象
            User user = new User();
            user.setUsername(username);
            // 密码加密
            String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
            user.setPassword(encryptedPassword);
            user.setRealName(realName);
            user.setUserType(userType);
            user.setStatus(1); // 启用状态
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            
            // 保存用户
            boolean success = userService.addUser(user);
            
            if (success) {
                System.out.println("成功创建用户 '" + username + "' (ID: " + user.getId() + ")");
            } else {
                System.out.println("创建用户失败!");
            }
            
        } catch (Exception e) {
            System.out.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 