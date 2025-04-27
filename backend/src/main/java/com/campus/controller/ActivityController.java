package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Activity;
import com.campus.entity.User;
import com.campus.entity.FileRecord;
import com.campus.exception.AuthenticationException;
import com.campus.exception.ResourceNotFoundException;
import com.campus.service.ActivityService;
import com.campus.service.AuthService;
import com.campus.service.FileService;
import com.campus.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.campus.enums.UserType;

/**
 * 校园活动控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthService authService;

    /**
     * 获取活动详情
     *
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
     *
     * @param page         页码, 默认为1
     * @param pageSize     每页数量, 默认为10 (注意前端请求的是pageSize)
     * @param keyword      关键词 (可选)
     * @param activityType 活动类型 (可选)
     * @param statusInt    活动状态 (可选, Integer for request param)
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
     *
     * @param activityType 活动类型
     * @return 活动列表
     */
    @GetMapping("/type/{activityType}")
    public ResponseEntity<List<Activity>> getActivitiesByType(@PathVariable String activityType) {
        return ResponseEntity.ok(activityService.getActivitiesByType(activityType));
    }

    /**
     * 根据状态获取活动
     *
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
     *
     * @return 活动列表
     */
    @GetMapping("/ongoing")
    public ResponseEntity<List<Activity>> getOngoingActivities() {
        return ResponseEntity.ok(activityService.getOngoingActivities());
    }

    /**
     * 获取即将开始的活动
     *
     * @param days 未来天数，默认值7
     * @return 活动列表
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Activity>> getUpcomingActivities(@RequestParam(defaultValue = "7") Integer days) {
        return ResponseEntity.ok(activityService.getUpcomingActivities(days));
    }

    /**
     * 创建活动
     *
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
     * 单独上传活动海报文件 (需要关联的 activityId)
     *
     * @param file       文件
     * @param activityId 要关联的活动ID
     * @param user       当前登录用户
     * @return 文件访问URL
     */
    @PostMapping("/poster/upload")
    public Result<String> uploadPoster(@RequestParam("file") MultipartFile file,
                                       @RequestParam("activityId") Long activityId,
                                       @AuthenticationPrincipal User user) {
        if (user == null) {
            return Result.error(401, "用户未登录");
        }
        if (activityId == null) {
            return Result.error(400, "必须提供 activityId");
        }
        // Optional: Check if activity exists and user has permission to modify it
        // Activity existingActivity = activityService.getActivityById(activityId);
        // if (existingActivity == null) return Result.error(404, "活动不存在");
        // if (!Objects.equals(existingActivity.getOrganizerId(), user.getId()) /* && !isAdmin */) {
        //     return Result.error(403, "无权修改该活动的海报");
        // }

        try {
            // Call the new uploadFile service
            FileRecord record = fileService.uploadFile(file, user.getId(), "activity_poster", activityId);
            // Assuming filePath needs to be converted to a web-accessible URL
            // This conversion logic might need adjustment based on FileServiceImpl and web server config
            String url = record.getFilePath(); // Placeholder: might need URL conversion

            // Update the activity's poster URL
            Activity activityToUpdate = new Activity();
            activityToUpdate.setId(activityId);
            activityToUpdate.setPosterUrl(url); // Save the URL/path
            activityService.updateActivity(activityToUpdate);

            return Result.success("上传成功", url);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("处理海报上传失败: " + e.getMessage());
        }
    }

    /**
     * 创建活动（包含海报上传）
     *
     * @param activity   活动信息 (JSON part)
     * @param posterFile 海报图片 (File part, optional)
     * @param user       当前登录用户
     * @return 创建结果 (包含创建的活动信息)
     */
    @PostMapping("/with-poster")
    public Result<Activity> addActivityWithPoster(@RequestPart("activity") Activity activity,
                                                  @RequestPart(value = "posterFile", required = false) MultipartFile posterFile,
                                                  @AuthenticationPrincipal User user) {
        if (user == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            // 1. Set organizer ID from authenticated user
            activity.setOrganizerId(user.getId());

            // 2. Save the activity first to get its ID
            boolean created = activityService.addActivity(activity);
            if (!created || activity.getId() == null) {
                return Result.error("创建活动失败");
            }
            Long newActivityId = activity.getId();

            // 3. Upload poster if provided and associate with the new activity ID
            if (posterFile != null && !posterFile.isEmpty()) {
                try {
                    FileRecord record = fileService.uploadFile(posterFile, user.getId(), "activity_poster", newActivityId);
                    // Assuming filePath needs conversion to URL
                    String posterUrl = record.getFilePath(); // Placeholder: Convert path to URL if needed

                    // 4. Update the newly created activity with the poster URL
                    Activity activityToUpdate = new Activity();
                    activityToUpdate.setId(newActivityId);
                    activityToUpdate.setPosterUrl(posterUrl);
                    activityService.updateActivity(activityToUpdate);

                    // Update the activity object to return with the URL
                    activity.setPosterUrl(posterUrl);

                } catch (IOException e) {
                    log.error("海报上传失败，但活动已创建 (activityId: {}): {}", newActivityId, e.getMessage());
                    // Return success but indicate poster upload failure
                    return Result.success("活动创建成功，但海报上传失败: " + e.getMessage(), activity);
                } catch (Exception e) {
                    log.error("处理海报上传时出错 (activityId: {}): {}", newActivityId, e.getMessage());
                    return Result.success("活动创建成功，但处理海报时出错: " + e.getMessage(), activity);
                }
            }

            // 5. Return the created activity (potentially with poster URL)
            return Result.success("活动创建成功", activity);

        } catch (Exception e) {
            log.error("创建活动时发生错误", e);
            return Result.error("创建活动时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新活动
     *
     * @param id       活动ID
     * @param activity 活动信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Object> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        try {
            Activity existingActivity = activityService.getActivityById(id);
            if (existingActivity == null) {
                return Result.error("活动不存在");
            }
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new AuthenticationException("用户未登录");
            }
            if (!existingActivity.getOrganizerId().equals(currentUser.getId()) /* && !isAdmin(currentUser) */) {
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
     *
     * @param id 活动ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteActivity(@PathVariable Long id) {
        try {
            Activity existingActivity = activityService.getActivityById(id);
            if (existingActivity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("活动不存在");
            }
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请登录后操作");
            }
            if (!existingActivity.getOrganizerId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权删除此活动");
            }

            boolean success = activityService.deleteActivity(id);
            return success ? ResponseEntity.ok("活动删除成功") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除活动失败");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除活动时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新活动状态
     *
     * @param id        活动ID
     * @param statusInt 活动状态 (使用 Integer 接收路径参数)
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{statusInt}")
    public ResponseEntity<String> updateActivityStatus(@PathVariable Long id, @PathVariable Integer statusInt) {
        // TODO: Add permission check (e.g., only organizer or admin)
        try {
            Activity existingActivity = activityService.getActivityById(id);
            if (existingActivity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("活动不存在");
            }
            User currentUser = authService.getCurrentAuthenticatedUser(); // Get current user
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请登录后操作");
            }
            // Check if user is organizer or admin (assuming isAdmin method or role check)
            if (!existingActivity.getOrganizerId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权修改活动状态");
            }

            // Validate statusInt before converting
            String status;
            try {
                // Convert Integer to String status based on your ActivityStatus enum or convention
                // This example assumes numeric strings like "0", "1", "2" or enum names
                status = String.valueOf(statusInt); // Or map to enum names if needed
                // Add validation if statusInt should map to specific enum values
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("无效的状态值格式");
            }

            boolean success = activityService.updateActivityStatus(id, status);
            return success ? ResponseEntity.ok("活动状态更新成功") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("活动状态更新失败");
        } catch (Exception e) {
            log.error("更新活动 {} 状态为 {} 时出错", id, statusInt, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新活动状态时发生错误: " + e.getMessage());
        }
    }

    // Helper method to check admin role (replace with your actual logic)
    private boolean isAdmin(User user) {
        // Example: return user.getRoles().contains(UserType.ADMIN);
        // Assuming UserType enum and User has getRoles() method
        // Reverting to String comparison as UserType enum is not resolved
        // return "ADMIN".equalsIgnoreCase(user.getUserType()); // Simplified check using userType
        return user.getUserType() == UserType.ADMIN; // Corrected comparison
    }

    /**
     * 获取当前用户的辅助方法
     *
     * @return 当前用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 如果需要完整的 User 对象，需要注入 UserService 并查询
                // String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                // return userService.findByUsername(username);
                return null; // 暂时返回 null，因为 AuthServcie 实现未知
            } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
                // return userService.findByUsername((String) principal);
                return null; // 暂时返回 null
            }
        }
        // 或者尝试从 AuthService 获取 (如果它能从 SecurityContext 获取)
        return authService.getCurrentAuthenticatedUser(); // Corrected call
    }

    @PostMapping("/join/{id}")
    public ResponseEntity<?> joinActivity(@PathVariable Long id) {
        try {
            // Ensure this uses the correct method
            User currentUser = authService.getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error("用户未认证或无法获取用户信息"));
            }
            // The call to activityService.joinActivity(id, currentUser.getId()) is now expected to work
            // because we added the method to the ActivityService interface.
            boolean success = activityService.joinActivity(id, currentUser.getId());
            if (success) {
                return ResponseEntity.ok(Result.success("成功参与活动"));
            } else {
                // Consider specific error from service layer if available
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error("参与活动失败"));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(e.getMessage()));
        } catch (IllegalStateException e) {
            // Conflict usually means already joined or activity not open for joining
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.error(e.getMessage()));
        } catch (Exception e) {
            // Log the exception
            // log.error("Error joining activity {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error("发生意外错误，请稍后重试"));
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelJoinActivity(@PathVariable Long id) {
        try {
            // Ensure this uses the correct method
            User currentUser = authService.getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error("用户未认证"));
            }
            // The call to activityService.cancelJoinActivity(id, currentUser.getId()) is now expected to work.
            boolean success = activityService.cancelJoinActivity(id, currentUser.getId());
            if (success) {
                return ResponseEntity.ok(Result.success("成功取消报名"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error("取消报名失败"));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.error(e.getMessage()));
        } catch (Exception e) {
            // log.error("Error cancelling join for activity {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error("发生意外错误"));
        }
    }

    /**
     * 批量删除活动
     *
     * @param ids 活动ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteActivities(@RequestBody List<Long> ids) {
        // TODO: Implement batch deletion logic in service
        try {
            User currentUser = authService.getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "请登录后操作");
            }
            // Add permission check here if needed (e.g., only admins can batch delete)
            // if (!isAdmin(currentUser)) {
            //     return Result.error(HttpStatus.FORBIDDEN.value(), "无权执行批量删除");
            // }

            // Convert List<Long> to Long[] before calling service
            Long[] idArray = ids.toArray(new Long[0]);
            boolean success = activityService.batchDeleteActivities(idArray);
            return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除活动时出错: {}", ids, e);
            return Result.error("批量删除时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据发布者ID获取活动列表
     *
     * @param publisherId 发布者ID
     * @param page        页码
     * @param pageSize    每页大小
     * @return 活动列表
     */
    @GetMapping("/publisher/{publisherId}")
    public Result<IPage<Activity>> getActivitiesByPublisher(
            @PathVariable Long publisherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // TODO: Implement service logic
        try {
            IPage<Activity> activityPage = activityService.getActivitiesByPublisher(publisherId, page, pageSize);
            return Result.success(activityPage);
        } catch (Exception e) {
            log.error("根据发布者 {} 获取活动时出错", publisherId, e);
            return Result.error("获取活动列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前学生参加/报名的活动列表
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @return 活动列表
     */
    @GetMapping("/student/my")
    public Result<IPage<Activity>> getMyActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "请登录后查看");
        }
        // Reverting to String comparison as UserType enum is not resolved
        // if (!"STUDENT".equalsIgnoreCase(currentUser.getUserType())) { // Check if user is a student
        if (currentUser.getUserType() != UserType.STUDENT) { // Corrected check for student type
            return Result.error(HttpStatus.FORBIDDEN.value(), "只有学生可以查看\'我的活动\'");
        }
        // TODO: Implement service logic to get activities joined by currentUser.getId()
        try {
            IPage<Activity> activityPage = activityService.getActivitiesJoinedByUser(currentUser.getId(), page, pageSize);
            return Result.success(activityPage);
        } catch (Exception e) {
            log.error("获取用户 {} 的活动时出错", currentUser.getId(), e);
            return Result.error("获取我的活动列表失败: " + e.getMessage());
        }
    }

    /**
     * 评价活动
     *
     * @param id 活动ID
     * @param ratingData 评价数据 (e.g., Map with score, comment)
     * @return 评价结果
     */
    @PostMapping("/rate/{id}")
    public Result<String> rateActivity(@PathVariable Long id, @RequestBody Map<String, Object> ratingData) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "请登录后评价");
        }
        // TODO: Implement rating logic in service
        try {
            // Extract rating details from map safely
            Integer score = (Integer) ratingData.get("score"); // Add validation
            String comment = (String) ratingData.get("comment"); // Add validation

            boolean success = activityService.rateActivity(id, currentUser.getId(), score, comment);
            return success ? Result.success("评价成功") : Result.error("评价失败或重复评价");
        } catch (Exception e) {
            log.error("评价活动 {} 时出错", id, e);
            return Result.error("评价活动时发生错误: " + e.getMessage());
        }
    }
}