package com.campus.controller;

import com.campus.service.FileService;
import com.campus.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/api/file")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private FileService fileService;
    
    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
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
    public Result uploadImageByType(@RequestParam("file") MultipartFile file, @PathVariable("type") String type) {
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
    public Result uploadActivityPoster(@RequestParam("file") MultipartFile file) {
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
    public Result uploadUserAvatar(@RequestParam("file") MultipartFile file, @PathVariable("userId") Long userId) {
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
    public Result uploadPostImage(@RequestParam("file") MultipartFile file) {
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
    public Result uploadDocument(@RequestParam("file") MultipartFile file) {
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
     * @return 上传结果
     */
    @PostMapping("/upload/course/material/{courseId}")
    public Result uploadCourseMaterial(@RequestParam("file") MultipartFile file, @PathVariable("courseId") Long courseId) {
        try {
            String filePath = fileService.uploadCourseMaterial(file, courseId);
            return Result.success("课程材料上传成功", filePath);
        } catch (Exception e) {
            logger.error("课程材料上传失败", e);
            return Result.error("课程材料上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result deleteFile(@RequestParam("filePath") String filePath) {
        try {
            boolean success = fileService.deleteFile(filePath);
            if (success) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败");
            }
        } catch (Exception e) {
            logger.error("文件删除失败", e);
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
    public Result getFileInfo(@RequestParam("filePath") String filePath) {
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
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
            
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
    public Result generateTempUrl(@RequestParam("filePath") String filePath, 
                                 @RequestParam(value = "expireTime", defaultValue = "3600000") long expireTime) {
        try {
            String tempUrl = fileService.generateTempUrl(filePath, expireTime);
            return Result.success("临时URL生成成功", tempUrl);
        } catch (Exception e) {
            logger.error("生成临时URL失败", e);
            return Result.error("生成临时URL失败: " + e.getMessage());
        }
    }
} 