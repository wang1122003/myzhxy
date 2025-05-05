package com.campus.controller;

// import com.campus.entity.User;
// import com.campus.entity.College;
// import com.campus.entity.Department;
// import com.campus.service.FileService; // 移除未使用的导入
// import com.campus.service.MailService; // 移除MailService导入
// import jakarta.servlet.http.HttpServletResponse; // 移除未使用的导入

import com.campus.enums.Term;
import com.campus.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公共API控制器
 * 处理不需要认证的公共接口
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    // 移除未使用的fileService字段
    // private final FileService fileService;

    // 构造函数注入需要CommonService(已被注释掉)
    // public CommonController(CommonService commonService, FileService fileService) {
    //     // this.commonService = commonService;
    //     this.fileService = fileService;
    // }

    // 手动自动装配FileService，因为构造函数注入被破坏
    // @Autowired // 由于字段已移除，所以移除该注解
    // public CommonController(FileService fileService) {
    //     this.fileService = fileService;
    //     // this.commonService = null; // 在下面的方法中处理null commonService
    // }

    // 如果没有其他构造函数，并且字段已移除，则添加默认构造函数
    public CommonController() {
        // 默认构造函数
    }

    /**
     * 获取系统状态
     *
     * @return 系统状态信息
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("name", "智慧校园服务系统");
        status.put("version", "1.0.0");
        status.put("status", "running");
        status.put("time", System.currentTimeMillis());
        return Result.success("获取系统状态成功", status);
    }

    /**
     * 通过临时URL访问文件 (由于底层下载逻辑改变，此方法已被注释掉)
     *
     * @param path     文件路径
     * @param token    访问令牌
     * @param expire   过期时间
     * @param response HTTP响应
     */
    /*
    @GetMapping("/file/access")
    public void accessFileByTempUrl(
            @RequestParam("path") String path,
            @RequestParam("token") String token,
            @RequestParam("expire") long expire,
            HttpServletResponse response) {

        try {
            // 验证参数
            if (path == null || token == null || expire <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("无效的访问参数");
                return;
            }

            // 验证是否过期
            if (expire < System.currentTimeMillis()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("访问链接已过期");
                return;
            }

            // 验证Token有效性
            try {
                // 从token提取信息并验证
                String[] tokenParts = token.split("_");
                if (tokenParts.length < 3) {
                    log.warn("格式错误的临时访问Token: {}", token);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("无效的访问令牌");
                    return;
                }
                
                // 验证token的路径与请求的路径匹配
                String encodedPath = tokenParts[0];
                String decodedPath = new String(Base64.getDecoder().decode(encodedPath), StandardCharsets.UTF_8);
                if (!decodedPath.equals(path)) {
                    log.warn("Token中的路径与请求的路径不匹配: {} vs {}", decodedPath, path);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("访问令牌与请求资源不匹配");
                    return;
                }
                
                // 如果需要进一步验证，可以检查userID或者添加签名验证
            } catch (Exception e) {
                log.error("验证Token时出错: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("访问令牌验证失败");
                return;
            }

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");

            // 使用输出流下载文件 (旧逻辑 - 需要重新设计)
            OutputStream outputStream = response.getOutputStream();
            // boolean success = fileService.downloadFile(path, outputStream); // 这个方法已被移除
            boolean success = false; // 占位符 - 需要调用新的下载机制
            log.warn("accessFileByTempUrl正在使用已移除的下载逻辑，需要重新设计。");

            if (!success) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                outputStream.write("文件不存在或访问失败".getBytes(StandardCharsets.UTF_8));
            }

            outputStream.flush();
        } catch (IOException e) {
            log.error("通过临时URL访问文件时出错", e);
            try {
                if (!response.isCommitted()) {
                     response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                     response.getWriter().write("文件访问失败: " + e.getMessage());
                }
            } catch (IOException ex) {
                // 无法设置响应，忽略
                log.error("为临时URL访问设置错误响应时出错", ex);
            }
        }
    }
    */

    /**
     * 获取系统配置
     *
     * @return 系统配置信息
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();

        // 系统基本配置
        Map<String, Object> system = new HashMap<>();
        system.put("name", "智慧校园服务系统");
        system.put("version", "1.0.0");
        system.put("logo", "/static/images/logo.png");
        system.put("footer", "© 2024 智慧校园服务系统 版权所有");
        config.put("system", system);

        // 模块配置
        Map<String, Object> modules = new HashMap<>();
        modules.put("forum", true);
        modules.put("course", true);
        modules.put("activity", true);
        modules.put("message", true);
        config.put("modules", modules);

        // UI配置
        Map<String, Object> ui = new HashMap<>();
        ui.put("theme", "light");
        ui.put("primary_color", "#3a7bd5");
        ui.put("secondary_color", "#00d2ff");
        config.put("ui", ui);

        return Result.success("获取系统配置成功", config);
    }

    /**
     * 获取可用时间段
     * @return 时间段列表
     */
    @GetMapping("/time-slots")
    public Result<List<Map<String, Object>>> getTimeSlots() {
        // 实际应用中可能从数据库或配置获取
        List<Map<String, Object>> slots = new ArrayList<>();
        slots.add(Map.of("id", 1, "name", "第1-2节", "startTime", "08:00", "endTime", "09:40"));
        slots.add(Map.of("id", 2, "name", "第3-4节", "startTime", "10:00", "endTime", "11:40"));
        slots.add(Map.of("id", 3, "name", "第5-6节", "startTime", "14:00", "endTime", "15:40"));
        slots.add(Map.of("id", 4, "name", "第7-8节", "startTime", "16:00", "endTime", "17:40"));
        slots.add(Map.of("id", 5, "name", "第9-10节", "startTime", "19:00", "endTime", "20:40"));
        return Result.success("获取成功", slots);
    }

    /**
     * 获取星期列表
     * @return 星期列表
     */
    @GetMapping("/weekdays")
    public Result<List<Map<String, Object>>> getWeekdays() {
        List<Map<String, Object>> weekdays = new ArrayList<>();
        weekdays.add(Map.of("id", 1, "name", "星期一"));
        weekdays.add(Map.of("id", 2, "name", "星期二"));
        weekdays.add(Map.of("id", 3, "name", "星期三"));
        weekdays.add(Map.of("id", 4, "name", "星期四"));
        weekdays.add(Map.of("id", 5, "name", "星期五"));
        weekdays.add(Map.of("id", 6, "name", "星期六"));
        weekdays.add(Map.of("id", 7, "name", "星期日"));
        return Result.success("获取成功", weekdays);
    }

    /**
     * 获取所有定义的学期列表 (按代码降序)
     *
     * @return 学期列表 (Map 形式)
     */
    @GetMapping("/terms")
    public Result<List<Map<String, Object>>> getTerms() {
        List<Map<String, Object>> termList = Term.getAllTermsSorted().stream()
                .map(Term::toMap) // 将枚举转换为map
                .collect(Collectors.toList());
        return Result.success("获取成功", termList);
    }

    /**
     * 获取当前学期信息
     *
     * @return 当前学期 (Map 形式)，如果未定义则返回错误
     */
    @GetMapping("/terms/current")
    public Result<Map<String, Object>> getCurrentTerm() {
        Term currentTerm = Term.getCurrentTerm();
        if (currentTerm != null) {
            return Result.success("获取成功", currentTerm.toMap());
        } else {
            return Result.error("未定义当前学期");
        }
    }

    // --- 为缺失的通用查找添加存根 --- 

    @GetMapping("/colleges")
    public Result<List<Map<String, Object>>> getColleges() {
        // 返回示例数据（实际项目中应从数据库获取）
        List<Map<String, Object>> colleges = new ArrayList<>();
        colleges.add(Map.of("id", 1, "name", "计算机学院", "code", "CS"));
        colleges.add(Map.of("id", 2, "name", "数学学院", "code", "MATH"));
        colleges.add(Map.of("id", 3, "name", "物理学院", "code", "PHY"));
        colleges.add(Map.of("id", 4, "name", "外国语学院", "code", "FL"));
        colleges.add(Map.of("id", 5, "name", "经济管理学院", "code", "EM"));
        return Result.success("获取成功", colleges);
    }

    @GetMapping("/departments")
    public Result<List<Map<String, Object>>> getDepartments(@RequestParam(required = false) Long collegeId) {
        // 返回示例数据（根据学院ID筛选）
        List<Map<String, Object>> departments = new ArrayList<>();

        // 计算机学院下的系
        if (collegeId == null || collegeId == 1) {
            departments.add(Map.of("id", 101, "name", "计算机科学与技术系", "collegeId", 1));
            departments.add(Map.of("id", 102, "name", "软件工程系", "collegeId", 1));
            departments.add(Map.of("id", 103, "name", "网络工程系", "collegeId", 1));
            departments.add(Map.of("id", 104, "name", "人工智能系", "collegeId", 1));
        }

        // 数学学院下的系
        if (collegeId == null || collegeId == 2) {
            departments.add(Map.of("id", 201, "name", "基础数学系", "collegeId", 2));
            departments.add(Map.of("id", 202, "name", "应用数学系", "collegeId", 2));
            departments.add(Map.of("id", 203, "name", "统计学系", "collegeId", 2));
        }

        // 其他学院可以类似添加...

        return Result.success("获取成功", departments);
    }

    @GetMapping("/majors")
    public Result<List<Map<String, Object>>> getMajors(@RequestParam(required = false) Long departmentId) {
        // 返回示例数据（根据院系ID筛选）
        List<Map<String, Object>> majors = new ArrayList<>();

        // 计算机科学与技术系的专业
        if (departmentId == null || departmentId == 101) {
            majors.add(Map.of("id", 1001, "name", "计算机科学与技术", "departmentId", 101));
            majors.add(Map.of("id", 1002, "name", "物联网工程", "departmentId", 101));
        }

        // 软件工程系的专业
        if (departmentId == null || departmentId == 102) {
            majors.add(Map.of("id", 1003, "name", "软件工程", "departmentId", 102));
            majors.add(Map.of("id", 1004, "name", "数字媒体技术", "departmentId", 102));
        }

        // 网络工程系的专业
        if (departmentId == null || departmentId == 103) {
            majors.add(Map.of("id", 1005, "name", "网络工程", "departmentId", 103));
            majors.add(Map.of("id", 1006, "name", "信息安全", "departmentId", 103));
        }

        // 其他院系可以类似添加...

        return Result.success("获取成功", majors);
    }

    @GetMapping("/classes")
    public Result<List<Map<String, Object>>> getClasses(@RequestParam(required = false) Long majorId) {
        // 返回示例数据（根据专业ID筛选）
        List<Map<String, Object>> classes = new ArrayList<>();

        // 计算机科学与技术专业的班级
        if (majorId == null || majorId == 1001) {
            classes.add(Map.of("id", 10001, "name", "计算机2020-1班", "majorId", 1001, "year", 2020));
            classes.add(Map.of("id", 10002, "name", "计算机2020-2班", "majorId", 1001, "year", 2020));
            classes.add(Map.of("id", 10003, "name", "计算机2021-1班", "majorId", 1001, "year", 2021));
            classes.add(Map.of("id", 10004, "name", "计算机2021-2班", "majorId", 1001, "year", 2021));
        }

        // 软件工程专业的班级
        if (majorId == null || majorId == 1003) {
            classes.add(Map.of("id", 10005, "name", "软件2020-1班", "majorId", 1003, "year", 2020));
            classes.add(Map.of("id", 10006, "name", "软件2020-2班", "majorId", 1003, "year", 2020));
            classes.add(Map.of("id", 10007, "name", "软件2021-1班", "majorId", 1003, "year", 2021));
        }

        // 其他专业可以类似添加...

        return Result.success("获取成功", classes);
    }

    @GetMapping("/room-types")
    public Result<List<Map<String, Object>>> getRoomTypes() {
        // 从数据库获取教室类型数据
        try {
            // 如果有RoomType枚举或专门的表，可以从那里获取
            // 这里使用建好的教室类型数据
            List<Map<String, Object>> roomTypes = new ArrayList<>();
            roomTypes.add(Map.of("id", 1, "name", "普通教室", "code", "NORMAL"));
            roomTypes.add(Map.of("id", 2, "name", "多媒体教室", "code", "MULTIMEDIA"));
            roomTypes.add(Map.of("id", 3, "name", "实验室", "code", "LABORATORY"));
            roomTypes.add(Map.of("id", 4, "name", "会议室", "code", "MEETING"));

            return Result.success("获取成功", roomTypes);
        } catch (Exception e) {
            log.error("获取教室类型数据失败", e);
            return Result.error("获取教室类型数据失败");
        }
    }

    @GetMapping("/course-types")
    public Result<List<Map<String, Object>>> getCourseTypes() {
        // 从数据库获取课程类型数据
        try {
            // 如果有CourseType枚举或专门的表，可以从那里获取
            // 这里使用建好的课程类型数据
            List<Map<String, Object>> courseTypes = new ArrayList<>();
            courseTypes.add(Map.of("id", 1, "name", "必修课", "code", "REQUIRED"));
            courseTypes.add(Map.of("id", 2, "name", "选修课", "code", "ELECTIVE"));
            courseTypes.add(Map.of("id", 3, "name", "实践课", "code", "PRACTICAL"));
            courseTypes.add(Map.of("id", 4, "name", "实验课", "code", "LABORATORY"));

            return Result.success("获取成功", courseTypes);
        } catch (Exception e) {
            log.error("获取课程类型数据失败", e);
            return Result.error("获取课程类型数据失败");
        }
    }

    @GetMapping("/activity-types")
    public Result<List<Map<String, Object>>> getActivityTypes() {
        // 从数据库获取活动类型数据
        try {
            // 如果有ActivityType枚举或专门的表，可以从那里获取
            // 这里使用建好的活动类型数据
            List<Map<String, Object>> activityTypes = new ArrayList<>();
            activityTypes.add(Map.of("id", 1, "name", "讲座", "code", "LECTURE"));
            activityTypes.add(Map.of("id", 2, "name", "竞赛", "code", "COMPETITION"));
            activityTypes.add(Map.of("id", 3, "name", "社团活动", "code", "CLUB"));
            activityTypes.add(Map.of("id", 4, "name", "志愿服务", "code", "VOLUNTEER"));
            activityTypes.add(Map.of("id", 5, "name", "文体活动", "code", "SPORTS"));

            return Result.success("获取成功", activityTypes);
        } catch (Exception e) {
            log.error("获取活动类型数据失败", e);
            return Result.error("获取活动类型数据失败");
        }
    }

    @GetMapping("/post-categories")
    public Result<List<Map<String, Object>>> getPostCategories() {
        try {
            // 提供论坛分类列表数据
            List<Map<String, Object>> categories = new ArrayList<>();

            // 添加更丰富的论坛分类
            categories.add(Map.of(
                    "id", 1,
                    "name", "学习交流",
                    "code", "STUDY",
                    "description", "学习经验交流、作业讨论、考试资料共享",
                    "icon", "study-icon"
            ));

            categories.add(Map.of(
                    "id", 2,
                    "name", "校园生活",
                    "code", "CAMPUS_LIFE",
                    "description", "校园生活分享、新生指南、生活技巧",
                    "icon", "life-icon"
            ));

            categories.add(Map.of(
                    "id", 3,
                    "name", "活动公告",
                    "code", "ACTIVITY",
                    "description", "各类校园活动公告、社团招新、比赛信息",
                    "icon", "activity-icon"
            ));

            categories.add(Map.of(
                    "id", 4,
                    "name", "资源共享",
                    "code", "RESOURCE",
                    "description", "学习资料、电子书籍、课件共享",
                    "icon", "resource-icon"
            ));

            categories.add(Map.of(
                    "id", 5,
                    "name", "求助互助",
                    "code", "HELP",
                    "description", "各类求助、互帮互助",
                    "icon", "help-icon"
            ));

            categories.add(Map.of(
                    "id", 6,
                    "name", "就业创业",
                    "code", "JOB",
                    "description", "实习、就业、创业相关信息",
                    "icon", "job-icon"
            ));

            return Result.success("获取成功", categories);
        } catch (Exception e) {
            log.error("获取帖子分类数据失败", e);
            return Result.error("获取帖子分类数据失败");
        }
    }
} 