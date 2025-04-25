package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.User;
import com.campus.enums.UserStatus;
import com.campus.enums.UserType;
import com.campus.service.AuthService;
import com.campus.service.UserService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
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
     */
    @GetMapping("/check-session")
    public Result checkSession(HttpServletRequest request) {
        User user = authService.getCurrentUserFromRequest(request);
        
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            // Return the string value ('Admin', 'Student', 'Teacher')
            data.put("userType", user.getUserType() != null ? user.getUserType().getValue() : null);
            
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
     */
    @GetMapping("/permissions")
    public Result getUserPermissions(HttpServletRequest request) {
        User currentUser = authService.getCurrentUserFromRequest(request);
        if (currentUser != null && currentUser.getUserType() != null) {
            Map<String, Boolean> permissions = new HashMap<>();
            UserType userType = currentUser.getUserType(); // Get Enum from User object

            // Use Enum for comparison
            if (userType == UserType.ADMIN) { 
                permissions.put("user_manage", true);
                permissions.put("course_manage", true);
                permissions.put("classroom_manage", true);
                permissions.put("activity_manage", true);
                permissions.put("forum_manage", true);
                permissions.put("system_config", true);
            } else if (userType == UserType.TEACHER) { 
                permissions.put("user_manage", false);
                permissions.put("course_manage", true);
                permissions.put("classroom_manage", false);
                permissions.put("activity_manage", true);
                permissions.put("forum_manage", false);
                permissions.put("system_config", false);
            } else { // Assuming STUDENT
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
     */
    @GetMapping("/check-permission")
    public Result checkPermission(
            HttpServletRequest request,
            @RequestParam String operation) {
        User currentUser = authService.getCurrentUserFromRequest(request);

        if (currentUser != null && currentUser.getUserType() != null) {
            boolean hasPermission = false;
            UserType userType = currentUser.getUserType(); // Get Enum from User object

            if (userType == UserType.ADMIN) {
                hasPermission = true;
            } else if (userType == UserType.TEACHER) {
                if ("course_edit".equals(operation) || 
                    "grade_manage".equals(operation) || 
                    "activity_create".equals(operation) ||
                    "post_create".equals(operation)) {
                    hasPermission = true;
                }
            } else { // Assuming STUDENT
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

        if (user.getUserType() == null) {
            return Result.error("用户类型不能为空");
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
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

        UserType userTypeEnum = null;
        if (userType != null && !userType.isEmpty()) {
            try {
                userTypeEnum = UserType.fromValue(userType);
            } catch (IllegalArgumentException e) {
                return Result.error("无效的用户类型: " + userType);
            }
        }

        IPage<User> userPage = userService.findUsersPage(page, size, keyword, userTypeEnum);
        userPage.getRecords().forEach(u -> u.setPassword(null));
        return Result.success("查询成功", userPage);
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean success = userService.updateUser(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 新状态 (e.g., 0 for inactive, 1 for active)
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        UserStatus statusEnum;
        try {
            statusEnum = UserStatus.fromValue(status);
        } catch (IllegalArgumentException e) {
            return Result.error("无效的状态值: " + status);
        }
        boolean success = userService.updateUserStatus(id, statusEnum);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
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

        user.setId(currentUserId);
        user.setPassword(null);
        user.setUsername(null);
        user.setUserType(null);
        user.setStatus(null);
        user.setCreateTime(null);
        user.setUpdateTime(new Date());

        boolean success = userService.updateUserProfile(user);

        if (success) {
            User updatedUser = userService.getUserById(currentUserId); // 获取更新后的信息
            if (updatedUser != null) updatedUser.setPassword(null);
            return Result.success("个人资料更新成功", updatedUser);
        } else {
            return Result.error("个人资料更新失败");
        }
    }

} // End of UserController class