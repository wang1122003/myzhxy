package com.campus.controller;

import com.campus.service.FileService;
import com.campus.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/api")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private FileService fileService;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileService.uploadImage(file);
            return Result.success("图片上传成功", filePath);
        } catch (Exception e) {
            logger.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传特定类型的图片
     *
     * @param file 图片文件
     * @param type 图片类型
     * @return 上传结果
     */
    @PostMapping("/upload/image/{type}")
    public Result<String> uploadImageByType(@RequestParam("file") MultipartFile file, @PathVariable("type") String type) {
        try {
            String filePath = fileService.uploadImage(file, type);
            return Result.success("图片上传成功", filePath);
        } catch (Exception e) {
            logger.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传活动海报
     *
     * @param file 海报文件
     * @return 上传结果
     */
    @PostMapping("/upload/activity/poster")
    public Result<String> uploadActivityPoster(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileService.uploadActivityPoster(file);
            return Result.success("活动海报上传成功", filePath);
        } catch (Exception e) {
            logger.error("活动海报上传失败", e);
            return Result.error("活动海报上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 上传结果
     */
    @PostMapping("/upload/avatar/{userId}")
    public Result<String> uploadUserAvatar(@RequestParam("file") MultipartFile file, @PathVariable("userId") Long userId) {
        try {
            String filePath = fileService.uploadUserAvatar(file, userId);
            return Result.success("用户头像上传成功", filePath);
        } catch (Exception e) {
            logger.error("用户头像上传失败", e);
            return Result.error("用户头像上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传帖子图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload/post/image")
    public Result<String> uploadPostImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileService.uploadPostImage(file);
            return Result.success("帖子图片上传成功", filePath);
        } catch (Exception e) {
            logger.error("帖子图片上传失败", e);
            return Result.error("帖子图片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传文档
     *
     * @param file 文档文件
     * @return 上传结果
     */
    @PostMapping("/upload/document")
    public Result<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileService.uploadDocument(file);
            return Result.success("文档上传成功", filePath);
        } catch (Exception e) {
            logger.error("文档上传失败", e);
            return Result.error("文档上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传课程材料
     *
     * @param file 课程材料文件
     * @param courseId 课程ID
     * @return 上传结果 (文件相对路径)
     */
    @PostMapping("/upload/course/material/{courseId}")
    public Result<String> uploadCourseMaterial(@RequestParam("file") MultipartFile file, @PathVariable("courseId") Long courseId) {
        try {
            String filePath = fileService.uploadCourseMaterial(file, courseId);
            return Result.success("课程材料上传成功", filePath);
        } catch (Exception e) {
            logger.error("课程材料上传失败", e);
            return Result.error("课程材料上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传通知附件
     *
     * @param file 附件文件
     * @return 上传结果 (包含 name 和 url 的 Map)
     */
    @PostMapping("/upload/notice/attachment")
    public Result<Map<String, String>> uploadNoticeAttachment(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> fileInfo = fileService.uploadNoticeAttachment(file);
            return Result.success("通知附件上传成功", fileInfo);
        } catch (IOException e) {
            logger.error("通知附件上传失败: IO Error", e);
            return Result.error("通知附件上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("通知附件上传失败: Invalid Argument - {}", e.getMessage());
            return Result.error("通知附件上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("通知附件上传失败: Unexpected Error", e);
            return Result.error("通知附件上传失败，请稍后重试");
        }
    }
    
    /**
     * 删除文件
     *
     * @param filePath 文件相对路径 (e.g., uploads/images/uuid.jpg)
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteFile(@RequestParam("filePath") String filePath) {
        try {
            boolean success = fileService.deleteFile(filePath);
            if (success) {
                return Result.success("文件删除成功", true);
            } else {
                // Consider differentiating between "not found" and "failed to delete"
                return Result.error("文件删除失败或文件不存在"); 
            }
        } catch (Exception e) {
            logger.error("删除文件时发生异常, path: {}", filePath, e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件信息
     *
     * @param filePath 文件路径
     * @return 文件信息
     */
    @GetMapping("/info")
    public Result<String> getFileInfo(@RequestParam("filePath") String filePath) {
        try {
            String fileInfo = fileService.getFileInfo(filePath);
            return Result.success("获取文件信息成功", fileInfo);
        } catch (Exception e) {
            logger.error("获取文件信息失败", e);
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件
     *
     * @param filePath 文件路径
     * @param response HTTP响应
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam("filePath") String filePath, HttpServletResponse response) {
        try {
            // 获取文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            
            // 使用输出流下载文件
            OutputStream outputStream = response.getOutputStream();
            boolean success = fileService.downloadFile(filePath, outputStream);
            
            if (!success) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                outputStream.write("文件不存在或下载失败".getBytes(StandardCharsets.UTF_8));
            }
            
            outputStream.flush();
        } catch (IOException e) {
            logger.error("文件下载失败", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getOutputStream().write(("文件下载失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                logger.error("设置错误响应失败", ex);
            }
        }
    }
    
    /**
     * 生成临时访问URL
     *
     * @param filePath 文件路径
     * @param expireTime 过期时间（毫秒）
     * @return 临时URL
     */
    @GetMapping("/temp-url")
    public Result<String> generateTempUrl(@RequestParam("filePath") String filePath, 
                                 @RequestParam(value = "expireTime", defaultValue = "3600000") long expireTime) {
        try {
            String tempUrl = fileService.generateTempUrl(filePath, expireTime);
            return Result.success("临时URL生成成功", tempUrl);
        } catch (Exception e) {
            logger.error("生成临时URL失败", e);
            return Result.error("生成临时URL失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表（目录结构）
     *
     * @param directory 请求的相对目录，默认为空（即根目录）
     * @return 目录下的文件和子目录列表
     */
    @GetMapping("/file/manager/list")
    public Result listDirectory(@RequestParam(value = "directory", defaultValue = "") String directory) {
        logger.info("Listing directory: '{}' relative to upload path: '{}'", directory, uploadPath);
        try {
            // -- 修复 Windows 路径问题 --
            Path baseUploadPath;
            // 检查 uploadPath 是否是绝对路径 (Windows or Unix-like)
            if (Paths.get(uploadPath).isAbsolute()) {
                baseUploadPath = Paths.get(uploadPath).normalize();
            } else {
                // 如果是相对路径，则相对于当前工作目录解析
                baseUploadPath = Paths.get(System.getProperty("user.dir"), uploadPath).normalize();
            }
            logger.debug("Resolved base upload path object: {}", baseUploadPath);

            // 确保基础上传目录存在
            if (!Files.exists(baseUploadPath)) {
                logger.warn("Base upload directory does not exist, attempting to create: {}", baseUploadPath);
                try {
                    Files.createDirectories(baseUploadPath);
                } catch (IOException ioException) {
                    logger.error("Failed to create base upload directory: {}", baseUploadPath, ioException);
                    return Result.error("基础上传目录不存在且无法创建");
                }
            }
            // -- 修复结束 --

            // 解析请求的相对目录路径，并清理可能存在的路径遍历字符
            String cleanDirectory = directory.replace("..", ""); // Basic sanitation
            Path targetDirectoryPath = baseUploadPath.resolve(cleanDirectory).normalize();
            logger.debug("Target directory path object: {}", targetDirectoryPath);

            // 安全检查：确保目标目录仍在基础上传路径之下
            if (!targetDirectoryPath.startsWith(baseUploadPath)) {
                logger.warn("Access denied: Attempt to access directory outside base upload path. Target: {}", targetDirectoryPath);
                return Result.error("无权访问该目录");
            }

            File targetDir = targetDirectoryPath.toFile();
            logger.debug("Target directory file object: {}, Exists: {}, IsDirectory: {}", targetDir, targetDir.exists(), targetDir.isDirectory());

            if (!targetDir.exists() || !targetDir.isDirectory()) {
                logger.warn("Directory not found or not a directory: {}", targetDirectoryPath);
                return Result.error("目录不存在或不是一个有效的目录");
            }

            // 获取文件和子目录列表
            List<Map<String, Object>> fileList;
            try (Stream<Path> stream = Files.list(targetDirectoryPath)) {
                fileList = stream.map(path -> {
                            File file = path.toFile();
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("name", file.getName());
                            // 生成相对于 baseUploadPath 的相对路径，并统一使用 / 作为分隔符
                            String relativePath = baseUploadPath.relativize(path).toString().replace('\\', '/');
                            fileInfo.put("path", relativePath);
                            fileInfo.put("isDirectory", file.isDirectory());
                            fileInfo.put("size", file.isDirectory() ? 0 : file.length());
                            fileInfo.put("lastModified", file.lastModified());
                            // 可以添加更多信息，如类型、权限等
                            return fileInfo;
                        })
                        .collect(Collectors.toList());
            }

            logger.info("Successfully listed {} items in directory: {}", fileList.size(), targetDirectoryPath);
            // 返回当前目录的相对路径和文件列表
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("currentPath", baseUploadPath.relativize(targetDirectoryPath).toString().replace('\\', '/'));
            responseData.put("files", fileList);

            return Result.success("获取目录结构成功", responseData);

        } catch (IOException e) {
            logger.error("获取目录 '{}' 结构失败: IO Error", directory, e);
            // 提供更具体的错误信息给前端可能没有必要，但日志需要详细
            return Result.error("获取目录结构失败，请检查服务器日志");
        } catch (InvalidPathException e) {
            logger.error("获取目录 '{}' 结构失败: Invalid Path Syntax for '{}' or '{}'", directory, uploadPath, directory, e);
            return Result.error("获取目录结构失败: 路径包含非法字符或格式错误");
        } catch (Exception e) {
            logger.error("获取目录 '{}' 结构失败: Unexpected Error", directory, e);
            return Result.error("获取目录结构失败，请联系管理员");
        }
    }

    /**
     * 批量删除文件
     *
     * @param filePaths 文件相对路径列表 (相对于 uploadPath)
     * @return 删除结果
     */
    @DeleteMapping("/manager/batch-delete")
    public Result batchDeleteFiles(@RequestBody List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return Result.error("未选择任何文件");
        }

        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        for (String filePath : filePaths) {
            boolean success = fileService.deleteFile(filePath);
            if (success) {
                successList.add(filePath);
            } else {
                failList.add(filePath);
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
     *
     * @return 统计信息
     */
    @GetMapping("/file/manager/stats")
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
            logger.error("获取文件统计信息失败", e);
            return Result.error("获取文件统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定目录下的所有文件（递归）
     *
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
     * 获取文件扩展名
     *
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