package com.campus.dao;

import org.apache.ibatis.annotations.*;

import com.campus.entity.Backup;

import java.util.List;

/**
 * 数据备份与恢复DAO接口
 */
public interface BackupDao {
    
    @Insert("INSERT INTO backup (file_name, file_path, description, backup_time, created_by, file_size, backup_type, status) " +
            "VALUES (#{fileName}, #{filePath}, #{description}, #{backupTime}, #{createdBy}, #{fileSize}, #{backupType}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createBackup(Backup backup);
    
    @Select("SELECT * FROM backup WHERE id = #{id}")
    Backup getBackupById(Integer id);
    
    @Select("SELECT * FROM backup ORDER BY backup_time DESC")
    List<Backup> getAllBackups();
    
    @Select("SELECT * FROM backup WHERE backup_type = #{backupType} ORDER BY backup_time DESC")
    List<Backup> getBackupsByType(String backupType);
    
    @Delete("DELETE FROM backup WHERE id = #{id}")
    int deleteBackup(Integer id);
    
    @Update("UPDATE backup SET description = #{description} WHERE id = #{id}")
    int updateBackupDescription(@Param("id") Integer id, @Param("description") String description);
    
    @Update("UPDATE backup SET status = #{status} WHERE id = #{id}")
    int updateBackupStatus(@Param("id") Integer id, @Param("status") String status);
} 