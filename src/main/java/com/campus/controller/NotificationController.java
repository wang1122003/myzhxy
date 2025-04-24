package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Notification;
import com.campus.exception.CustomException;
import com.campus.service.NotificationService;
import com.campus.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知公告控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取通知详情
     *
     * @param id 通知ID
     * @return 通知详情
     */
    @GetMapping("/{id}")
    public Result<Notification> getNotificationById(@PathVariable Long id) {
        try {
            Notification notification = notificationService.getNotificationWithAttachments(id);
            if (notification != null) {
                notificationService.incrementViewCount(id);
                return Result.success("获取成功", notification);
            } else {
                return Result.error("通知不存在");
            }
        } catch (Exception e) {
            log.error("获取通知详情失败, ID: {}", id, e);
            return Result.error("获取通知详情失败");
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
        List<Notification> notifications = notificationService.getNotificationsByType(noticeType);
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
        List<Notification> notifications = notificationService.getNotificationsByStatus(status);
        return Result.success("获取成功", notifications);
    }

    /**
     * 获取最近的通知
     *
     * @param limit 数量限制，默认5条
     * @return 通知列表
     */
    @GetMapping("/recent")
    public Result<List<Notification>> getRecentNotifications(@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        List<Notification> notifications = notificationService.getRecentNotifications(limit);
        return Result.success("获取成功", notifications);
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
     * 添加通知
     *
     * @param notification 通知信息
     * @return 添加结果
     */
    @PostMapping
    public Result addNotification(@RequestBody Notification notification) {
        try {
            // 注意：这里需要前端配合传递附件ID列表，或者在Service层处理附件上传和关联
            // 假设暂时不处理附件
            notificationService.addNotification(notification);
            return Result.success("添加成功");
        } catch (CustomException ce) {
            log.warn("添加通知失败: {}", ce.getMessage());
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            log.error("添加通知时发生错误", e);
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新通知
     *
     * @param notification 通知信息
     * @return 更新结果
     */
    @PutMapping
    public Result updateNotification(@RequestBody Notification notification) {
        try {
            // 假设暂时不处理附件
            notificationService.updateNotification(notification);
            return Result.success("更新成功");
        } catch (CustomException ce) {
            log.warn("更新通知失败: {}", ce.getMessage());
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            log.error("更新通知时发生错误", e);
            return Result.error("更新失败: " + e.getMessage());
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
    public Result updateNotificationStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            notificationService.updateNotificationStatus(id, status);
            return Result.success("更新状态成功");
        } catch (Exception e) {
            log.error("更新通知状态失败, ID: {}, Status: {}", id, status, e);
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
    public Result deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除通知失败, ID: {}", id, e);
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
    public Result batchDeleteNotifications(@RequestBody List<Long> ids) {
        try {
            notificationService.batchDeleteNotifications(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除通知失败, IDs: {}", ids, e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取通知列表
     *
     * @param pageNo       页码
     * @param pageSize     每页数量
     * @param notification 查询条件 (可选)
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<IPage<Notification>> getNotificationPage(@RequestParam(defaultValue = "1") int pageNo,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                           Notification notification) {
        try {
            IPage<Notification> page = notificationService.getNotificationPage(pageNo, pageSize, notification);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("分页查询通知失败", e);
            return Result.error("查询失败");
        }
    }
} 