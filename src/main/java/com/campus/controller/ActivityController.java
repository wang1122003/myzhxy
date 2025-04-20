package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Activity;
import com.campus.entity.User;
import com.campus.exception.CustomException;
import com.campus.service.ActivityService;
import com.campus.service.AuthService;
import com.campus.service.FileService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
            log.error("海报上传失败", e);
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
    public Result addActivityWithPoster(@RequestPart("activity") Activity activity,
                                        @RequestPart(value = "posterFile", required = false) MultipartFile posterFile,
                                        HttpServletRequest request) {
        try {
            // 1. Upload poster if provided
            if (posterFile != null && !posterFile.isEmpty()) {
                // Assume fileService.uploadActivityPoster returns String directly
                String posterUrl = fileService.uploadActivityPoster(posterFile);
                if (posterUrl != null) {
                    activity.setPosterUrl(posterUrl); // Assuming setPosterUrl exists
                } else {
                    log.error("海报上传失败:未能获取URL");
                    return Result.error("海报上传失败: 未能获取URL");
                }
            }

            // 2. Set publisher ID from authenticated user
            User currentUser = authService.getCurrentUserFromRequest(request); // Assuming method exists
            if (currentUser != null) {
                activity.setPublisherId(currentUser.getId()); // Assuming setPublisherId exists
            } else {
                return Result.error("无法获取发布者信息，请登录");
            }

            // 3. Add activity
            boolean result = activityService.addActivity(activity);
            return result ? Result.success("活动添加成功", activity) : Result.error("活动添加失败");
        } catch (IOException e) {
            log.error("处理文件或添加活动时出错", e);
            return Result.error("处理文件或添加活动时发生错误: " + e.getMessage());
        } catch (CustomException e) {
            log.error("活动添加失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("活动添加时发生未知错误", e);
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
    public Result updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        boolean result = activityService.updateActivity(activity);
        return result ? Result.success("更新成功") : Result.error("更新失败");
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
        User currentUser = authService.getCurrentUserFromRequest(request);
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

    /**
     * 学生报名参加活动
     *
     * @param activityId 活动ID
     * @param request    HTTP请求
     * @return 报名结果
     */
    @PostMapping("/join/{activityId}")
    public Result joinActivity(@PathVariable Long activityId, HttpServletRequest request) {
        User currentUser = authService.getCurrentUserFromRequest(request);
        if (currentUser == null) {
            return Result.error(401, "用户未登录或认证失败");
        }
        try {
            boolean success = activityService.joinActivity(activityId, currentUser.getId());
            return success ? Result.success("报名成功") : Result.error("报名失败，可能已报名或活动不存在");
        } catch (CustomException e) {
            log.error("Join activity error for activity {} and user {}: {}", activityId, currentUser.getId(), e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error joining activity {} for user {}: {}", activityId, currentUser.getId(), e.getMessage(), e);
            return Result.error("报名时发生未知错误");
        }
    }

    /**
     * 学生退出活动
     *
     * @param id      活动ID
     * @param request HTTP请求
     * @return 退出结果
     */
    @PostMapping("/quit/{id}")
    public Result quitActivity(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = null; // 将声明移到 try 块外部
        try {
            // 获取当前用户信息
            currentUser = authService.getCurrentUserFromRequest(request);
            if (currentUser == null) {
                return Result.error(401, "用户未登录");
            }

            boolean success = activityService.quitActivity(id, currentUser.getId());
            return success ? Result.success("退出成功") : Result.error("退出失败，可能未报名或活动不存在");
        } catch (CustomException e) {
            log.error("Quit activity error for activity {} and user {}: {}", id, currentUser.getId(), e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error quitting activity {} for user {}: {}", id, currentUser.getId(), e.getMessage(), e);
            return Result.error("退出时发生未知错误");
        }
    }
}