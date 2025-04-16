package com.campus.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 文件服务接口
 * 提供文件上传、下载和管理的功能
 */
public interface FileService {
    
    /**
     * 上传图片
     * @param file 图片文件
     * @return 文件访问路径
     */
    String uploadImage(MultipartFile file) throws IOException;
    
    /**
     * 上传特定类型的图片
     * @param file 图片文件
     * @param type 图片类型
     * @return 文件访问路径
     */
    String uploadImage(MultipartFile file, String type) throws IOException;
    
    /**
     * 上传活动海报
     * @param file 海报文件
     * @return 文件访问路径
     */
    String uploadActivityPoster(MultipartFile file) throws IOException;
    
    /**
     * 上传用户头像
     * @param file 头像文件
     * @param userId 用户ID
     * @return 文件访问路径
     */
    String uploadUserAvatar(MultipartFile file, Long userId) throws IOException;
    
    /**
     * 上传帖子图片
     * @param file 图片文件
     * @return 文件访问路径
     */
    String uploadPostImage(MultipartFile file) throws IOException;
    
    /**
     * 上传文档
     * @param file 文档文件
     * @return 文件访问路径
     */
    String uploadDocument(MultipartFile file) throws IOException;
    
    /**
     * 上传课程资料
     * @param file 资料文件
     * @param courseId 课程ID
     * @return 文件访问路径
     */
    String uploadCourseMaterial(MultipartFile file, Long courseId) throws IOException;
    
    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);
    
    /**
     * 获取文件信息
     * @param filePath 文件路径
     * @return 文件信息（JSON格式）
     */
    String getFileInfo(String filePath);
    
    /**
     * 下载文件
     * @param filePath 文件路径
     * @param outputStream 输出流
     * @return 是否下载成功
     */
    boolean downloadFile(String filePath, OutputStream outputStream);
    
    /**
     * 生成临时访问URL
     * @param filePath 文件路径
     * @param expireTime 过期时间（毫秒）
     * @return 临时访问URL
     */
    String generateTempUrl(String filePath, long expireTime);
} 