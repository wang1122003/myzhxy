package com.campus.controller;

import com.campus.service.FileService;
import com.campus.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件管理控制器
 * 用于管理已上传的文件，提供目录浏览和批量操作功能
 */
@Slf4j
@RestController
@RequestMapping("/api/file-manager")
public class FileManagerController {

    @Autowired
    private FileService fileService;
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    /**
     * 获取目录结构
     * @param directory 目录路径，相对于上传根目录
     * @return 目录内容
     */
    @GetMapping("/list")
    public Result listDirectory(@RequestParam(value = "directory", defaultValue = "") String directory) {
        try {
            // 安全检查：防止目录遍历攻击
            if (directory.contains("..") || directory.startsWith("/") || directory.contains("\\")) {
                return Result.error("无效的目录路径");
            }
            
            // 构建完整路径
            Path dirPath = Paths.get(uploadPath, directory);
            File dir = dirPath.toFile();
            
            if (!dir.exists() || !dir.isDirectory()) {
                return Result.error("目录不存在");
            }
            
            List<Map<String, Object>> files = new ArrayList<>();
            File[] fileList = dir.listFiles();
            
            if (fileList != null) {
                for (File file : fileList) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("name", file.getName());
                    fileInfo.put("isDirectory", file.isDirectory());
                    fileInfo.put("size", file.length());
                    fileInfo.put("lastModified", file.lastModified());
                    fileInfo.put("path", getRelativePath(file));
                    
                    if (!file.isDirectory()) {
                        // 构建文件ID
                        String relativePath = getRelativePath(file);
                        fileInfo.put("fileId", relativePath);
                    }
                    
                    files.add(fileInfo);
                }
            }
            
            // 先排序目录，再排序文件
            files.sort((a, b) -> {
                boolean aIsDir = (boolean) a.get("isDirectory");
                boolean bIsDir = (boolean) b.get("isDirectory");
                
                if (aIsDir && !bIsDir) {
                    return -1;
                } else if (!aIsDir && bIsDir) {
                    return 1;
                } else {
                    return ((String) a.get("name")).compareTo((String) b.get("name"));
                }
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("currentPath", directory);
            result.put("files", files);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取目录结构失败", e);
            return Result.error("获取目录结构失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除文件
     * @param fileIds 文件ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch-delete")
    public Result batchDeleteFiles(@RequestBody List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Result.error("未选择任何文件");
        }
        
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();
        
        for (String fileId : fileIds) {
            boolean success = fileService.deleteFile(fileId);
            if (success) {
                successList.add(fileId);
            } else {
                failList.add(fileId);
            }
        }
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", successList);
        resultMap.put("fail", failList);
        
        if (failList.isEmpty()) {
            return Result.success("所有文件删除成功", resultMap);
        } else if (successList.isEmpty()) {
            return Result.error("所有文件删除失败");
        } else {
            return Result.success("部分文件删除成功", resultMap);
        }
    }
    
    /**
     * 获取文件统计信息
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Result getFileStats() {
        try {
            File baseDir = new File(uploadPath);
            if (!baseDir.exists()) {
                return Result.error("文件存储目录不存在");
            }
            
            // 统计不同类型文件数量和大小
            Map<String, Long> countByType = new HashMap<>();
            Map<String, Long> sizeByType = new HashMap<>();
            long totalSize = 0;
            int totalCount = 0;
            
            // 递归统计所有文件
            List<File> allFiles = getAllFiles(baseDir);
            for (File file : allFiles) {
                if (file.isFile()) {
                    totalCount++;
                    totalSize += file.length();
                    
                    String extension = getFileExtension(file.getName()).toLowerCase();
                    if (extension.startsWith(".")) {
                        extension = extension.substring(1);
                    }
                    
                    // 更新计数
                    countByType.put(extension, countByType.getOrDefault(extension, 0L) + 1);
                    sizeByType.put(extension, sizeByType.getOrDefault(extension, 0L) + file.length());
                }
            }
            
            // 格式化结果
            List<Map<String, Object>> typeStats = new ArrayList<>();
            for (String type : countByType.keySet()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("type", type);
                stat.put("count", countByType.get(type));
                stat.put("size", sizeByType.get(type));
                typeStats.add(stat);
            }
            
            // 按文件数量降序排序
            typeStats.sort((a, b) -> ((Long) b.get("count")).compareTo((Long) a.get("count")));
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("totalSize", totalSize);
            result.put("typeStats", typeStats);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取文件统计信息失败", e);
            return Result.error("获取文件统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定目录下的所有文件（递归）
     * @param directory 目录
     * @return 文件列表
     */
    private List<File> getAllFiles(File directory) {
        List<File> files = new ArrayList<>();
        File[] fileList = directory.listFiles();
        
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    files.addAll(getAllFiles(file));
                } else {
                    files.add(file);
                }
            }
        }
        
        return files;
    }
    
    /**
     * 获取文件相对于上传根目录的路径
     * @param file 文件
     * @return 相对路径
     */
    private String getRelativePath(File file) {
        Path basePath = Paths.get(uploadPath);
        Path filePath = file.toPath();
        return basePath.relativize(filePath).toString().replace("\\", "/");
    }
    
    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
} 