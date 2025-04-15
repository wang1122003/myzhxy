package com.campus.service;

import java.util.List;

import com.campus.entity.Backup;

/**
 * 数据备份与恢复服务接口
 */
public interface BackupService {

    /**
     * 创建数据库备份
     */
    Backup createBackup(String description, String backupType, Integer userId) throws Exception;
    
    /**
     * 从备份恢复数据库
     */
    boolean restoreFromBackup(Integer backupId) throws Exception;
    
    /**
     * 获取所有备份记录
     */
    List<Backup> getAllBackups();
    
    /**
     * 获取指定类型的备份记录
     */
    List<Backup> getBackupsByType(String backupType);
    
    /**
     * 根据ID获取备份记录
     */
    Backup getBackupById(Integer id);
    
    /**
     * 删除备份记录及文件
     */
    boolean deleteBackup(Integer id) throws Exception;
    
    /**
     * 更新备份描述
     */
    boolean updateBackupDescription(Integer id, String description);
} 