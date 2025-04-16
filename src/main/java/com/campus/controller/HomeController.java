package com.campus.controller;

import com.campus.entity.User;
import com.campus.service.AuthService;
import com.campus.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 * 处理系统根路径请求，实现页面跳转
 */
@Controller
public class HomeController {

    @Autowired
    private AuthService authService;

    /**
     * 处理根路径请求，重定向到首页
     * @return 重定向到首页
     */
    @GetMapping(value = {"/", "/campus", "/campus/"})
    public String home() {
        return "redirect:/index.html";
    }
    
    /**
     * 处理admin目录请求，重定向到admin首页
     * 验证用户是否拥有管理员权限
     * @param request HTTP请求对象
     * @return 重定向到admin首页或登录页
     */
    @GetMapping(value = {"/admin", "/admin/", "/campus/admin", "/campus/admin/"})
    public String adminHome(HttpServletRequest request) {
        // 验证是否有管理员权限
        if (authService.isAuthenticated(request) && authService.isAdmin(request)) {
            return "redirect:/admin/index.html";
        }
        
        // 权限验证失败，重定向到登录页
        return "redirect:/index.html";
    }
    
    /**
     * 处理teacher目录请求，重定向到teacher首页
     * 验证用户是否拥有教师权限
     * @param request HTTP请求对象
     * @return 重定向到teacher首页或登录页
     */
    @GetMapping(value = {"/teacher", "/teacher/", "/campus/teacher", "/campus/teacher/"})
    public String teacherHome(HttpServletRequest request) {
        // 验证是否有教师权限
        if (authService.isAuthenticated(request) && (authService.isTeacher(request) || authService.isAdmin(request))) {
            return "redirect:/teacher/index.html";
        }
        
        // 权限验证失败，重定向到登录页
        return "redirect:/index.html";
    }
    
    /**
     * 处理student目录请求，重定向到student首页
     * 验证用户是否拥有学生权限
     * @param request HTTP请求对象
     * @return 重定向到student首页或登录页
     */
    @GetMapping(value = {"/student", "/student/", "/campus/student", "/campus/student/"})
    public String studentHome(HttpServletRequest request) {
        // 验证是否有学生权限
        if (authService.isAuthenticated(request) && (authService.isStudent(request) || authService.isAdmin(request))) {
            return "redirect:/student/index.html";
        }
        
        // 权限验证失败，重定向到登录页
        return "redirect:/index.html";
    }
} 