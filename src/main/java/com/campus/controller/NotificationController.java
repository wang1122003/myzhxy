package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Notification;
import com.campus.entity.User;
import com.campus.exception.CustomException;
import com.campus.exception.ResourceNotFoundException;
import com.campus.service.NotificationService;
import com.campus.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通知公告控制器
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    // private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper; // 确保添加 @Autowired 注解到 objectMapper 字段

    /**
     * 获取通知详情
     *
     * @param id 通知ID
     * @return 通知详情
     */
    @GetMapping("/{id}")
    public Result<Notification> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationWithAttachments(id);
        if (notification != null) {
            return Result.success("获取成功", notification);
        } else {
            return Result.error("通知未找到");
        }
    }

    /**
     * 获取所有通知
     *
     * @return 通知列表
     */
    @GetMapping("/all")
    public Result<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return Result.success("获取成功", notifications);
    }

    /**
     * 根据类型获取通知
     *
     * @param noticeType 通知类型
     * @return 通知列表
     */
    @GetMapping("/type/{noticeType}")
    public Result<List<Notification>> getNotificationsByType(@PathVariable Integer noticeType) {
        List<Notification> notifications = notificationService.getNotificationsByType(String.valueOf(noticeType));
        return Result.success("获取成功", notifications);
    }

    /**
     * 根据状态获取通知
     *
     * @param status 通知状态
     * @return 通知列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Notification>> getNotificationsByStatus(@PathVariable Integer status) {
        List<Notification> notifications = notificationService.getNotificationsByStatus(String.valueOf(status));
        return Result.success("获取成功", notifications);
    }

    /**
     * 获取最近的通知
     *
     * @param limit 数量限制，默认5条
     * @return 通知列表
     */
    @GetMapping("/recent")
    public Result<List<Notification>> getRecentNotifications(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        return Result.success(notificationService.getRecentNotifications(limit));
    }

    /**
     * 获取置顶通知
     *
     * @return 通知列表
     */
    @GetMapping("/top")
    public Result<List<Notification>> getTopNotifications() {
        List<Notification> notifications = notificationService.getTopNotifications();
        return Result.success("获取成功", notifications);
    }

    /**
     * 根据发布者ID获取通知
     *
     * @param publisherId 发布者ID
     * @return 通知列表
     */
    @GetMapping("/publisher/{publisherId}")
    public Result<List<Notification>> getNotificationsByPublisherId(@PathVariable Long publisherId) {
        List<Notification> notifications = notificationService.getNotificationsByPublisherId(publisherId);
        return Result.success("获取成功", notifications);
    }

    /**
     * 发送通知给指定用户列表
     *
     * @param requestBody 包含 "notification" (通知内容) 和 "userIds" (目标用户ID列表) 的 Map
     * @return 发送结果
     */
    @PostMapping("/send/users")
    @SuppressWarnings("unchecked") // Suppress cast warning for requestBody.get("notification")
    public Result<?> sendNotificationToUsers(@RequestBody Map<String, Object> requestBody) {
        try {
            Notification notification = convertMapToNotification((Map<String, Object>) requestBody.get("notification"));
            List<Long> userIds = convertObjectToListLong(requestBody.get("userIds"));

            if (notification == null || userIds == null || userIds.isEmpty()) {
                return Result.error("请求参数无效：通知内容和目标用户列表不能为空");
            }
            // 可以在这里设置发送者ID等默认值
            // notification.setSenderId(getCurrentUserId()); // 示例：如果需要
            // notification.setPublisherId(getCurrentUserId()); // 示例：如果需要
            notification.setStatus("PUBLISHED"); // 假设发送即发布
            notification.setSendTime(new java.util.Date());

            // 调用修改后的 Service 方法
            notificationService.sendNotificationToUsers(notification, userIds);
            return Result.success("发送成功"); // Service 返回 void，调用不抛异常即认为成功
        } catch (ClassCastException cce) {
            // logger.error("发送通知请求体格式错误", cce);
            return Result.error("请求参数格式错误：" + cce.getMessage());
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            // logger.error("发送通知给用户列表失败", e);
            return Result.error("发送失败: " + e.getMessage());
        }
    }

    // --- Helper methods for conversion ---

    private Notification convertMapToNotification(Map<String, Object> map) {
        if (map == null) return null;
        try {
            // 使用 ObjectMapper 将 Map 转换为 Notification 对象
            // 注意：这要求 Map 中的 key 与 Notification 的字段名匹配
            // 对于 attachmentFiles 这种 List<Map<String, String>> 可能需要特殊处理或确保前端格式正确
            return objectMapper.convertValue(map, Notification.class);
        } catch (IllegalArgumentException e) {
            System.err.println("Map 转换为 Notification 失败: " + e.getMessage());
            // 可以抛出自定义异常或返回 null
            throw new IllegalArgumentException("无法将提供的 'notification' 数据转换为通知对象: " + e.getMessage(), e);
        }
    }

    private List<Long> convertObjectToListLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof List<?> list) {
            List<Long> longList = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Number) {
                    longList.add(((Number) item).longValue());
                } else if (item instanceof String) {
                    try {
                        longList.add(Long.parseLong((String) item));
                    } catch (NumberFormatException e) {
                        throw new ClassCastException("列表中的元素无法转换为 Long: " + item);
                    }
                } else {
                    throw new ClassCastException("列表包含非数字类型的元素: " + item.getClass().getName());
                }
            }
            return longList;
        }
        throw new ClassCastException("提供的 'userIds' 不是一个列表");
    }

    /**
     * 发送通知给指定用户类型
     *
     * @param userType     用户类型 (如 STUDENT, TEACHER, ADMIN)
     * @param notification 通知内容对象 (应包含 title, content, type, publisherId, attachmentFiles 等)
     * @return 发送结果
     */
    @PostMapping("/send/type/{userType}")
    public Result<Boolean> sendNotificationByUserType(@PathVariable String userType, @RequestBody Notification notification) {
        try {
            if (notification == null) {
                return Result.error("请求参数无效：通知内容不能为空");
            }
            // 前端需要在 notification 中填充 title, content, type, publisherId, attachmentFiles 等
            boolean success = notificationService.sendNotificationByUserType(notification, userType);
            return success ? Result.success("发送成功", true) : Result.error("发送失败或未找到该类型用户");
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (IllegalArgumentException iae) {
            return Result.error("发送失败: " + iae.getMessage()); //捕获无效用户类型
        } catch (Exception e) {
            // logger.error("发送通知给用户类型 {} 失败", userType, e);
            return Result.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送通知给全体用户
     *
     * @param notification 通知内容对象 (应包含 title, content, type, publisherId, attachmentFiles 等)
     * @return 发送结果
     */
    @PostMapping("/send/all")
    public Result<Boolean> sendNotificationToAll(@RequestBody Notification notification) {
        try {
            if (notification == null) {
                return Result.error("请求参数无效：通知内容不能为空");
            }
            // 前端需要在 notification 中填充 title, content, type, publisherId, attachmentFiles 等
            boolean success = notificationService.sendNotificationToAll(notification);
            return success ? Result.success("发送成功", true) : Result.error("发送失败或系统无用户");
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (IllegalArgumentException iae) {
            return Result.error("发送失败: " + iae.getMessage());
        } catch (Exception e) {
            // logger.error("发送全体通知失败", e);
            return Result.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的通知分页列表
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @param status   状态过滤 ("all", "unread", "read")
     * @return 分页结果
     */
    @GetMapping("/user")
    public Result<IPage<Notification>> getNotificationsForCurrentUser(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "all") String status) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "无法获取当前用户信息，请登录");
        }

        try {
            IPage<Notification> page = notificationService.getNotificationsForUser(currentUserId, pageNo, pageSize, status);
            return Result.success("获取用户通知成功", page);
        } catch (Exception e) {
            // logger.error("获取用户 {} 的通知失败", currentUserId, e);
            return Result.error("获取用户通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 将指定通知标记为当前用户已读
     *
     * @param notificationId 要标记的通知ID
     * @return 操作结果
     */
    @PutMapping("/read/{notificationId}")
    public Result<Boolean> markNotificationAsReadForCurrentUser(@PathVariable Long notificationId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "无法获取当前用户信息，请登录");
        }

        try {
            boolean success = notificationService.markNotificationAsRead(notificationId, currentUserId);
            return success ? Result.success("标记已读成功", true) : Result.error("标记已读失败或已经是已读状态");
        } catch (ResourceNotFoundException rnfe) {
            return Result.error(404, rnfe.getMessage());
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            // logger.error("用户 {} 标记通知 {} 为已读失败", currentUserId, notificationId, e);
            return Result.error("标记已读时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新通知状态
     *
     * @param id     通知ID
     * @param status 通知状态
     * @return 更新结果
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateNotificationStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            notificationService.updateNotificationStatus(id, status);
            return Result.success("更新状态成功");
        } catch (Exception e) {
            return Result.error("更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除通知
     *
     * @param ids 通知ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteNotifications(@RequestBody List<Long> ids) {
        try {
            notificationService.batchDeleteNotifications(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询通知
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @param type     通知类型（可选）
     * @param keyword  关键词（可选）
     * @param status   通知状态（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<IPage<Notification>> getNotificationPage(
            @RequestParam(name = "page", defaultValue = "1") int pageNo,
            @RequestParam(name = "size", defaultValue = "10") int pageSize,
            @RequestParam(name = "type", required = false) Integer type,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status) {
        try {
            String typeStr = (type != null) ? String.valueOf(type) : null;
            String statusStr = (status != null) ? String.valueOf(status) : null;
            IPage<Notification> page = notificationService.getNotificationPage(pageNo, pageSize, typeStr, keyword, statusStr);
            return Result.success("获取通知列表成功", page);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误信息以便调试
            return Result.error("获取通知分页数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有通知类型
     *
     * @return 通知类型列表
     */
    @GetMapping("/types")
    public Result<List<Map<String, Object>>> getNoticeTypes() {
        // 恢复为简单实现或调用 Service (如果 Service 中有)
        // 这里暂时提供一个硬编码的示例，你需要根据实际情况调整
        // 或者调用 notificationService.getNoticeTypes() (如果存在)
        List<Map<String, Object>> types = new ArrayList<>();
        types.add(Map.of("typeCode", 1, "typeName", "系统通知", "tagType", "info"));
        types.add(Map.of("typeCode", 2, "typeName", "教学通知", "tagType", "success"));
        types.add(Map.of("typeCode", 3, "typeName", "活动公告", "tagType", "warning"));
        types.add(Map.of("typeCode", 4, "typeName", "其他", "tagType", "danger"));
        return Result.success("获取成功 (Hardcoded)", types);

        // // 基于枚举的实现（确保整个实现被注释掉或移除）
        // List<Map<String, Object>> typeList = NotificationType.getAllTypes().stream()
        //         .map(type -> Map.<String, Object>of(
        //                 "typeCode", type.getCode(),
        //                 "typeName", type.getDescription(), // 中文名，用于显示
        //                 "tagType", type.getTagType(),
        //                 "typeEnumName", type.name() // 添加枚举名称，用于前端匹配
        //         ))
        //         .collect(Collectors.toList());
        // return Result.success("获取成功", typeList);
    }

    /**
     * 从 Spring Security 上下文获取当前用户 ID
     *
     * @return 当前用户ID，如果未认证或获取失败则返回 null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 如果 Principal 是 UserDetails 但不是 User 实例，尝试获取 username 然后查询 (备用逻辑)
                // String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                // 这里需要 userService 来根据 username 查询 ID，暂不实现此复杂备用逻辑
                // User user = userService.findByUsername(username);
                // return user != null ? user.getId() : null;
                System.err.println("Principal is UserDetails but not User instance: " + principal.getClass());
                return null; // 无法直接获取ID
            } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
                // 如果 Principal 是 String (用户名), 尝试查询 (更进一步的备用)
                // User user = userService.findByUsername((String) principal);
                // return user != null ? user.getId() : null;
                System.err.println("Principal is a String (username?): " + principal);
                return null; // 无法直接获取ID
            }
        }
        return null;
    }
} 