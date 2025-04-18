package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.Classroom;
import com.campus.exception.CustomException;
import com.campus.service.ClassroomService;
import com.campus.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教室管理控制器
 */
@Slf4j
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
    public Result getClassroomsByPage(
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

            return Result.success(result);
        } catch (Exception e) {
            log.error("分页获取教室列表失败", e);
            return Result.error("获取教室列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有教室
     * @return 教室列表
     */
    @GetMapping("/all")
    public Result getAllClassrooms() {
        try {
            List<Classroom> classrooms = classroomService.getAllClassrooms();
            return Result.success(classrooms);
        } catch (Exception e) {
            log.error("获取教室列表失败", e);
            return Result.error("获取教室列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取教室
     * @param id 教室ID
     * @return 教室详情
     */
    @GetMapping("/{id}")
    public Result getClassroomById(@PathVariable Long id) {
        try {
            Classroom classroom = classroomService.getClassroomById(id);
            if (classroom != null) {
                return Result.success(classroom);
            } else {
                return Result.error("教室不存在");
            }
        } catch (Exception e) {
            log.error("获取教室详情失败", e);
            return Result.error("获取教室详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加教室
     * @param classroom 教室信息
     * @return 添加结果
     */
    @PostMapping
    public Result addClassroom(@RequestBody Classroom classroom) {
        try {
            // 参数验证
            if (classroom.getRoomNo() == null || classroom.getRoomNo().trim().isEmpty()) {
                return Result.error("教室编号不能为空");
            }
            
            if (classroom.getRoomName() == null || classroom.getRoomName().trim().isEmpty()) {
                return Result.error("教室名称不能为空");
            }
            
            // 检查是否已存在相同编号的教室
            Classroom existingClassroom = classroomService.getClassroomByRoomNo(classroom.getRoomNo());
            if (existingClassroom != null) {
                return Result.error("教室编号已存在");
            }
            
            boolean success = classroomService.addClassroom(classroom);
            if (success) {
                return Result.success("添加教室成功");
            } else {
                return Result.error("添加教室失败");
            }
        } catch (Exception e) {
            log.error("添加教室失败", e);
            return Result.error("添加教室失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新教室
     * @param id 教室ID
     * @param classroom 教室信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result updateClassroom(@PathVariable Long id, @RequestBody Classroom classroom) {
        try {
            // 参数验证
            if (classroom.getRoomNo() == null || classroom.getRoomNo().trim().isEmpty()) {
                return Result.error("教室编号不能为空");
            }
            
            if (classroom.getRoomName() == null || classroom.getRoomName().trim().isEmpty()) {
                return Result.error("教室名称不能为空");
            }
            
            // 检查是否已存在相同编号的教室（排除自身）
            Classroom existingClassroom = classroomService.getClassroomByRoomNo(classroom.getRoomNo());
            if (existingClassroom != null && !existingClassroom.getId().equals(id)) {
                return Result.error("教室编号已存在");
            }
            
            // 设置ID
            classroom.setId(id);
            
            boolean success = classroomService.updateClassroom(classroom);
            if (success) {
                return Result.success("更新教室成功");
            } else {
                return Result.error("更新教室失败");
            }
        } catch (Exception e) {
            log.error("更新教室失败", e);
            return Result.error("更新教室失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除教室
     * @param id 教室ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteClassroom(@PathVariable Long id) {
        try {
            boolean success = classroomService.deleteClassroom(id);
            if (success) {
                return Result.success("删除教室成功");
            } else {
                return Result.error("删除教室失败");
            }
        } catch (CustomException ce) {
            log.warn("删除教室失败 (ID: {}): {}", id, ce.getMessage());
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            log.error("删除教室失败 (ID: {})", id, e);
            return Result.error("删除教室失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除教室
     * @param ids 教室ID数组
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result batchDeleteClassrooms(@RequestBody Long[] ids) {
        try {
            if (ids == null || ids.length == 0) {
                return Result.error("未选择要删除的教室");
            }
            boolean success = classroomService.batchDeleteClassrooms(ids);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (CustomException ce) {
            log.warn("批量删除教室失败: {}", ce.getMessage());
            return Result.error(ce.getMessage());
        } catch (Exception e) {
            log.error("批量删除教室失败", e);
            return Result.error("批量删除教室失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新教室状态
     * @param id 教室ID
     * @param status 状态值
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    public Result updateClassroomStatus(@PathVariable Long id, @PathVariable Integer status) {
        try {
            if (status != 0 && status != 1) {
                return Result.error("状态值无效");
            }
            
            boolean success = classroomService.updateClassroomStatus(id, status);
            if (success) {
                return Result.success("更新状态成功");
            } else {
                return Result.error("更新状态失败");
            }
        } catch (Exception e) {
            log.error("更新教室状态失败", e);
            return Result.error("更新教室状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取可用教室
     * @return 可用教室列表
     */
    @GetMapping("/available")
    public Result getAvailableClassrooms() {
        try {
            List<Classroom> classrooms = classroomService.getAvailableClassrooms();
            return Result.success(classrooms);
        } catch (Exception e) {
            log.error("获取可用教室列表失败", e);
            return Result.error("获取可用教室列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据教学楼查询教室
     * @param building 教学楼
     * @return 教室列表
     */
    @GetMapping("/building/{building}")
    public Result getClassroomsByBuilding(@PathVariable String building) {
        try {
            List<Classroom> classrooms = classroomService.getClassroomsByBuilding(building);
            return Result.success(classrooms);
        } catch (Exception e) {
            log.error("根据教学楼查询教室失败", e);
            return Result.error("根据教学楼查询教室失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据教室类型查询教室
     * @param roomType 教室类型
     * @return 教室列表
     */
    @GetMapping("/type/{roomType}")
    public Result getClassroomsByRoomType(@PathVariable Integer roomType) {
        try {
            List<Classroom> classrooms = classroomService.getClassroomsByRoomType(roomType);
            return Result.success(classrooms);
        } catch (Exception e) {
            log.error("根据教室类型查询教室失败", e);
            return Result.error("根据教室类型查询教室失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据容量范围查询教室
     * @param minCapacity 最小容量
     * @param maxCapacity 最大容量
     * @return 教室列表
     */
    @GetMapping("/capacity")
    public Result getClassroomsByCapacityRange(
            @RequestParam(required = false, defaultValue = "0") Integer minCapacity,
            @RequestParam(required = false, defaultValue = "1000") Integer maxCapacity) {
        try {
            List<Classroom> classrooms = classroomService.getClassroomsByCapacityRange(minCapacity, maxCapacity);
            return Result.success(classrooms);
        } catch (Exception e) {
            log.error("根据容量范围查询教室失败", e);
            return Result.error("根据容量范围查询教室失败: " + e.getMessage());
        }
    }
}