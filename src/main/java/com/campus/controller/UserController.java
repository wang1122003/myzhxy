package com.campus.controller;

import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.service.UserService;
import com.campus.utils.JwtUtil;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {
        User user = userService.login(username, password);
        if (user != null) {
            // 生成token
            String token = authService.generateToken(user);
            
            // 设置token到Cookie
            authService.setTokenToCookie(response, token);
            
            // 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            
            return Result.success("登录成功", data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
    
    /**
     * 检查会话有效性
     * @param request HTTP请求
     * @return 会话检查结果
     */
    @GetMapping("/check-session")
    public Result checkSession(HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("userType", user.getUserType());
            data.put("role", user.getUserType() == 0 ? "ADMIN" : (user.getUserType() == 1 ? "STUDENT" : "TEACHER"));
            
            return Result.success("会话有效", data);
        } else {
            return Result.error("无效的会话");
        }
    }
    
    /**
     * 修改密码
     * @param request HTTP请求
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public Result changePassword(
            HttpServletRequest request,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        // 从请求属性中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId != null) {
            boolean success = userService.updatePassword(userId, oldPassword, newPassword);
            
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败，请检查旧密码是否正确");
            }
        } else {
            return Result.error("未授权的操作");
        }
    }
    
    /**
     * 获取用户权限
     * @param request HTTP请求
     * @return 用户权限信息
     */
    @GetMapping("/permissions")
    public Result getUserPermissions(HttpServletRequest request) {
        // 从请求属性中获取用户信息
        Long userId = (Long) request.getAttribute("userId");
        Integer userType = (Integer) request.getAttribute("userType");
        
        if (userId != null && userType != null) {
            Map<String, Boolean> permissions = new HashMap<>();
            
            // 根据用户类型设置不同权限
            if (userType == 0) { // 管理员
                permissions.put("user_manage", true);
                permissions.put("course_manage", true);
                permissions.put("classroom_manage", true);
                permissions.put("activity_manage", true);
                permissions.put("forum_manage", true);
                permissions.put("system_config", true);
            } else if (userType == 2) { // 教师
                permissions.put("user_manage", false);
                permissions.put("course_manage", true);
                permissions.put("classroom_manage", false);
                permissions.put("activity_manage", true);
                permissions.put("forum_manage", false);
                permissions.put("system_config", false);
            } else { // 学生
                permissions.put("user_manage", false);
                permissions.put("course_manage", false);
                permissions.put("classroom_manage", false);
                permissions.put("activity_manage", false);
                permissions.put("forum_manage", false);
                permissions.put("system_config", false);
            }
            
            return Result.success("获取权限成功", permissions);
        } else {
            return Result.error("无法获取用户权限");
        }
    }
    
    /**
     * 检查特定操作权限
     * @param request HTTP请求
     * @param operation 操作名称
     * @return 权限检查结果
     */
    @GetMapping("/check-permission")
    public Result checkPermission(
            HttpServletRequest request,
            @RequestParam String operation) {
        // 从请求属性中获取用户类型
        Integer userType = (Integer) request.getAttribute("userType");
        
        if (userType != null) {
            boolean hasPermission = false;
            
            // 管理员拥有所有权限
            if (userType == 0) {
                hasPermission = true;
            } else if (userType == 2) { // 教师
                // 教师特定权限
                if ("course_edit".equals(operation) || 
                    "grade_manage".equals(operation) || 
                    "activity_create".equals(operation) ||
                    "post_create".equals(operation)) {
                    hasPermission = true;
                }
            } else { // 学生
                // 学生特定权限
                if ("course_select".equals(operation) || 
                    "activity_join".equals(operation) || 
                    "post_create".equals(operation)) {
                    hasPermission = true;
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("hasPermission", hasPermission);
            
            return Result.success("权限检查完成", data);
        } else {
            return Result.error("未授权的操作");
        }
    }
    
    /**
     * 注册新用户
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        // 检查用户名是否已存在
        User existingUser = userService.getUserByUsername(user.getUsername());
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }
        
        // 设置默认值
        user.setStatus(1); // 默认启用
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        // 保存用户
        boolean success = userService.addUser(user);
        
        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败，请稍后再试");
        }
    }
    
    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            // 清除敏感信息
            user.setPassword(null);
            return Result.success("获取成功", user);
        } else {
            return Result.error("用户不存在");
        }
    }
    
    /**
     * 获取当前登录用户信息
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/current")
    public Result getCurrentUser(HttpServletRequest request) {
        // 从请求属性中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId != null) {
            User user = userService.getUserById(userId);
            if (user != null) {
                // 清除敏感信息
                user.setPassword(null);
                return Result.success("获取成功", user);
            }
        }
        
        return Result.error("无法获取当前用户信息");
    }
    
    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Result getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user != null) {
            // 清除敏感信息
            user.setPassword(null);
            return Result.success("获取成功", user);
        } else {
            return Result.error("用户不存在");
        }
    }
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result getAllUsers() {
        List<User> users = userService.getAllUsers();
        // 清除敏感信息
        users.forEach(user -> user.setPassword(null));
        return Result.success("获取成功", users);
    }
    
    /**
     * 搜索用户
     * @param keyword 关键词
     * @return 匹配的用户列表
     */
    @GetMapping("/search")
    public Result searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        // 清除敏感信息
        users.forEach(user -> user.setPassword(null));
        return Result.success("获取成功", users);
    }
    
    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 更新的用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {
        // 设置ID和更新时间
        user.setId(id);
        user.setUpdateTime(new Date());
        
        // 更新用户信息
        boolean success = userService.updateUser(user);
        
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败，请稍后再试");
        }
    }
    
    /**
     * 修改用户状态
     * @param id 用户ID
     * @param status 状态值（0-禁用，1-启用）
     * @return 修改结果
     */
    @PutMapping("/{id}/status")
    public Result updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = userService.updateUserStatus(id, status);
        
        if (success) {
            return Result.success("状态修改成功");
        } else {
            return Result.error("状态修改失败，请稍后再试");
        }
    }
    
    /**
     * 重置用户密码
     * @param id 用户ID
     * @return 重置结果
     */
    @PostMapping("/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id) {
        // 默认密码为123456
        String defaultPassword = "123456";
        String encryptedPassword = DigestUtils.md5DigestAsHex(defaultPassword.getBytes(StandardCharsets.UTF_8));
        
        boolean success = userService.resetPassword(id, encryptedPassword);
        
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("defaultPassword", defaultPassword);
            return Result.success("密码重置成功", data);
        } else {
            return Result.error("密码重置失败，请稍后再试");
        }
    }
    
    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败，请稍后再试");
        }
    }
    
    /**
     * 获取用户统计信息
     * @return 用户统计信息
     */
    @GetMapping("/stats")
    public Result getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = userService.getUserCount();
        long studentCount = userService.getUserCountByType(1);
        long teacherCount = userService.getUserCountByType(2);
        long adminCount = userService.getUserCountByType(0);
        long activeCount = userService.getUserCountByStatus(1);
        long inactiveCount = userService.getUserCountByStatus(0);
        
        stats.put("total", total);
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("adminCount", adminCount);
        stats.put("activeCount", activeCount);
        stats.put("inactiveCount", inactiveCount);
        
        return Result.success("获取成功", stats);
    }
    
    /**
     * 创建默认管理员账号
     * @param securityKey 安全密钥
     * @return 创建结果
     */
    @PostMapping("/create-admin")
    public Result createDefaultAdmin(@RequestParam(defaultValue = "") String securityKey) {
        // 出于安全考虑，需要提供一个密钥才能创建管理员账号
        if (!"campus_security_key".equals(securityKey)) {
            return Result.error("安全密钥错误，无权创建管理员账号");
        }
        
        // 检查是否已存在admin用户
        User existingAdmin = userService.getUserByUsername("admin");
        if (existingAdmin != null) {
            return Result.error("管理员账号已存在，无需重复创建");
        }
        
        // 创建管理员账号
        User admin = new User();
        admin.setUsername("admin");
        // 密码为admin123，使用MD5加密
        String encryptedPassword = DigestUtils.md5DigestAsHex("admin123".getBytes(StandardCharsets.UTF_8));
        admin.setPassword(encryptedPassword);
        admin.setRealName("系统管理员");
        admin.setUserType(0); // 0表示管理员
        admin.setStatus(1);   // 1表示启用
        admin.setCreateTime(new Date());
        admin.setUpdateTime(new Date());
        
        boolean success = userService.addUser(admin);
        
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", "admin");
            data.put("password", "admin123");
            return Result.success("管理员账号创建成功", data);
        } else {
            return Result.error("管理员账号创建失败");
        }
    }
    
    /**
     * 登出
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        // 清除Cookie中的token
        authService.clearTokenCookie(response);
        return Result.success("登出成功");
    }
}