package com.campus.controller;

// import com.campus.entity.Term; // Missing
// import com.campus.service.TermService; // Missing
// import com.campus.utils.Result;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.List;

/**
 * 学期管理控制器
 * TODO: [学期功能] - Controller commented out as Term will be managed via config file, not DB entity.
 */
/*
@RestController
@RequestMapping("/api/terms")
public class TermController {

    @Autowired
    private TermService termService;

    @GetMapping
    public Result<List<Term>> getAllTerms(
            @RequestParam(value = "sortByCreateDesc", required = false, defaultValue = "false") boolean sortByCreateDesc) {
        try {
            List<Term> terms = termService.getAllTerms(sortByCreateDesc);
            return Result.success("获取学期列表成功", terms);
        } catch (Exception e) {
            return Result.error("获取学期列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/current")
    public Result<Term> getCurrentTerm() {
        try {
            Term currentTerm = termService.getCurrentTerm();
            if (currentTerm != null) {
                return Result.success("获取当前学期成功", currentTerm);
            } else {
                return Result.success("未设置当前学期", null);
            }
        } catch (Exception e) {
            return Result.error("获取当前学期失败: " + e.getMessage());
        }
    }

    // 未来可以添加 POST, PUT, DELETE 等接口用于管理学期
}
*/ 