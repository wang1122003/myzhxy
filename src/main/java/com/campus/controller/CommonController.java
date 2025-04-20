package com.campus.controller;

// import com.campus.dto.CommonStatusDTO; // 移除未使用
import com.campus.service.FileService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 公共API控制器
 * 处理不需要认证的公共接口
 */
@Slf4j
@RestController
@RequestMapping("/api/common")
public class CommonController {
    
    @Autowired
    private FileService fileService;

    /**
     * 获取系统状态
     * @return 系统状态信息
     */
    @GetMapping("/status")
    public Result getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("name", "智慧校园服务系统");
        status.put("version", "1.0.0");
        status.put("status", "running");
        status.put("time", System.currentTimeMillis());
        return Result.success("获取系统状态成功", status);
    }
    
    /**
     * 获取最近通知公告
     * @param limit 返回条数，默认5条
     * @return 通知公告列表
     */
    @GetMapping("/notices/recent")
    public Result getRecentNotices(@RequestParam(required = false, defaultValue = "5") Integer limit) {
        try {
            // 这里应该连接数据库获取通知公告
            // 为了演示，返回模拟数据
            List<Map<String, Object>> notices = new ArrayList<>();
            
            Map<String, Object> notice1 = new HashMap<>();
            notice1.put("id", 1);
            notice1.put("title", "关于2023-2024学年第二学期选课的通知");
            notice1.put("content", "各位同学：2023-2024学年第二学期选课将于2024年1月5日开始，请同学们提前做好准备。本学期选课采用分批次进行，具体安排如下：\n\n第一批次：2024年1月5日8:00-18:00，适用于大四年级学生\n第二批次：2024年1月6日8:00-18:00，适用于大三年级学生\n第三批次：2024年1月7日8:00-18:00，适用于大二年级学生\n第四批次：2024年1月8日8:00-18:00，适用于大一年级学生\n\n请学生们提前了解本专业培养方案，合理安排选课计划。选课系统登录方式不变，登录网址：http://xk.campus.edu.cn");
            notice1.put("createTime", new Date().getTime() - 24 * 60 * 60 * 1000); // 一天前
            notice1.put("author", "教务处");
            
            Map<String, Object> notice2 = new HashMap<>();
            notice2.put("id", 2);
            notice2.put("title", "寒假放假安排及注意事项");
            notice2.put("content", "根据学校工作安排，2023-2024学年寒假将于2024年1月22日开始，2024年2月25日结束。请各位师生做好以下工作：\n\n1. 妥善保管个人财物，离校前关闭宿舍电源、水源\n2. 积极做好学生宿舍卫生清理工作\n3. 假期留校学生须提前向辅导员报备\n4. 注意交通安全，遵守疫情防控要求\n\n祝全体师生寒假愉快！");
            notice2.put("createTime", new Date().getTime() - 5 * 24 * 60 * 60 * 1000); // 五天前
            notice2.put("author", "学生处");
            
            Map<String, Object> notice3 = new HashMap<>();
            notice3.put("id", 3);
            notice3.put("title", "2024年教师招聘公告");
            notice3.put("content", "为加强师资队伍建设，提升教育教学质量，我校现面向社会公开招聘教师若干名。招聘岗位包括计算机科学与技术、数学、英语、物理等学科教师。\n\n应聘条件：\n1. 具有博士学位，年龄一般在40周岁以下\n2. 具有扎实的专业基础和较强的教学能力\n3. 有海外知名高校学习或工作经历者优先\n\n应聘者请将个人简历、学历证书、科研成果等材料发送至hr@campus.edu.cn");
            notice3.put("createTime", new Date().getTime() - 10 * 24 * 60 * 60 * 1000); // 十天前
            notice3.put("author", "人事处");
            
            notices.add(notice1);
            notices.add(notice2);
            notices.add(notice3);
            
            // 只返回请求的条数
            if (notices.size() > limit) {
                notices = notices.subList(0, limit);
            }
            
            return Result.success(notices);
        } catch (Exception e) {
            log.error("获取最近通知公告失败", e);
            return Result.error("获取最近通知公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有通知类型
     *
     * @return 通知类型列表
     */
    @GetMapping("/notice-types")
    public Result getNoticeTypes() {
        try {
            // 实际应用中，这些类型可能来自数据库或枚举
            // 修改键名以匹配前端 ElOption 的期望 (typeCode, typeName)
            List<Map<String, Object>> noticeTypes = new ArrayList<>();
            noticeTypes.add(Map.of("typeCode", "1", "typeName", "系统通知", "description", "关于系统维护、升级等重要通知")); // id -> typeCode, name -> typeName
            noticeTypes.add(Map.of("typeCode", "2", "typeName", "教学通知", "description", "关于选课、考试、成绩等教学相关通知"));
            noticeTypes.add(Map.of("typeCode", "3", "typeName", "学工通知", "description", "关于奖助学金、评优、活动等学生工作通知"));
            noticeTypes.add(Map.of("typeCode", "4", "typeName", "生活通知", "description", "关于宿舍、食堂、水电等生活相关通知"));
            noticeTypes.add(Map.of("typeCode", "99", "typeName", "其他通知", "description", "未分类的其他通知"));

            // 确保 typeCode 是字符串或其他 Element Plus 支持的类型，这里使用字符串

            return Result.success("获取通知类型成功", noticeTypes);
        } catch (Exception e) {
            log.error("获取通知类型失败", e);
            return Result.error("获取通知类型失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过临时URL访问文件
     * @param path 文件路径
     * @param token 访问令牌
     * @param expire 过期时间
     * @param response HTTP响应
     */
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
            
            // 提取文件名
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            
            // 使用输出流下载文件
            OutputStream outputStream = response.getOutputStream();
            boolean success = fileService.downloadFile(path, outputStream);
            
            if (!success) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                outputStream.write("文件不存在或访问失败".getBytes(StandardCharsets.UTF_8));
            }
            
            outputStream.flush();
        } catch (IOException e) {
            log.error("文件访问失败", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("文件访问失败: " + e.getMessage());
            } catch (IOException ex) {
                log.error("设置错误响应失败", ex);
            }
        }
    }
    
    /**
     * 获取系统配置
     * @return 系统配置信息
     */
    @GetMapping("/config")
    public Result getSystemConfig() {
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
     * 获取学期列表
     *
     * @return 学期列表
     */
    @GetMapping("/terms")
    public Result getTerms() {
        try {
            // 在实际应用中，学期列表可能来自数据库或更复杂的配置
            // 这里提供一个简单的硬编码示例
            List<Map<String, String>> terms = new ArrayList<>();
            terms.add(Map.of("value", "2024-2025-1", "label", "2024-2025学年 第一学期"));
            terms.add(Map.of("value", "2023-2024-2", "label", "2023-2024学年 第二学期"));
            terms.add(Map.of("value", "2023-2024-1", "label", "2023-2024学年 第一学期"));
            terms.add(Map.of("value", "2022-2023-2", "label", "2022-2023学年 第二学期"));
            // 可以添加更多学期...

            return Result.success("获取学期列表成功", terms);
        } catch (Exception e) {
            log.error("获取学期列表失败", e);
            return Result.error("获取学期列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取时间段列表 (用于课表)
     *
     * @return 时间段列表
     */
    @GetMapping("/time-slots")
    public Result getTimeSlots() {
        try {
            List<Map<String, Object>> timeSlots = new ArrayList<>();
            // value 对应数据库存储， label 用于显示， slot 是节次
            timeSlots.add(Map.of("value", 1, "label", "第1节", "slot", 1, "startTime", "08:00", "endTime", "08:45"));
            timeSlots.add(Map.of("value", 2, "label", "第2节", "slot", 2, "startTime", "08:55", "endTime", "09:40"));
            timeSlots.add(Map.of("value", 3, "label", "第3节", "slot", 3, "startTime", "10:00", "endTime", "10:45"));
            timeSlots.add(Map.of("value", 4, "label", "第4节", "slot", 4, "startTime", "10:55", "endTime", "11:40"));
            timeSlots.add(Map.of("value", 5, "label", "第5节", "slot", 5, "startTime", "14:00", "endTime", "14:45"));
            timeSlots.add(Map.of("value", 6, "label", "第6节", "slot", 6, "startTime", "14:55", "endTime", "15:40"));
            timeSlots.add(Map.of("value", 7, "label", "第7节", "slot", 7, "startTime", "16:00", "endTime", "16:45"));
            timeSlots.add(Map.of("value", 8, "label", "第8节", "slot", 8, "startTime", "16:55", "endTime", "17:40"));
            timeSlots.add(Map.of("value", 9, "label", "第9节", "slot", 9, "startTime", "19:00", "endTime", "19:45"));
            timeSlots.add(Map.of("value", 10, "label", "第10节", "slot", 10, "startTime", "19:55", "endTime", "20:40"));

            return Result.success("获取时间段成功", timeSlots);
        } catch (Exception e) {
            log.error("获取时间段失败", e);
            return Result.error("获取时间段失败: " + e.getMessage());
        }
    }

    /**
     * 获取星期列表 (用于课表)
     *
     * @return 星期列表
     */
    @GetMapping("/weekdays")
    public Result getWeekdays() {
        try {
            List<Map<String, Object>> weekdays = new ArrayList<>();
            // value 对应数据库存储 (通常 1-7), label 用于显示
            weekdays.add(Map.of("value", 1, "label", "周一"));
            weekdays.add(Map.of("value", 2, "label", "周二"));
            weekdays.add(Map.of("value", 3, "label", "周三"));
            weekdays.add(Map.of("value", 4, "label", "周四"));
            weekdays.add(Map.of("value", 5, "label", "周五"));
            weekdays.add(Map.of("value", 6, "label", "周六"));
            weekdays.add(Map.of("value", 7, "label", "周日"));

            return Result.success("获取星期成功", weekdays);
        } catch (Exception e) {
            log.error("获取星期失败", e);
            return Result.error("获取星期失败: " + e.getMessage());
        }
    }
} 