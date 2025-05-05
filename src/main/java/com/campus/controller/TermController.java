package com.campus.controller;

import com.campus.entity.Term;
import com.campus.service.TermService;
import com.campus.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学期管理控制器
 */
@RestController
@RequestMapping("/api/term")
public class TermController {

    @Autowired
    private TermService termService;

    /**
     * 获取所有学期列表
     *
     * @return 学期列表
     */
    @GetMapping("/list")
    public Result<List<Term>> getAllTerms() {
        List<Term> termList = termService.getAllTerms();
        return Result.success(termList);
    }

    /**
     * 获取所有启用的学期列表
     *
     * @return 学期列表
     */
    @GetMapping("/active")
    public Result<List<Term>> getActiveTerms() {
        List<Term> termList = termService.getAllActiveTerms();
        return Result.success(termList);
    }

    /**
     * 获取当前学期
     *
     * @return 当前学期
     */
    @GetMapping("/current")
    public Result<Term> getCurrentTerm() {
        Term term = termService.getCurrentTerm();
        return Result.success(term);
    }

    /**
     * 根据ID获取学期
     *
     * @param id 学期ID
     * @return 学期对象
     */
    @GetMapping("/{id}")
    public Result<Term> getTermById(@PathVariable Long id) {
        Term term = termService.getTermById(id);
        return (term != null) ? Result.success(term) : Result.error("学期不存在");
    }

    /**
     * 根据学期代码获取学期
     *
     * @param code 学期代码
     * @return 学期对象
     */
    @GetMapping("/code/{code}")
    public Result<Term> getTermByCode(@PathVariable String code) {
        Term term = termService.getTermByCode(code);
        return (term != null) ? Result.success(term) : Result.error("学期不存在");
    }

    /**
     * 添加学期
     *
     * @param term 学期对象
     * @return 添加结果
     */
    @PostMapping
    public Result<Void> addTerm(@RequestBody Term term) {
        boolean success = termService.addTerm(term);
        return success ? Result.success() : Result.error("添加学期失败");
    }

    /**
     * 更新学期
     *
     * @param term 学期对象
     * @return 更新结果
     */
    @PutMapping
    public Result<Void> updateTerm(@RequestBody Term term) {
        boolean success = termService.updateTerm(term);
        return success ? Result.success() : Result.error("更新学期失败");
    }

    /**
     * 删除学期
     *
     * @param id 学期ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTerm(@PathVariable Long id) {
        boolean success = termService.deleteTerm(id);
        return success ? Result.success() : Result.error("删除学期失败");
    }

    /**
     * 设置当前学期
     *
     * @param id 学期ID
     * @return 设置结果
     */
    @PutMapping("/current/{id}")
    public Result<Void> setCurrentTerm(@PathVariable Long id) {
        boolean success = termService.setCurrentTerm(id);
        return success ? Result.success() : Result.error("设置当前学期失败");
    }

    /**
     * 分页查询学期
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getTermPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> pageData = termService.getTermPage(page, size);
        return Result.success(pageData);
    }

    /**
     * 获取特定学年的学期列表
     *
     * @param academicYear 学年，如 2023-2024
     * @return 学期列表
     */
    @GetMapping("/year/{academicYear}")
    public Result<List<Term>> getTermsByAcademicYear(@PathVariable String academicYear) {
        List<Term> termList = termService.getTermsByAcademicYear(academicYear);
        return Result.success(termList);
    }
} 