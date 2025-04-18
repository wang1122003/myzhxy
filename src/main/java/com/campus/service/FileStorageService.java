package com.campus.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 初始化存储位置
     */
    void init();

    /**
     * 存储上传的文件
     *
     * @param file         MultipartFile
     * @param subDirectory 子目录 (可选, 用于分类存储，例如 "avatars", "post_images")
     * @return 存储后的相对路径文件名
     */
    String storeFile(MultipartFile file, String... subDirectory);

    /**
     * 加载所有文件路径
     *
     * @return 文件路径 Stream
     */
    Stream<Path> loadAll();

    /**
     * 根据文件名加载文件路径
     *
     * @param filename 文件名
     * @return 文件路径
     */
    Path load(String filename);

    /**
     * 加载文件为资源
     *
     * @param filename 文件名
     * @return Spring Resource
     */
    Resource loadAsResource(String filename);

    /**
     * 删除所有文件（谨慎使用！）
     */
    void deleteAll();

    /**
     * 根据相对路径删除文件
     *
     * @param relativePath 文件的相对路径 (例如 "post_images/image.jpg")
     * @return 是否删除成功
     */
    boolean deleteFile(String relativePath);
} 