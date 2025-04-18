package com.campus.controller;

import com.campus.dto.NoticeDTO;
import com.campus.entity.Notice;
import com.campus.exception.CustomException;
import com.campus.service.NoticeService;
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
@RequestMapping("/api/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;
    
    /**
     * 获取通知详情
     * @param id 通知ID
     * @return 通知详情
     */
    @GetMapping("/{id}")
    public Result getNotice(@PathVariable("id") Long id) {
        try {
            Notice notice = noticeService.getNoticeWithAttachments(id);
            if (notice != null) {
                noticeService.incrementViewCount(id);
                return Result.success("获取成功", notice);
            } else {
                return Result.error("通知不存在");
            }
        } catch (Exception e) {
            log.error("获取通知详情失败 (ID: {})", id, e);
            return Result.error("获取通知详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有通知
     * @return 通知列表
     */
    @GetMapping
    public Result getAllNotices() {
        List<Notice> notices = noticeService.getAllNotices();
        return Result.success("获取成功", notices);
    }
    
    /**
     * 根据类型获取通知
     * @param noticeType 通知类型
     * @return 通知列表
     */
    @GetMapping("/type/{noticeType}")
    public Result getNoticesByType(@PathVariable Integer noticeType) {
        List<Notice> notices = noticeService.getNoticesByType(noticeType);
        return Result.success("获取成功", notices);
    }
    
    /**
     * 根据状态获取通知
     * @param status 通知状态
     * @return 通知列表
     */
    @GetMapping("/status/{status}")
    public Result getNoticesByStatus(@PathVariable Integer status) {
        List<Notice> notices = noticeService.getNoticesByStatus(status);
        return Result.success("获取成功", notices);
    }
    
    /**
     * 获取最近的通知
     * @param limit 数量限制，默认5条
     * @return 通知列表
     */
    @GetMapping("/recent")
    public Result getRecentNotices(@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        List<Notice> notices = noticeService.getRecentNotices(limit);
        return Result.success("获取成功", notices);
    }
    
    /**
     * 获取置顶通知
     * @return 通知列表
     */
    @GetMapping("/top")
    public Result getTopNotices() {
        List<Notice> notices = noticeService.getTopNotices();
        return Result.success("获取成功", notices);
    }
    
    /**
     * 根据发布者ID获取通知
     * @param publisherId 发布者ID
     * @return 通知列表
     */
    @GetMapping("/publisher/{publisherId}")
    public Result getNoticesByPublisherId(@PathVariable Long publisherId) {
        List<Notice> notices = noticeService.getNoticesByPublisherId(publisherId);
        return Result.success("获取成功", notices);
    }
    
    /**
     * 添加通知
     * @param noticeDto 通知信息 DTO (包含 attachmentIds)
     * @return 添加结果
     */
    @PostMapping
    public Result addNotice(@RequestBody NoticeDTO noticeDto) {
        try {
            noticeService.addNotice(noticeDto);
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
     * @param id 通知ID
     * @param noticeDto 通知信息 DTO (包含 attachmentIds)
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result updateNotice(@PathVariable Long id, @RequestBody NoticeDTO noticeDto) {
        try {
            noticeDto.setId(id);
            noticeService.updateNotice(noticeDto);
            return Result.success("更新成功");
        } catch (CustomException ce) {
            log.warn("更新通知失败 (ID: {}): {}", id, ce.getMessage());
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            log.error("更新通知时发生错误 (ID: {})", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新通知状态
     * @param id 通知ID
     * @param status 通知状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    public Result updateNoticeStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (noticeService.updateNoticeStatus(id, status)) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }
    
    /**
     * 删除通知
     * @param id 通知ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteNotice(@PathVariable Long id) {
        if (noticeService.deleteNotice(id)) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 批量删除通知
     * @param ids 通知ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result batchDeleteNotices(@RequestBody Long[] ids) {
        if (noticeService.batchDeleteNotices(ids)) {
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }
} 