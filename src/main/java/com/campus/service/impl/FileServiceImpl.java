package com.campus.service.impl;

import com.campus.config.StorageProperties;
import com.campus.service.FileService;
import com.campus.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 文件服务实现类
 */
@Service
public class FileServiceImpl implements FileService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // URL 前缀，应与 Spring MVC 资源映射路径一致
    private static final String URL_PREFIX = "/uploads"; // 保持和 application.properties 及 mvc 配置一致

    // 注入 StorageProperties
    private final StorageProperties storageProperties;

    @Autowired // 使用构造函数注入
    public FileServiceImpl(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    private String getBasePath() {
        String location = storageProperties.getLocation();
        if (location == null || location.trim().isEmpty()) {
            System.out.println("文件上传基础路径未配置 (storageProperties.location is empty/null). 请检查 application.properties 中的 'file.upload.path'");
            // 抛出异常或者返回一个默认值（不推荐）
            throw new IllegalStateException("文件上传基础路径未配置。");
        }
        return location.trim();
    }
    
    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        String targetDir = Paths.get(getBasePath(), "images").toString();
        // uploadFile 返回完整路径
        String fullPath = FileUtils.uploadFile(file, targetDir);
        // 返回相对于 Web 访问的 URL
        return convertFullPathToUrl(fullPath);
    }
    
    @Override
    public String uploadImage(MultipartFile file, String type) throws IOException {
        validateImageFile(file);
        String baseDir = getBasePath();
        String subDir;
        
        switch (type.toLowerCase()) {
            case "avatar":
                subDir = "avatars";
                break;
            case "poster":
                subDir = "posters";
                break;
            case "post":
                subDir = "posts";
                break;
            default:
                // 对于未知类型，放入 images 下的子目录
                subDir = Paths.get("images", type).toString();
        }
        String targetDir = Paths.get(baseDir, subDir).toString();
        // FileUtils.uploadFile 内部会创建目录，移除外部调用
        // FileUtils.createDirectoryIfNotExists(directory);
        String fullPath = FileUtils.uploadFile(file, targetDir);
        return convertFullPathToUrl(fullPath);
    }
    
    @Override
    public String uploadActivityPoster(MultipartFile file) throws IOException {
        validateImageFile(file);
        String targetDir = Paths.get(getBasePath(), "posters").toString();
        String fullPath = FileUtils.uploadFile(file, targetDir);
        return convertFullPathToUrl(fullPath);
    }
    
    @Override
    public String uploadUserAvatar(MultipartFile file, Long userId) throws IOException {
        validateImageFile(file);
        String targetDir = Paths.get(getBasePath(), "avatars", String.valueOf(userId)).toString();
        // FileUtils.uploadFile 内部会创建目录，移除外部调用
        // FileUtils.createDirectoryIfNotExists(userAvatarDir);
        String fullPath = FileUtils.uploadFile(file, targetDir);
        return convertFullPathToUrl(fullPath);
    }
    
    @Override
    public String uploadPostImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        String targetDir = Paths.get(getBasePath(), "posts").toString();
        String fullPath = FileUtils.uploadFile(file, targetDir);
        return convertFullPathToUrl(fullPath);
    }
    
    @Override
    public String uploadDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        if (!FileUtils.isDocument(file.getOriginalFilename())) {
            throw new IllegalArgumentException("请上传合法的文档文件");
        }
        String targetDir = Paths.get(getBasePath(), "documents").toString();
        String fullPath = FileUtils.uploadFile(file, targetDir);
        // 通常文档也需要返回 URL
        return convertFullPathToUrl(fullPath);
    }
    
    @Override
    public String uploadCourseMaterial(MultipartFile file, Long courseId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        String targetDir = Paths.get(getBasePath(), "courses", String.valueOf(courseId)).toString();
        // FileUtils.uploadFile 内部会创建目录，移除外部调用
        // FileUtils.createDirectoryIfNotExists(courseMaterialDir);
        String fullPath = FileUtils.uploadFile(file, targetDir);
        // 返回 URL 供访问
        return convertFullPathToUrl(fullPath);
    }

    @Override
    public Map<String, String> uploadNoticeAttachment(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传附件不能为空");
        }
        // No specific validation for generic attachments for now

        String targetDir = Paths.get(getBasePath(), "notices").toString();
        String fullPath = FileUtils.uploadFile(file, targetDir);

        // Construct the result map
        Map<String, String> result = new HashMap<>();
        result.put("name", file.getOriginalFilename());
        result.put("url", convertFullPathToUrl(fullPath)); // 使用辅助方法转换 URL
        result.put("physicalPath", fullPath); // 可以选择性返回物理路径

        return result;
    }
    
    @Override
    public boolean deleteFile(String relativeOrFullPath) {
        if (relativeOrFullPath == null || relativeOrFullPath.isEmpty()) {
            System.out.println("文件路径不能为空");
            return false;
        }

        String fullPath;
        try {
             // 尝试判断传入的是相对路径还是绝对路径
            Path path = Paths.get(relativeOrFullPath);

            if (path.isAbsolute()) {
                 // 如果已经是绝对路径，直接使用，但验证它是否在 basePath 内
                 fullPath = relativeOrFullPath;
                 Path basePathObj = Paths.get(getBasePath()).normalize();
                 if (!path.normalize().startsWith(basePathObj)) {
                     System.out.println("尝试删除基础上传目录之外的文件: " + fullPath);
                    return false;
                }
            } else {
                 // 如果是相对路径，则基于 basePath 构建完整路径
                 fullPath = Paths.get(getBasePath(), relativeOrFullPath).toString();
            }
        } catch (Exception e) {
            System.out.println("解析删除文件路径时出错: " + relativeOrFullPath + ", " + e.getMessage());
            return false;
        }

        // 使用 FileUtils.deleteFile，它接收完整路径
        return FileUtils.deleteFile(fullPath);
    }

    @Override
    public boolean deleteAll() {
        String baseDir = getBasePath(); // 获取基础目录
        Path directoryPath = Paths.get(baseDir);

        if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            System.out.println("上传目录不存在，无需删除: " + baseDir);
                return true;
            }

        System.out.println("开始删除目录及其所有内容: " + baseDir);
        try (Stream<Path> walk = Files.walk(directoryPath)) {
            walk.sorted(java.util.Comparator.reverseOrder()) // 逆序删除，先删文件再删目录
                .map(Path::toFile)
                // .peek(System.out::println) // for debugging
                .forEach(file -> {
                    // 不删除根目录本身，比较标准化路径
                    if (!file.getAbsoluteFile().toPath().normalize().equals(directoryPath.normalize())) {
                        if (!file.delete()) {
                            System.out.println("无法删除文件或目录: " + file.getAbsolutePath());
                             // 可以选择抛出异常中断操作或继续
            } else {
                            // System.out.println("已删除: " + file.getAbsolutePath()); // Debug级别日志可以取消注释
                        }
                    }
                });

            System.out.println("成功删除上传目录内容: " + baseDir);
             // 如果需要，重新创建根目录 (通常不需要，除非上面的删除逻辑会删除根目录)
             // Files.createDirectories(directoryPath);
            return true;
        } catch (IOException e) {
            System.out.println("删除目录内容时发生错误: " + baseDir + ", " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getFileInfo(String relativeOrFullPath) {
        if (relativeOrFullPath == null || relativeOrFullPath.isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        // 构建完整物理路径
        String fullPath;
         try {
             Path pathObj = Paths.get(relativeOrFullPath);
             if (pathObj.isAbsolute()) {
                  fullPath = relativeOrFullPath;
                  // 可选：验证是否在 basePath 内
                  Path basePathObj = Paths.get(getBasePath()).normalize();
                  if (!pathObj.normalize().startsWith(basePathObj)) {
                      System.out.println("尝试获取基础上传目录之外的文件信息: " + fullPath);
                        throw new IllegalArgumentException("不允许访问指定路径的文件");
                  }
             } else {
                 fullPath = Paths.get(getBasePath(), relativeOrFullPath).toString();
             }
         } catch (Exception e) {
             System.out.println("解析文件信息路径时出错: " + relativeOrFullPath + ", " + e.getMessage());
              throw new IllegalArgumentException("无效的文件路径: " + relativeOrFullPath, e);
         }

        File file = new File(fullPath);
        if (!file.exists()) {
            // 可能是请求时路径错误，或者文件已被删除
            System.out.println("尝试获取文件信息但文件不存在: FullPath=" + fullPath + ", RequestPath=" + relativeOrFullPath);
            throw new IllegalArgumentException("文件不存在: " + relativeOrFullPath);
        }
        
        try {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("name", file.getName());
            fileInfo.put("path", relativeOrFullPath); // 返回请求时使用的路径
            // fileInfo.put("fullPath", fullPath); // 避免泄露服务器完整路径
            fileInfo.put("size", file.length());
            fileInfo.put("readableSize", FileUtils.getReadableFileSize(file.length()));
            fileInfo.put("type", FileUtils.getFileType(file.getName()));
            fileInfo.put("lastModified", file.lastModified());
            fileInfo.put("isDirectory", file.isDirectory());
            fileInfo.put("url", convertFullPathToUrl(fullPath)); // 添加 URL
            
            return objectMapper.writeValueAsString(fileInfo);
        } catch (IOException e) {
            System.out.println("获取文件信息序列化失败: " + relativeOrFullPath + ", " + e.getMessage());
            throw new RuntimeException("获取文件信息失败", e);
        }
    }
    
    @Override
    public boolean downloadFile(String relativeOrFullPath, OutputStream outputStream) {
         if (relativeOrFullPath == null || relativeOrFullPath.isEmpty() || outputStream == null) {
             System.out.println("文件路径或输出流不能为空");
            return false;
        }
        
        // 构建完整物理路径
        String fullPath;
        try {
             Path pathObj = Paths.get(relativeOrFullPath);
             if (pathObj.isAbsolute()) {
                  fullPath = relativeOrFullPath;
                  // 可选：验证是否在 basePath 内
                   Path basePathObj = Paths.get(getBasePath()).normalize();
                  if (!pathObj.normalize().startsWith(basePathObj)) {
                      System.out.println("尝试下载基础上传目录之外的文件: " + fullPath);
                       return false;
                  }
             } else {
                 fullPath = Paths.get(getBasePath(), relativeOrFullPath).toString();
             }
         } catch (Exception e) {
            System.out.println("解析下载文件路径时出错: " + relativeOrFullPath + ", " + e.getMessage());
             return false;
         }


        Path filePath = Paths.get(fullPath);
        if (!Files.exists(filePath) || !Files.isReadable(filePath) || Files.isDirectory(filePath)) {
            System.out.println("下载目标文件不存在、不可读或为目录: " + fullPath);
                return false;
            }
            
        try {
            Files.copy(filePath, outputStream);
            outputStream.flush(); // 确保内容写入
            // System.out.println("文件下载成功: " + fullPath); // 成功日志可能过于频繁
            return true;
        } catch (IOException e) {
            // 捕获可能的客户端连接关闭异常
            if (e.getClass().getName().contains("ClientAbortException")) {
                System.out.println("文件下载中断 (客户端关闭连接): " + fullPath);
            } else {
                System.out.println("文件下载失败: " + fullPath + ", " + e.getMessage());
            }
            return false;
        } finally {
             // 通常不应在此处关闭 outputStream，调用者负责关闭
            // try { outputStream.close(); } catch (IOException e) { System.out.println("关闭输出流失败"); }
        }
    }
    
    @Override
    public String generateTempUrl(String filePath, long expireTime) {
        // 临时 URL 生成逻辑需要根据实际需求实现，可能涉及签名等
        System.out.println("generateTempUrl 方法未实现或已废弃，返回文件的常规 URL");
        // 可能需要将物理路径转换为 Web 可访问路径
         return convertFullPathToUrl(filePath); // 示例：直接返回普通 URL
    }
    
    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (!FileUtils.isImage(file.getOriginalFilename())) {
            // 返回更详细的允许类型
            throw new IllegalArgumentException("请上传合法的图片文件 (jpg, jpeg, png, gif, bmp, webp)");
        }
        // 可以添加文件大小限制检查等
        /*
        long maxSize = 10 * 1024 * 1024; // 示例：10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + FileUtils.getReadableFileSize(maxSize));
        }
        */
    }

    @Override
    public void init() {
         // 这个 init 方法似乎未使用 Spring 的初始化回调 (如 @PostConstruct)，用途是什么？
         // 如果是为了确保目录存在，InitializationUtils.SystemInitializer 已经处理了
         // 考虑使用 @PostConstruct 代替，或者移除此方法
        System.out.println("FileServiceImpl init() called. Base path from properties: " + getBasePath());
        // 可以考虑移除此方法，除非有特定用途
        /*
        try {
            // 确保基础目录存在 (虽然 InitializationUtils 也会做)
            Files.createDirectories(Paths.get(getBasePath()));
        } catch (Exception e) {
            System.out.println("初始化存储目录失败");
            throw new RuntimeException("Could not initialize storage location", e);
        }
        */
    }

    @Override
    public String storeFile(MultipartFile file, String... extraParams) throws IOException {
        // 这个方法似乎与 uploadXXX 方法重复，并且使用了 varargs extraParams，用途不明
        // 建议整合或移除
        System.out.println("storeFile 方法可能已废弃或需要整合");
        String type = (extraParams != null && extraParams.length > 0) ? extraParams[0] : "general"; // 假设第一个参数是类型

        String baseDir = getBasePath();
        String targetDir = Paths.get(baseDir, "general", type).toString(); // 存入 general 子目录

        // 应该使用统一的上传逻辑
        String fullPath = FileUtils.uploadFile(file, targetDir);
        return convertFullPathToUrl(fullPath); // 返回 URL
    }

    @Override
    public Path load(String filename) {
        // filename 应该是相对于 basePath 的路径
        return Paths.get(getBasePath()).resolve(filename);
    }

    @Override
    public Stream<Path> loadAll() {
        Path rootLocation = Paths.get(getBasePath());
        try {
            return Files.walk(rootLocation, 1) // 只遍历第一层
                    .filter(path -> !path.equals(rootLocation)) // 过滤掉根目录本身
                    .map(rootLocation::relativize); // 返回相对路径
        } catch (IOException e) {
            System.out.println("无法读取存储的文件");
            throw new RuntimeException("Failed to read stored files", e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
         // filename 应该是相对于 basePath 的路径
        try {
            Path file = load(filename); // load 返回的是绝对路径
            Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                System.out.println("无法读取文件: " + filename);
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (java.net.MalformedURLException e) {
            System.out.println("无法读取文件: " + filename);
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    /**
     * 将文件的完整物理路径转换为可通过 Web 访问的 URL
     * @param fullPath 文件的完整物理路径 (e.g., d:/Workspace/campus/uploads/images/uuid.jpg)
     * @return 相对于 Web 根目录的 URL (e.g., /uploads/images/uuid.jpg)
     */
    private String convertFullPathToUrl(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) {
            return "";
        }
        String basePath = getBasePath();
        // 确保 fullPath 以 basePath 开头，忽略大小写和路径分隔符差异
        Path fullPathObj = Paths.get(fullPath);
        Path basePathObj = Paths.get(basePath);

        if (fullPathObj.normalize().startsWith(basePathObj.normalize())) {
            // 获取相对路径
            Path relativePath = basePathObj.relativize(fullPathObj);
            // 构建 URL
            return URL_PREFIX + "/" + relativePath.toString().replace("\\", "/");
        } else {
            System.out.println("无法将物理路径转换为URL，因为它不在基础上传路径下。FullPath: " + fullPath + ", BasePath: " + basePath);
            // 返回原始路径或空字符串，或者可以尝试只返回文件名
            // 更好的方式可能是让 uploadFile 返回相对路径，但这与方式一的目标不符
             return fullPath; // 或者返回 FileUtils.getFileNameFromPath(fullPath)
        }
    }
} 