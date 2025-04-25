package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Classroom;
import com.campus.exception.CustomException;
import com.campus.service.ClassroomService;
import com.campus.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教室管理控制器
 */
@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {
    
    @Autowired
    private ClassroomService classroomService;

    /**
     * 分页并按条件获取教室列表
     *
     * @param page     当前页码, 默认为1
     * @param size     每页数量, 默认为10
     * @param keyword  关键词(教室编号/名称)
     * @param building 教学楼
     * @param status   状态 (0:禁用, 1:正常)
     * @return 分页教室列表
     */
    @GetMapping
    public Result<IPage<Classroom>> getClassroomsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer status) {
        try {
            IPage<Classroom> pageData = classroomService.getClassroomsByPage(page, size, keyword, building, status);

            Map<String, Object> result = new HashMap<>();
            result.put("total", pageData.getTotal());
            result.put("list", pageData.getRecords());
            result.put("pages", pageData.getPages());
            result.put("current", pageData.getCurrent());
            result.put("size", pageData.getSize());

            // 返回 IPage 对象，而不是 Map
            return Result.success(pageData);
        } catch (Exception e) {
            return Result.error("获取教室列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有教室
     * @return 教室列表
     */
    @GetMapping("/all")
    public Result<List<Classroom>> getAllClassrooms() {
        try {
            List<Classroom> classrooms = classroomService.getAllClassrooms();
            return Result.success(classrooms);
        } catch (Exception e) {
            return Result.error("获取教室列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取教室
     * @param id 教室ID
     * @return 教室详情
     */
    @GetMapping("/{id}")
    public Result<Classroom> getClassroomById(@PathVariable Long id) {
        try {
            Classroom classroom = classroomService.getClassroomById(id);
            if (classroom != null) {
                return Result.success(classroom);
            } else {
                return Result.error("教室不存在");
            }
        } catch (Exception e) {
            return Result.error("获取教室详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加教室
     *
     * @param classroom 教室信息
     * @return 添加结果
     */
    @PostMapping
    public Result<Void> addClassroom(@RequestBody Classroom classroom) {
        try {
            // 基本验证 (可以扩展)
            if (classroom.getName() == null || classroom.getName().isEmpty()) {
                return Result.error("教室名称不能为空");
            }
            boolean result = classroomService.addClassroom(classroom);
            // 返回 Result<Void>，成功时不带数据体
            return result ? Result.success("添加成功") : Result.error("添加失败，教室名称可能已存在");
        } catch (CustomException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("添加教室时发生未知错误");
        }
    }
    
    /**
     * 更新教室信息
     *
     * @param id        教室ID
     * @param classroom 教室信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateClassroom(@PathVariable Long id, @RequestBody Classroom classroom) {
        // 确保路径中的ID设置到对象上
        classroom.setId(id);
        try {
            // 基本验证
            if (classroom.getName() == null || classroom.getName().isEmpty()) {
                return Result.error("教室名称不能为空");
            }
            boolean result = classroomService.updateClassroom(classroom);
            return result ? Result.success("更新成功") : Result.error("更新失败，教室名称可能重复或教室不存在");
        } catch (CustomException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新教室时发生未知错误");
        }
    }
    
    /**
     * 删除教室
     * @param id 教室ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteClassroom(@PathVariable Long id) {
        try {
            boolean success = classroomService.deleteClassroom(id);
            if (success) {
                return Result.success("删除教室成功");
            } else {
                return Result.error("删除教室失败");
            }
        } catch (CustomException ce) {
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            return Result.error("删除教室失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除教室
     *
     * @param ids 教室ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteClassrooms(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请提供要删除的教室ID");
        }
        try {
            // 使用循环调用 deleteClassroom 替换 batchDeleteClassrooms
            int successCount = 0;
            int failCount = 0;
            for (Long id : ids) {
                try {
                    if (classroomService.deleteClassroom(id)) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }
            if (failCount > 0) {
                return Result.success(String.format("批量删除完成，%d个成功，%d个失败", successCount, failCount));
            } else {
                return Result.success("批量删除成功");
            }
        } catch (Exception e) {
            return Result.error("批量删除教室时发生未知错误");
        }
    }
    
    /**
     * 更新教室状态
     *
     * @param id     教室ID
     * @param status 状态值
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateClassroomStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (status == null) {
            return Result.error("状态值不能为空");
        }
        // 对单个更新也使用 batchUpdateStatus，传入包含单个ID的列表
        try {
            List<Long> idList = List.of(id);
            classroomService.batchUpdateStatus(idList, status);
            return Result.success("更新状态成功");
        } catch (Exception e) {
            return Result.error("更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新教室状态
     *
     * @param statusUpdateRequest 包含ids和status的对象
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateClassroomStatus(@RequestBody Map<String, Object> statusUpdateRequest) {
        @SuppressWarnings("unchecked") // 类型转换警告
        List<Long> ids = (List<Long>) statusUpdateRequest.get("ids");
        Integer status = (Integer) statusUpdateRequest.get("status");

        if (ids == null || ids.isEmpty() || status == null) {
            return Result.error("参数错误：缺少ids或status");
        }
        try {
            classroomService.batchUpdateStatus(ids, status);
            return Result.success("批量更新状态成功");
        } catch (Exception e) {
            return Result.error("批量更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用教室
     * @return 可用教室列表
     */
    @GetMapping("/available")
    public Result<List<Classroom>> getAvailableClassrooms() {
        try {
            List<Classroom> classrooms = classroomService.getAvailableClassrooms();
            return Result.success(classrooms);
        } catch (Exception e) {
            return Result.error("获取可用教室列表失败: " + e.getMessage());
        }
    }
}