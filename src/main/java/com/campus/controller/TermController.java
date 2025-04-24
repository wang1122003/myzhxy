package com.campus.controller;

import com.campus.entity.Term;
import com.campus.service.TermService;
import com.campus.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学期管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/terms")
public class TermController {

    @Autowired
    private TermService termService;

    /**
     * 获取所有学期列表
     * @param sortByCreateDesc 是否按创建时间降序排序 (可选, 默认 false)
     * @return 学期列表
     */
    @GetMapping
    public Result<List<Term>> getAllTerms(
            @RequestParam(value = "sortByCreateDesc", required = false, defaultValue = "false") boolean sortByCreateDesc) {
        try {
            List<Term> terms = termService.getAllTerms(sortByCreateDesc);
            return Result.success("获取学期列表成功", terms);
        } catch (Exception e) {
            log.error("获取学期列表失败", e);
            return Result.error("获取学期列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前学期
     * @return 当前学期信息
     */
    @GetMapping("/current")
    public Result<Term> getCurrentTerm() {
        try {
            Term currentTerm = termService.getCurrentTerm();
            if (currentTerm != null) {
                return Result.success("获取当前学期成功", currentTerm);
            } else {
                return Result.success("未设置当前学期", null); // 或者返回错误，取决于业务需求
            }
        } catch (Exception e) {
            log.error("获取当前学期失败", e);
            return Result.error("获取当前学期失败: " + e.getMessage());
        }
    }

    // 未来可以添加 POST, PUT, DELETE 等接口用于管理学期
} 