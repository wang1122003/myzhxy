package com.campus.controller;

// import com.campus.entity.User;
// import com.campus.entity.College;
// import com.campus.entity.Department;
// import com.campus.service.FileService; // Removed unused import
// import com.campus.service.MailService; // Removed MailService import
// import jakarta.servlet.http.HttpServletResponse; // Removed unused import

import com.campus.enums.Term;
import com.campus.utils.Result;
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

    // Removed unused fileService field
    // private final FileService fileService;

    // Constructor injection requires CommonService (which is commented out)
    // public CommonController(CommonService commonService, FileService fileService) {
    //     // this.commonService = commonService;
    //     this.fileService = fileService;
    // }

    // Manually Autowire FileService for now since constructor injection is broken
    // @Autowired // Removed as field is removed
    // public CommonController(FileService fileService) {
    //     this.fileService = fileService;
    //     // this.commonService = null; // Handle null commonService in methods below
    // }

    // Add default constructor if no other constructor is present and field is removed
    public CommonController() {
        // Default constructor
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
     * 通过临时URL访问文件 (Method commented out as the underlying download logic changed)
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

            // 使用输出流下载文件 (Old logic - needs rework)
            OutputStream outputStream = response.getOutputStream();
            // boolean success = fileService.downloadFile(path, outputStream); // This method is removed
            boolean success = false; // Placeholder - need to call new download mechanism
            log.warn("accessFileByTempUrl is using removed download logic and needs rework.");

            if (!success) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                outputStream.write("文件不存在或访问失败".getBytes(StandardCharsets.UTF_8));
            }

            outputStream.flush();
        } catch (IOException e) {
            log.error("Error accessing file by temp URL", e);
            try {
                if (!response.isCommitted()) {
                     response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                     response.getWriter().write("文件访问失败: " + e.getMessage());
                }
            } catch (IOException ex) {
                // Cannot set response, ignore
                log.error("Error setting error response for temp URL access", ex);
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
                .map(Term::toMap) // Convert enum to map
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

    // --- Added Stubs for Missing Common Lookups --- 

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
        // TODO: Implement actual data retrieval (e.g., from Enum or DB)
        return Result.success("获取成功 (Stub)", List.of(
                Map.of("id", 1, "name", "普通教室"),
                Map.of("id", 2, "name", "实验室"),
                Map.of("id", 3, "name", "会议室")
        ));
    }

    @GetMapping("/course-types")
    public Result<List<Map<String, Object>>> getCourseTypes() {
        // TODO: Implement actual data retrieval (e.g., from Enum or DB)
        return Result.success("获取成功 (Stub)", List.of(
                Map.of("id", 1, "name", "必修课"),
                Map.of("id", 2, "name", "选修课"),
                Map.of("id", 3, "name", "实验课")
        ));
    }

    @GetMapping("/activity-types")
    public Result<List<Map<String, Object>>> getActivityTypes() {
        // TODO: Implement actual data retrieval (e.g., from Enum or DB)
        return Result.success("获取成功 (Stub)", List.of(
                Map.of("id", 1, "name", "讲座"),
                Map.of("id", 2, "name", "竞赛"),
                Map.of("id", 3, "name", "社团活动")
        ));
    }

    @GetMapping("/post-categories")
    public Result<List<Map<String, Object>>> getPostCategories() {
        // Note: ForumController /categories returns types based on Post entity
        // This endpoint might be redundant or need different implementation
        // TODO: Implement actual data retrieval or align with ForumController
        // List<Map<String, Object>> categories = commonService.getForumCategories(); // Needs commonService
        return Result.success("获取成功 (Stub)", List.of(
                Map.of("id", 1, "name", "学习交流 (Stub)"),
                Map.of("id", 2, "name", "校园生活 (Stub)")
        ));
    }
} 