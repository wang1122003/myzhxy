package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件服务接口 (Updated - ensure only these 5 methods exist)
 */
public interface FileService {

    /**
     * 上传文件并保存记录
     *
     * @param file         上传的文件
     * @param userId       上传者用户ID
     * @param contextType  文件上下文类型 (e.g., "personal", "course")
     * @param contextId    关联上下文ID (e.g., courseId, nullable for personal)
     * @return 保存的文件记录
     * @throws IOException 文件读写异常
     */
    FileRecord uploadFile(MultipartFile file, Long userId, String contextType, Long contextId) throws IOException;

    /**
     * 获取当前用户的个人文件列表 (分页)
     *
     * @param page      页码
     * @param size      每页数量
     * @param userId    用户ID
     * @return 文件记录分页结果
     */
    IPage<FileRecord> getMyPersonalFiles(int page, int size, Long userId);

    /**
     * 获取学生可见的课程资源列表 (分页)
     *
     * @param page      页码
     * @param size      每页数量
     * @param studentId 学生ID
     * @param courseId  课程ID (可选, 为null查询所有已选课程)
     * @return 文件记录分页结果
     */
    IPage<FileRecord> getCourseResourcesForStudent(int page, int size, Long studentId, Long courseId);

    /**
     * 获取文件记录信息
     *
     * @param fileId 文件记录ID
     * @return 文件记录，不存在返回null
     */
    FileRecord getFileRecordById(Long fileId);

    /**
     * 删除文件 (软删除)
     *
     * @param fileId 文件记录ID
     * @param userId 操作者用户ID (用于权限校验)
     * @return 是否成功删除
     */
    boolean deleteFileRecord(Long fileId, Long userId);
} 