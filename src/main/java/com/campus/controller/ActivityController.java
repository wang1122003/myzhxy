package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Activity;
import com.campus.entity.User;
import com.campus.exception.AuthenticationException;
import com.campus.exception.CustomException;
import com.campus.service.ActivityService;
import com.campus.service.AuthService;
import com.campus.service.FileService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 校园活动控制器
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private FileService fileService;

    @Autowired
    private AuthService authService;

    @Autowired
    private HttpServletRequest request;
    
    /**
     * 获取活动详情
     * @param id 活动ID
     * @return 活动详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivity(@PathVariable Long id) {
        Activity activity = activityService.getActivityById(id);
        if (activity != null) {
            return ResponseEntity.ok(activity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取活动列表（支持分页和搜索）
     * @param page 页码, 默认为1
     * @param pageSize 每页数量, 默认为10 (注意前端请求的是pageSize)
     * @param keyword 关键词 (可选)
     * @param activityType 活动类型 (可选)
     * @param statusInt 活动状态 (可选, Integer for request param)
     * @return 分页后的活动列表及分页信息
     */
    @GetMapping
    public Result<Map<String, Object>> getAllActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) Integer statusInt
    ) {
        try {
            String status = (statusInt != null) ? String.valueOf(statusInt) : null;

            IPage<Activity> activityPage = activityService.getActivityPage(page, pageSize, keyword, activityType, status);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", activityPage.getRecords());
            responseData.put("total", activityPage.getTotal());
            responseData.put("pages", activityPage.getPages());
            responseData.put("currentPage", activityPage.getCurrent());
            responseData.put("pageSize", activityPage.getSize());

            return Result.success(responseData);
        } catch (Exception e) {
            return Result.error("获取活动列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型获取活动
     * @param activityType 活动类型
     * @return 活动列表
     */
    @GetMapping("/type/{activityType}")
    public ResponseEntity<List<Activity>> getActivitiesByType(@PathVariable String activityType) {
        return ResponseEntity.ok(activityService.getActivitiesByType(activityType));
    }
    
    /**
     * 根据状态获取活动
     * @param statusInt 活动状态
     * @return 活动列表
     */
    @GetMapping("/status/{statusInt}")
    public ResponseEntity<List<Activity>> getActivitiesByStatus(@PathVariable Integer statusInt) {
        String status = String.valueOf(statusInt);
        return ResponseEntity.ok(activityService.getActivitiesByStatus(status));
    }
    
    /**
     * 获取进行中的活动
     * @return 活动列表
     */
    @GetMapping("/ongoing")
    public ResponseEntity<List<Activity>> getOngoingActivities() {
        return ResponseEntity.ok(activityService.getOngoingActivities());
    }
    
    /**
     * 获取即将开始的活动
     * @param days 未来天数，默认值7
     * @return 活动列表
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Activity>> getUpcomingActivities(@RequestParam(defaultValue = "7") Integer days) {
        return ResponseEntity.ok(activityService.getUpcomingActivities(days));
    }
    
    /**
     * 创建活动
     * @param activity 活动信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<String> addActivity(@RequestBody Activity activity) {
        if (activityService.addActivity(activity)) {
            return ResponseEntity.ok("活动创建成功");
        } else {
            return ResponseEntity.badRequest().body("活动创建失败");
        }
    }
    
    /**
     * 单独上传活动海报文件
     * @param file 文件
     * @return 文件访问URL
     */
    @PostMapping("/poster/upload")
    public Result<String> uploadPoster(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileService.uploadActivityPoster(file);
            if (url != null) {
                return Result.success("上传成功", url);
            } else {
                return Result.error("上传失败，无法获取URL");
            }
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建活动（包含海报上传）
     * @param activity 活动信息
     * @param posterFile 海报图片
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping("/with-poster")
    public Result<Activity> addActivityWithPoster(@RequestPart("activity") Activity activity,
                                        @RequestPart(value = "posterFile", required = false) MultipartFile posterFile,
                                        HttpServletRequest request) {
        try {
            // 1. Upload poster if provided
            if (posterFile != null && !posterFile.isEmpty()) {
                String posterUrl = fileService.uploadActivityPoster(posterFile);
                if (posterUrl != null) {
                    activity.setPosterUrl(posterUrl);
                } else {
                    return Result.error("海报上传失败: 未能获取URL");
                }
            }

            // 2. Set organizer ID from authenticated user
            User currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser != null) {
                activity.setOrganizerId(currentUser.getId());
            } else {
                return Result.error("无法获取发布者信息，请登录");
            }

            // 3. Add activity
            boolean result = activityService.addActivity(activity);
            return result ? Result.success("活动添加成功", activity) : Result.error("活动添加失败");
        } catch (IOException e) {
            return Result.error("处理文件或添加活动时发生错误: " + e.getMessage());
        } catch (CustomException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("添加活动时发生未知错误");
        }
    }
    
    /**
     * 更新活动
     * @param id 活动ID
     * @param activity 活动信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Object> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        try {
            // Permission Check: Only organizer or admin can update
            Activity existingActivity = activityService.getActivityById(id);
            if (existingActivity == null) {
                return Result.error("活动不存在");
            }
            User currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser == null) {
                throw new AuthenticationException("用户未登录");
            }
            // Basic check: Organizer can update. Add admin check if needed.
            if (!existingActivity.getOrganizerId().equals(currentUser.getId()) /* && !authService.isAdmin(currentUser) */) {
                return Result.error("无权修改此活动");
            }

            activity.setId(id);
            boolean result = activityService.updateActivity(activity);
            return result ? Result.success("更新成功") : Result.error("更新失败");
        } catch (AuthenticationException ae) {
            return Result.error(401, ae.getMessage());
        } catch (Exception e) {
            return Result.error("更新活动时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 删除活动
     * @param id 活动ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteActivity(@PathVariable Long id) {
        try {
            // Permission Check: Only organizer or admin can delete
            Activity existingActivity = activityService.getActivityById(id);
            if (existingActivity == null) {
                return ResponseEntity.ok("活动不存在或已被删除"); // Already deleted
            }
            User currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser == null) {
                // Use ResponseEntity for consistency
                return ResponseEntity.status(401).body("用户未登录");
            }
            // Basic check: Organizer can delete. Add admin check if needed.
            if (!existingActivity.getOrganizerId().equals(currentUser.getId()) /* && !authService.isAdmin(currentUser) */) {
                return ResponseEntity.status(403).body("无权删除此活动");
            }

            if (activityService.deleteActivity(id)) {
                return ResponseEntity.ok("活动删除成功");
            } else {
                return ResponseEntity.badRequest().body("活动删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("删除活动时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除活动
     * @param ids 活动ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<String> batchDeleteActivities(@RequestBody Long[] ids) {
        // TODO: Implement fine-grained permission check for batch delete (e.g., check each ID or require admin)
        // Simple admin check example (requires authService.isAdmin method)
        /*
        User currentUser = authService.getCurrentUserFromRequest(request);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("用户未登录");
        }
        if (!authService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).body("无权执行批量删除操作");
        }
        */
        try {
            if (activityService.batchDeleteActivities(ids)) {
                return ResponseEntity.ok("批量删除成功");
            } else {
                return ResponseEntity.badRequest().body("批量删除失败 (部分活动可能不存在)");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("批量删除活动时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新活动状态
     * @param id 活动ID
     * @param statusInt 活动状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{statusInt}")
    public ResponseEntity<String> updateActivityStatus(@PathVariable Long id, @PathVariable Integer statusInt) {
        // TODO: Add permission check (e.g., only organizer or admin)
        String status = String.valueOf(statusInt);
        if (activityService.updateActivityStatus(id, status)) {
            return ResponseEntity.ok("活动状态更新成功");
        } else {
            return ResponseEntity.badRequest().body("活动状态更新失败");
        }
    }

    /**
     * 获取学生已报名的活动
     * @param request HTTP请求 (获取当前用户)
     * @return 活动列表
     */
    @GetMapping("/student/my")
    public Result<List<Activity>> getStudentActivities(HttpServletRequest request) {
        return Result.error(501, "获取已报名活动功能暂未实现");
    }

    /**
     * 学生报名参加活动
     * @param activityId 活动ID
     * @param request    HTTP请求 (获取当前用户)
     * @return 报名结果
     */
    @PostMapping("/join/{activityId}")
    public Result<Object> joinActivity(@PathVariable Long activityId, HttpServletRequest request) {
        return Result.error(501, "报名活动功能暂未实现");
    }

    /**
     * 学生退出活动
     * @param id      活动ID
     * @param request HTTP请求 (获取当前用户)
     * @return 退出结果
     */
    @PostMapping("/quit/{id}")
    public Result<Object> quitActivity(@PathVariable Long id, HttpServletRequest request) {
        return Result.error(501, "退出活动功能暂未实现");
    }
}