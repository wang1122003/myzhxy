package com.campus.service.impl;

import com.campus.dao.BackupDao;
import com.campus.entity.Backup;
import com.campus.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class BackupServiceImpl implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupServiceImpl.class);

    @Autowired
    private BackupDao backupDao;

    @Value("${mysql.bin.path}")
    private String mysqlBinPath;

    @Value("${mysql.username}")
    private String mysqlUsername;

    @Value("${mysql.password}")
    private String mysqlPassword;

    @Value("${mysql.database}")
    private String mysqlDatabase;

    @Value("${backup.path}")
    private String backupPath;

    @Override
    public Backup createBackup(String description, String backupType, Integer userId) throws Exception {
        try {
            // 创建备份目录
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());
            String fileName = mysqlDatabase + "_" + timestamp + ".sql";
            String filePath = backupPath + File.separator + fileName;

            // 构建备份命令
            String command;
            if (mysqlPassword != null && !mysqlPassword.isEmpty()) {
                command = mysqlBinPath + File.separator + "mysqldump" +
                        " -u" + mysqlUsername +
                        " -p" + mysqlPassword +
                        " " + mysqlDatabase +
                        " > " + filePath;
            } else {
                command = mysqlBinPath + File.separator + "mysqldump" +
                        " -u" + mysqlUsername +
                        " " + mysqlDatabase +
                        " > " + filePath;
            }

            logger.info("执行数据库备份命令...");
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});
            int exitCode = process.waitFor();

            Backup backup = new Backup();
            backup.setFileName(fileName);
            backup.setFilePath(filePath);
            backup.setDescription(description);
            backup.setBackupTime(new Date());
            backup.setCreatedBy(userId);
            backup.setBackupType(backupType);

            if (exitCode == 0) {
                File backupFile = new File(filePath);
                backup.setFileSize(backupFile.length());
                backup.setStatus("成功");
                logger.info("数据库备份成功: {}", filePath);
            } else {
                backup.setFileSize(0L);
                backup.setStatus("失败");
                logger.error("数据库备份失败，退出代码: {}", exitCode);
            }

            backupDao.createBackup(backup);
            return backup;
        } catch (Exception e) {
            logger.error("创建备份时出错: ", e);
            throw e;
        }
    }

    @Override
    public boolean restoreFromBackup(Integer backupId) throws Exception {
        Backup backup = backupDao.getBackupById(backupId);
        if (backup == null) {
            logger.error("找不到ID为{}的备份记录", backupId);
            return false;
        }

        File backupFile = new File(backup.getFilePath());
        if (!backupFile.exists()) {
            logger.error("备份文件不存在: {}", backup.getFilePath());
            return false;
        }

        try {
            // 构建恢复命令
            String command;
            if (mysqlPassword != null && !mysqlPassword.isEmpty()) {
                command = mysqlBinPath + File.separator + "mysql" +
                        " -u" + mysqlUsername +
                        " -p" + mysqlPassword +
                        " " + mysqlDatabase +
                        " < " + backup.getFilePath();
            } else {
                command = mysqlBinPath + File.separator + "mysql" +
                        " -u" + mysqlUsername +
                        " " + mysqlDatabase +
                        " < " + backup.getFilePath();
            }

            logger.info("执行数据库恢复命令...");
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("从备份{}恢复数据库成功", backup.getFileName());
                return true;
            } else {
                logger.error("从备份{}恢复数据库失败，退出代码: {}", backup.getFileName(), exitCode);
                return false;
            }
        } catch (Exception e) {
            logger.error("从备份{}恢复时出错: ", backup.getFileName(), e);
            throw e;
        }
    }

    @Override
    public List<Backup> getAllBackups() {
        return backupDao.getAllBackups();
    }

    @Override
    public List<Backup> getBackupsByType(String backupType) {
        return backupDao.getBackupsByType(backupType);
    }

    @Override
    public Backup getBackupById(Integer id) {
        return backupDao.getBackupById(id);
    }

    @Override
    public boolean deleteBackup(Integer id) throws Exception {
        Backup backup = backupDao.getBackupById(id);
        if (backup == null) {
            logger.error("找不到ID为{}的备份记录", id);
            return false;
        }

        try {
            // 删除备份文件
            File backupFile = new File(backup.getFilePath());
            if (backupFile.exists()) {
                Files.delete(Paths.get(backup.getFilePath()));
            }

            // 删除数据库记录
            backupDao.deleteBackup(id);
            logger.info("成功删除备份记录和文件: {}", backup.getFileName());
            return true;
        } catch (IOException e) {
            logger.error("删除备份文件时出错: ", e);
            throw e;
        }
    }

    @Override
    public boolean updateBackupDescription(Integer id, String description) {
        int result = backupDao.updateBackupDescription(id, description);
        return result > 0;
    }
} 