package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.User;
import com.campus.enums.UserStatus;
import com.campus.enums.UserType;
import com.campus.service.AuthService;
import com.campus.service.UserService;
import com.campus.utils.JwtUtil;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
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

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 用户登录 (使用Spring Security认证)
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        try {
            // 使用Spring Security进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword())
            );

            // 存储认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginUser.getUsername());

            // 生成JWT令牌
            String token = jwtUtil.generateToken(userDetails);

            // 获取用户信息
            User user = userService.getUserByUsername(loginUser.getUsername());
            user.setPassword(null); // 移除密码

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return Result.success("登录成功", data);
        } catch (BadCredentialsException e) {
            return Result.error("用户名或密码错误");
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 检查会话有效性
     */
    @GetMapping("/check-session")
    public Result<Map<String, Object>> checkSession(HttpServletRequest request) {
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
     *
     * @param request     HTTP请求
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(
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
    public Result<Map<String, Boolean>> getUserPermissions(HttpServletRequest request) {
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
    public Result<Map<String, Object>> checkPermission(
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
    public Result<Void> register(@RequestBody User user) {
        User existingUser = userService.getUserByUsername(user.getUsername());
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        if (user.getUserType() == null) {
            return Result.error("用户类型不能为空");
        }
        boolean success = userService.addUser(user);

        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败，请稍后再试");
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
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
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
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
     *
     * @param page     当前页码
     * @param size     每页数量
     * @param keyword  搜索关键词
     * @param userType 用户类型过滤
     * @return 用户列表
     */
    @GetMapping("") // 映射到 /api/users
    public Result<IPage<User>> getUserList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "userType", required = false) String userType) {

        UserType typeEnum = null;
        if (StringUtils.hasText(userType)) {
            try {
                typeEnum = UserType.valueOf(userType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Handle invalid user type string if needed, maybe return error or ignore
                return Result.error("无效的用户类型");
            }
        }

        IPage<User> userPage = userService.findUsersPage(page, size, keyword, typeEnum);

        // Clear password for each user before returning
        userPage.getRecords().forEach(user -> user.setPassword(null));

        return Result.success("获取成功", userPage);
    }

    /**
     * 更新用户信息
     *
     * @param id   用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        // Ensure ID consistency
        user.setId(id);
        // Do not allow updating username, password, or creation time via this endpoint
        user.setUsername(existingUser.getUsername());
        user.setPassword(null); // Prevent password update
        user.setCreateTime(existingUser.getCreateTime()); // Keep original creation time

        boolean success = userService.updateUser(user);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id     用户ID
     * @param status 用户状态 (Active/Inactive)
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        UserStatus userStatus;
        try {
            userStatus = UserStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Result.error("无效的状态值");
        }

        boolean success = userService.updateUserStatus(id, userStatus);
        if (success) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 重置用户密码 (管理员操作，使用明文密码)
     *
     * @param id 用户ID
     * @param requestBody 包含新密码的请求体
     * @return 重置结果
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String newPassword = requestBody.get("newPassword");
        if (!StringUtils.hasText(newPassword)) {
            return Result.error("新密码不能为空");
        }

        // 确保操作者有权限（这里简化，实际应检查管理员权限）

        // 直接使用明文密码进行重置
        boolean success = userService.resetPassword(id, newPassword);

        if (success) {
            return Result.success("密码重置成功");
        } else {
            return Result.error("密码重置失败");
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        if (success) {
            return Result.success("用户删除成功");
        } else {
            return Result.error("用户删除失败");
        }
    }

    /**
     * 获取用户统计信息
     *
     * @return 用户统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = userService.getUserStats();
        return Result.success("获取用户统计信息成功", stats);
    }

    /**
     * 用户登出
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
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
    public Result<Void> updateProfile(@RequestBody User user, HttpServletRequest request) {
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

        try {
            boolean success = userService.updateUserProfile(user);
            if (success) {
                // 返回更新后的 User 信息（不含密码）
                User updatedUser = userService.getUserById(currentUserId);
                if (updatedUser != null) updatedUser.setPassword(null);
                return Result.success("个人资料更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新个人资料时发生错误: " + e.getMessage());
        }
    }

    /**
     * 刷新用户令牌
     *
     * @param request HTTP请求
     * @return 包含新令牌的结果对象
     */
    @PostMapping("/refresh-token")
    public Result<Map<String, Object>> refreshToken(HttpServletRequest request) {
        try {
            // 从请求头中提取当前token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (token != null && jwtUtil.validateToken(token)) {
                    // 从token中获取用户名
                    String username = jwtUtil.getClaimFromToken(token, claims -> claims.getSubject());
                    // 加载用户
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    // 生成新token
                    String newToken = jwtUtil.generateToken(userDetails);

                    Map<String, Object> response = new HashMap<>();
                    response.put("token", newToken);

                    return Result.success("Token刷新成功", response);
                }
            }
            return Result.error("无效的Token，请重新登录");
        } catch (Exception e) {
            return Result.error("刷新Token失败: " + e.getMessage());
        }
    }

} // End of UserController class