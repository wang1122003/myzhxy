package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.File;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 文件服务接口 (Updated - ensure only these 5 methods exist)
 */
public interface FileService {

    /**
     * 上传文件并保存记录
     *
     * @param file        上传的文件
     * @param userId      上传者用户ID
     * @param contextType 文件上下文类型 (e.g., "personal", "course")
     * @param contextId   关联上下文ID (e.g., courseId, nullable for personal)
     * @return 保存的文件记录
     * @throws IOException 文件读写异常
     */
    File uploadFile(MultipartFile file, Long userId, String contextType, Long contextId) throws IOException;

    /**
     * 获取当前用户的个人文件列表 (分页)
     *
     * @param page   页码
     * @param size   每页数量
     * @param userId 用户ID
     * @return 文件记录分页结果
     */
    IPage<File> getMyPersonalFiles(int page, int size, Long userId);

    /**
     * 获取学生可见的课程资源列表 (分页)
     *
     * @param page      页码
     * @param size      每页数量
     * @param studentId 学生ID
     * @param courseId  课程ID (可选, 为null查询所有已选课程)
     * @return 文件记录分页结果
     */
    IPage<File> getCourseResourcesForStudent(int page, int size, Long studentId, Long courseId);

    /**
     * 获取文件记录信息
     *
     * @param fileId 文件记录ID
     * @return 文件记录，不存在返回null
     */
    File getFileRecordById(Long fileId);

    /**
     * 删除文件 (软删除)
     *
     * @param fileId 文件记录ID
     * @param userId 操作者用户ID (用于权限校验)
     * @return 是否成功删除
     */
    boolean deleteFileRecord(Long fileId, Long userId);

    /**
     * 获取文件总数
     *
     * @return 系统中的文件总数
     */
    Long countTotalFiles();

    /**
     * 获取系统中所有文件的总大小
     *
     * @return 文件总大小（字节）
     */
    Long sumTotalFileSize();

    /**
     * 按文件类型统计文件数量
     *
     * @return 包含各类型文件数量的Map
     */
    Map<String, Long> countFilesByType();

    /**
     * 统计最近几天的上传文件数量
     *
     * @param days 最近几天
     * @return 上传文件数量
     */
    Long countRecentUploads(int days);

    /**
     * 获取文件列表 (管理员视图，分页)
     *
     * @param page         页码
     * @param size         每页数量
     * @param filename     文件名关键词（可选）
     * @param uploaderName 上传者名称（可选）
     * @param fileType     文件类型（可选）
     * @return 文件记录分页结果
     */
    IPage<File> getFileList(int page, int size, String filename, String uploaderName, String fileType);

    /**
     * 获取文件统计信息
     *
     * @return 包含文件统计数据的Map
     */
    Map<String, Object> getFileStatistics();

    /**
     * 下载文件
     *
     * @param fileId   文件ID
     * @param response HTTP响应对象
     * @throws IOException 如果文件不存在或无法读取
     */
    void downloadFile(Long fileId, HttpServletResponse response) throws IOException;
} 