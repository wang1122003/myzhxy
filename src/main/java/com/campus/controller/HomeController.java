package com.campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 * 处理系统根路径请求，实现页面跳转
 * 注意：访问控制已由 Spring Security 处理，此处仅负责路径映射和重定向。
 */
@Controller
public class HomeController {

    /**
     * 处理根路径请求，重定向到首页
     *
     * @return 重定向到首页
     */
    @GetMapping(value = {"/", "/campus", "/campus/"})
    public String home() {
        return "redirect:/index.html";
    }

    /**
     * 处理admin目录请求，重定向到admin首页
     * (权限检查已由 Spring Security 完成)
     *
     * @return 重定向到admin首页
     */
    @GetMapping(value = {"/admin", "/admin/", "/campus/admin", "/campus/admin/"})
    public String adminHome() {
        return "redirect:/admin/index.html";
    }

    /**
     * 处理teacher目录请求，重定向到teacher首页
     * (权限检查已由 Spring Security 完成)
     *
     * @return 重定向到teacher首页
     */
    @GetMapping(value = {"/teacher", "/teacher/", "/campus/teacher", "/campus/teacher/"})
    public String teacherHome() {
        return "redirect:/teacher/index.html";
    }

    /**
     * 处理student目录请求，重定向到student首页
     * (权限检查已由 Spring Security 完成)
     *
     * @return 重定向到student首页
     */
    @GetMapping(value = {"/student", "/student/", "/campus/student", "/campus/student/"})
    public String studentHome() {
        return "redirect:/student/index.html";
    }
} 