package com.campus.controller;

import com.campus.entity.Backup;
import com.campus.service.BackupService;
import com.campus.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据库备份与恢复控制器
 */
@RestController
@RequestMapping("/api/backup")
public class BackupController {
    
    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);
    
    @Autowired
    private BackupService backupService;
    
    /**
     * 创建数据库备份
     * @param description 备份描述
     * @param backupType 备份类型
     * @return 备份结果
     */
    @PostMapping("/create")
    public Result<Backup> createBackup(
            @RequestParam("description") String description,
            @RequestParam("backupType") String backupType,
            @RequestParam("userId") Integer userId) {
        try {
            Backup backup = backupService.createBackup(description, backupType, userId);
            return Result.success(backup);
        } catch (Exception e) {
            logger.error("创建数据库备份失败", e);
            return Result.error("创建数据库备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 从备份恢复数据库
     * @param backupId 备份ID
     * @return 恢复结果
     */
    @PostMapping("/restore/{backupId}")
    public Result<Boolean> restoreFromBackup(@PathVariable("backupId") Integer backupId) {
        try {
            boolean success = backupService.restoreFromBackup(backupId);
            if (success) {
                return Result.success("数据库恢复成功", Boolean.TRUE);
            } else {
                return Result.error("数据库恢复失败");
            }
        } catch (Exception e) {
            logger.error("数据库恢复失败", e);
            return Result.error("数据库恢复失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有备份记录
     * @return 备份记录列表
     */
    @GetMapping("/list")
    public Result<List<Backup>> getAllBackups() {
        try {
            List<Backup> backups = backupService.getAllBackups();
            return Result.success(backups);
        } catch (Exception e) {
            logger.error("获取备份记录失败", e);
            return Result.error("获取备份记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定类型的备份记录
     * @param backupType 备份类型
     * @return 备份记录列表
     */
    @GetMapping("/list/{backupType}")
    public Result<List<Backup>> getBackupsByType(@PathVariable("backupType") String backupType) {
        try {
            List<Backup> backups = backupService.getBackupsByType(backupType);
            return Result.success(backups);
        } catch (Exception e) {
            logger.error("获取备份记录失败", e);
            return Result.error("获取备份记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取备份详情
     * @param backupId 备份ID
     * @return 备份记录
     */
    @GetMapping("/{backupId}")
    public Result<Backup> getBackupById(@PathVariable("backupId") Integer backupId) {
        try {
            Backup backup = backupService.getBackupById(backupId);
            if (backup != null) {
                return Result.success(backup);
            } else {
                return Result.error("找不到指定的备份记录");
            }
        } catch (Exception e) {
            logger.error("获取备份记录失败", e);
            return Result.error("获取备份记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除备份记录及文件
     * @param backupId 备份ID
     * @return 删除结果
     */
    @DeleteMapping("/{backupId}")
    public Result<Boolean> deleteBackup(@PathVariable("backupId") Integer backupId) {
        try {
            boolean success = backupService.deleteBackup(backupId);
            if (success) {
                return Result.success("删除备份成功", Boolean.TRUE);
            } else {
                return Result.error("删除备份失败");
            }
        } catch (Exception e) {
            logger.error("删除备份失败", e);
            return Result.error("删除备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新备份描述
     * @param backupId 备份ID
     * @param description 备份描述
     * @return 更新结果
     */
    @PutMapping("/{backupId}/description")
    public Result<Boolean> updateBackupDescription(
            @PathVariable("backupId") Integer backupId,
            @RequestParam("description") String description) {
        try {
            boolean success = backupService.updateBackupDescription(backupId, description);
            if (success) {
                return Result.success("更新备份描述成功", Boolean.TRUE);
            } else {
                return Result.error("更新备份描述失败");
            }
        } catch (Exception e) {
            logger.error("更新备份描述失败", e);
            return Result.error("更新备份描述失败: " + e.getMessage());
        }
    }
}