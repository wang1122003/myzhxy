package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.service.UserService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 显式声明 logger
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    /**
     * 用户登录 (使用明文密码)
     */
    @PostMapping("/login")
    public Result login(
            @RequestBody User loginUser,
            HttpServletResponse response) {

        // 直接使用用户输入的明文密码进行登录验证
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword()); 
        
        if (user != null) {
            // 生成token
            String token = authService.generateToken(user);
            
            // 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            user.setPassword(null); // 仍然清除密码，避免返回给前端
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
        User user = authService.getCurrentUserFromRequest(request); // 使用修正的方法名
        
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("userType", user.getUserType());
            data.put("role", "Admin".equals(user.getUserType()) ? "ADMIN" : ("Student".equals(user.getUserType()) ? "STUDENT" : "TEACHER"));
            
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
     * 注册新用户 (使用明文密码)
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        User existingUser = userService.getUserByUsername(user.getUsername());
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        user.setStatus("Active"); 
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        // 密码直接使用 user 对象中的明文密码
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
     * 获取用户列表（分页、过滤、搜索）
     * @param page 当前页码
     * @param size 每页数量
     * @param keyword 搜索关键词 (可选, 搜索用户名、真实姓名或邮箱)
     * @param userType 用户类型 (可选, 0: Admin, 1: Student, 2: Teacher)
     * @return 分页后的用户列表
     */
    @GetMapping("") // 映射到 /api/users
    public Result getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userType) {

        try {
            IPage<User> userPage = userService.findUsersPage(page, size, keyword, userType);

            // 清除密码等敏感信息
            if (userPage.getRecords() != null) {
                userPage.getRecords().forEach(u -> u.setPassword(null));
            }

            // 包装成前端需要的格式
            Map<String, Object> data = new HashMap<>();
            data.put("rows", userPage.getRecords());
            data.put("total", userPage.getTotal());

            return Result.success("获取用户列表成功", data);
        } catch (Exception e) {
            log.error("Error fetching user list", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {
        // 确保不更新密码字段，密码修改有专门的接口
        user.setPassword(null); 
        user.setId(id);
        user.setUpdateTime(new Date());
        
        boolean success = userService.updateUser(user);
        
        if (success) {
            User updatedUser = userService.getUserById(id); // 获取更新后的信息
            if (updatedUser != null) updatedUser.setPassword(null);
            return Result.success("更新成功", updatedUser);
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 新状态 (e.g., 0 for inactive, 1 for active)
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result updateUserStatus(@PathVariable Long id, @RequestParam String status) { // 前端传的是字符串 'Active'/'Inactive'
        boolean success = userService.updateUserStatus(id, status);
        if (success) {
            return Result.success("用户状态更新成功");
        } else {
            return Result.error("用户状态更新失败");
        }
    }
    
    /**
     * 重置用户密码
     * @param id 用户ID
     * @return 重置结果
     */
    @PostMapping("/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id) {
        // 实现密码重置逻辑，例如设置为默认密码
        String defaultPassword = "password123"; // 实际应从配置读取或生成
        boolean success = userService.resetPassword(id, defaultPassword);
        
        if (success) {
            return Result.success("密码重置成功，新密码为：" + defaultPassword); // 生产环境不应返回密码
        } else {
            return Result.error("密码重置失败");
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
            return Result.success("用户删除成功");
        } else {
            return Result.error("用户删除失败");
        }
    }

    /**
     * 获取用户统计信息
     * @return 用户统计数据
     */
    @GetMapping("/stats")
    public Result getUserStats() {
        Map<String, Object> stats = userService.getUserStats();
        return Result.success("获取用户统计信息成功", stats);
    }

    /**
     * 用户登出
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        // 实现登出逻辑，例如使Token失效
        // 简单的实现可以是让前端删除Token
        // 如果使用服务端Session或需要记录登出，可以在这里添加逻辑
        // authService.invalidateToken(request); // 假设有此方法
        return Result.success("登出成功");
    }

    /**
     * 更新用户个人资料
     *
     * @param user    包含更新信息的用户对象
     * @param request HTTP请求 (用于获取当前用户ID)
     * @return 更新结果
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody User user, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error("未授权的操作");
        }

        // 验证是否在更新自己的资料
        // if (!currentUserId.equals(user.getId())) { // 如果请求体中也包含id
        //     return Result.error("只能修改自己的资料");
        // }

        // 设置要更新的用户ID，防止意外更新他人资料
        user.setId(currentUserId);

        // 不允许通过此接口修改密码、用户名、用户类型或状态
        user.setPassword(null);
        user.setUsername(null);
        user.setUserType(null);
        user.setStatus(null);
        user.setCreateTime(null); // 不应修改创建时间
        user.setUpdateTime(new Date()); // 更新修改时间

        boolean success = userService.updateUserProfile(user); // 可能需要一个只更新允许字段的方法

        if (success) {
            User updatedUser = userService.getUserById(currentUserId); // 获取更新后的信息
            if (updatedUser != null) updatedUser.setPassword(null);
            return Result.success("个人资料更新成功", updatedUser);
        } else {
            return Result.error("个人资料更新失败");
        }
    }

} // End of UserController class