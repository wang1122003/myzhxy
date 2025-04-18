package com.campus.controller;

import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.service.UserService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // Explicitly declare the logger
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     * @param loginUser 用户登录信息
     * @param response HTTP响应
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result login(
            @RequestBody User loginUser,
            HttpServletResponse response) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
        if (user != null) {
            // 生成token
            String token = authService.generateToken(user);
            
            // 设置token到Cookie
            // authService.setTokenToCookie(response, token);
            
            // 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            user.setPassword(null); // 清除密码
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
        // 使用 AuthService 获取当前登录用户 (从 Token 解析)
        User basicUserInfo = authService.getCurrentUser(request);

        if (basicUserInfo != null && basicUserInfo.getId() != null) {
            // 根据 ID 从数据库获取完整的用户信息
            User fullUserInfo = userService.getUserById(basicUserInfo.getId());
            if (fullUserInfo != null) {
                // 清除敏感信息
                fullUserInfo.setPassword(null);
                return Result.success("获取成功", fullUserInfo);
            } else {
                // 如果根据ID找不到用户（理论上不应发生，除非用户刚被删除）
                log.warn("无法根据 AuthService 提供的有效 userId {} 找到用户", basicUserInfo.getId());
                return Result.error("无法获取当前用户信息，用户数据可能不存在");
            }
        } else {
            // 如果 AuthService 返回 null 或 ID 为 null，说明 Token 无效或解析失败
            log.info("无法获取当前用户信息，Token 无效或解析失败");
            return Result.error("无法获取当前用户信息，请重新登录");
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
        Map<String, Object> stats = userService.getUserStats();
        return Result.success("获取成功", stats);
    }

    /**
     * 批量导入用户
     *
     * @param file     Excel文件
     * @param userType 用户类型 (1-学生, 2-教师)
     * @return 导入结果
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result importUsers(@RequestParam("file") MultipartFile file,
                              @RequestParam("userType") Integer userType) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的文件");
            }

            // 校验文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return Result.error("文件格式不正确，请上传Excel文件");
            }

            // 调用服务层处理导入
            Map<String, Object> result = userService.importUsers(file, userType);

            return Result.success("导入成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 批量导入用户数据
     *
     * @param users 用户列表
     * @return 导入结果
     */
    @PostMapping("/batch-import")
    public Result batchImportUsers(@RequestBody List<User> users) {
        try {
            Map<String, Object> result = userService.batchImportUsers(users);
            return Result.success("批量导入成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量导入失败：" + e.getMessage());
        }
    }

    /**
     * 下载用户导入模板
     *
     * @return 导入模板
     */
    @GetMapping("/import-template")
    public Result downloadImportTemplate() {
        try {
            // 生成并返回模板数据
            String templateUrl = userService.generateImportTemplate();
            Map<String, Object> data = new HashMap<>();
            data.put("templateUrl", templateUrl);
            return Result.success("模板生成成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("模板生成失败：" + e.getMessage());
        }
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