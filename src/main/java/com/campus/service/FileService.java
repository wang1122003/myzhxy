package com.campus.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 文件服务接口
 * 提供文件上传、下载和管理的功能
 */
public interface FileService {

    /**
     * 初始化文件存储系统
     */
    void init();

    /**
     * 存储文件，并返回可访问的相对路径或URL
     *
     * @param file        上传的文件
     * @param extraParams 额外参数，可选
     * @return 文件访问路径
     */
    String storeFile(MultipartFile file, String... extraParams) throws IOException;

    /**
     * 加载文件路径
     *
     * @param filename 文件名
     * @return 文件路径
     */
    Path load(String filename);

    /**
     * 加载所有文件路径
     *
     * @return 文件路径流
     */
    Stream<Path> loadAll();

    /**
     * 将文件加载为资源，用于下载
     *
     * @param filename 文件名
     * @return 文件资源
     */
    Resource loadAsResource(String filename);
    
    /**
     * 上传图片
     * @param file 图片文件
     * @return 图片路径
     */
    String uploadImage(MultipartFile file) throws IOException;
    
    /**
     * 按类型上传图片
     * @param file 图片文件
     * @param type 图片类型
     * @return 图片路径
     */
    String uploadImage(MultipartFile file, String type) throws IOException;
    
    /**
     * 上传活动海报
     * @param file 海报文件
     * @return 海报路径
     */
    String uploadActivityPoster(MultipartFile file) throws IOException;
    
    /**
     * 上传用户头像
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像路径
     */
    String uploadUserAvatar(MultipartFile file, Long userId) throws IOException;
    
    /**
     * 上传帖子图片
     * @param file 图片文件
     * @return 图片路径
     */
    String uploadPostImage(MultipartFile file) throws IOException;
    
    /**
     * 上传文档
     * @param file 文档文件
     * @return 文档路径
     */
    String uploadDocument(MultipartFile file) throws IOException;
    
    /**
     * 上传课程资料
     * @param file 资料文件
     * @param courseId 课程ID
     * @return 资料路径
     */
    String uploadCourseMaterial(MultipartFile file, Long courseId) throws IOException;

    /**
     * 上传通知附件
     *
     * @param file 附件文件
     * @return 附件信息
     */
    Map<String, String> uploadNoticeAttachment(MultipartFile file) throws IOException;

    /**
     * 删除文件
     * @param relativePath 文件相对路径
     * @return 是否删除成功
     */
    boolean deleteFile(String relativePath);

    /**
     * 删除所有文件
     *
     * @return 是否删除成功
     */
    boolean deleteAll();
    
    /**
     * 获取文件信息
     * @param filePath 文件路径
     * @return 文件信息JSON字符串
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
     * @return 临时URL
     */
    String generateTempUrl(String filePath, long expireTime);
} 