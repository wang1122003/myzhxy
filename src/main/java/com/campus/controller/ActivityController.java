package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Activity;
import com.campus.entity.User;
import com.campus.service.ActivityService;
import com.campus.service.AuthService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private FileController fileController;

    @Autowired
    private AuthService authService;
    
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
     * @param status 活动状态 (可选)
     * @return 分页后的活动列表及分页信息
     */
    @GetMapping
    public Result getAllActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize, // 保持参数名为 pageSize
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer activityType,
            @RequestParam(required = false) Integer status
    ) {
        try {
            IPage<Activity> activityPage = activityService.getActivityPage(page, pageSize, keyword, activityType, status);
            // 将 IPage 转换为包含 list 和 total 的 Map，以便前端处理
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", activityPage.getRecords());
            responseData.put("total", activityPage.getTotal());
            responseData.put("pages", activityPage.getPages());
            responseData.put("currentPage", activityPage.getCurrent());
            responseData.put("pageSize", activityPage.getSize());

            return Result.success(responseData);
        } catch (Exception e) {
            // log.error("获取活动列表失败", e);
            return Result.error("获取活动列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型获取活动
     * @param activityType 活动类型
     * @return 活动列表
     */
    @GetMapping("/type/{activityType}")
    public ResponseEntity<List<Activity>> getActivitiesByType(@PathVariable Integer activityType) {
        return ResponseEntity.ok(activityService.getActivitiesByType(activityType));
    }
    
    /**
     * 根据状态获取活动
     * @param status 活动状态
     * @return 活动列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Activity>> getActivitiesByStatus(@PathVariable Integer status) {
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
     * 上传活动海报
     * @param file 海报图片文件
     * @return 上传结果
     */
    @PostMapping("/poster/upload")
    public Result<String> uploadPoster(@RequestParam("file") MultipartFile file) {
        return fileController.uploadActivityPoster(file);
    }
    
    /**
     * 创建活动（包含海报上传）
     * @param activity 活动信息
     * @param poster 海报图片
     * @return 创建结果
     */
    @PostMapping("/with-poster")
    public Result<Activity> addActivityWithPoster(
            @RequestPart("activity") Activity activity,
            @RequestPart("poster") MultipartFile poster) {
        
        // 先上传海报
        Result<String> posterResult = fileController.uploadActivityPoster(poster);
        
        if (posterResult.isSuccess()) {
            // 设置海报URL
            activity.setPosterUrl(posterResult.getData());
            
            // 保存活动信息
            if (activityService.addActivity(activity)) {
                return Result.success("活动创建成功", activity);
            } else {
                return Result.error("活动创建失败");
            }
        } else {
            return Result.error("海报上传失败: " + posterResult.getMessage());
        }
    }
    
    /**
     * 更新活动
     * @param id 活动ID
     * @param activity 活动信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        if (activityService.updateActivity(activity)) {
            return ResponseEntity.ok("活动更新成功");
        } else {
            return ResponseEntity.badRequest().body("活动更新失败");
        }
    }
    
    /**
     * 删除活动
     * @param id 活动ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteActivity(@PathVariable Long id) {
        if (activityService.deleteActivity(id)) {
            return ResponseEntity.ok("活动删除成功");
        } else {
            return ResponseEntity.badRequest().body("活动删除失败");
        }
    }
    
    /**
     * 批量删除活动
     * @param ids 活动ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<String> batchDeleteActivities(@RequestBody Long[] ids) {
        if (activityService.batchDeleteActivities(ids)) {
            return ResponseEntity.ok("批量删除成功");
        } else {
            return ResponseEntity.badRequest().body("批量删除失败");
        }
    }
    
    /**
     * 更新活动状态
     * @param id 活动ID
     * @param status 活动状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<String> updateActivityStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (activityService.updateActivityStatus(id, status)) {
            return ResponseEntity.ok("状态更新成功");
        } else {
            return ResponseEntity.badRequest().body("状态更新失败");
        }
    }

    /**
     * 获取当前学生参加的活动列表
     *
     * @param request HTTP请求，用于获取当前用户
     * @return 活动列表
     */
    @GetMapping("/student/my")
    public Result getStudentActivities(HttpServletRequest request) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("未登录或无法获取用户信息");
        }

        // 权限检查：确保是学生
        // 注意：User 类中表示学生类型的字段需要确认，假设是 1
        // if (currentUser.getUserType() == null || currentUser.getUserType() != 1) {
        //     return Result.error("非学生用户无权访问此接口");
        // }

        try {
            Long studentId = currentUser.getId();
            List<Activity> activities = activityService.getStudentEnrolledActivities(studentId);
            return Result.success(activities);
        } catch (Exception e) {
            // logger.error("获取学生参加的活动列表失败, studentId: {}", currentUser.getId(), e);
            return Result.error("获取活动列表失败: " + e.getMessage());
        }
    }
}