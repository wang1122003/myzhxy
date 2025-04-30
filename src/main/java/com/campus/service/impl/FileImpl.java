package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.FileDao;
import com.campus.entity.Course;
import com.campus.entity.File;
import com.campus.exception.CustomException;
import com.campus.service.CourseService;
import com.campus.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
public class FileImpl extends ServiceImpl<FileDao, File> implements FileService {

    // 从配置文件读取上传目录，需要你在 application.yml 或 properties 中配置
    @Value("${file.upload-dir:./uploads}") // 默认为项目根目录下的 uploads 文件夹
    private String uploadDir;

    @Autowired
    private CourseService courseService;

    @Override
    @Transactional
    public File uploadFile(MultipartFile file, Long userId, String contextType, Long contextId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new CustomException("上传的文件不能为空");
        }

        // 1. 存储文件到服务器
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 使用UUID生成唯一文件名，防止重名
        String storedFilename = UUID.randomUUID().toString().replace("-", "") + fileExtension;
        // 根据上下文类型创建子目录，例如 ./uploads/course, ./uploads/personal
        Path contextPath = Paths.get(uploadDir, contextType);
        if (!Files.exists(contextPath)) {
            try {
                Files.createDirectories(contextPath);
            } catch (IOException e) {
                log.error("创建目录失败: {}", contextPath, e);
                throw new CustomException("创建文件存储目录失败");
            }
        }
        Path destinationFile = contextPath.resolve(storedFilename).normalize();

        // 安全检查：确保目标路径在预期的上传目录下
        if (!destinationFile.startsWith(Paths.get(uploadDir).normalize())) {
            log.error("不安全的文件路径尝试: {}", destinationFile);
            throw new CustomException("文件路径不安全");
        }

        try {
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件上传成功: {}, 存储为: {}", originalFilename, destinationFile);
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalFilename, e);
            throw new CustomException("文件上传到服务器失败");
        }

        // 2. 保存文件记录到数据库
        File fileRecord = File.builder()
                .userId(userId)
                .filename(originalFilename)
                .filePath(destinationFile.toString()) // 存储完整或相对路径，取决于你的下载逻辑
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .uploadTime(new Date())
                .contextType(contextType)
                .contextId(contextId)
                .status(1) // 1: 正常
                .storageType("local") // 示例为本地存储
                .build();

        if (!this.save(fileRecord)) {
            // 如果数据库保存失败，需要删除已上传的物理文件
            try {
                Files.deleteIfExists(destinationFile);
                log.warn("数据库记录保存失败，已删除物理文件: {}", destinationFile);
            } catch (IOException ex) {
                log.error("数据库记录保存失败后，删除物理文件也失败: {}", destinationFile, ex);
                // 考虑添加补偿机制或告警
            }
            throw new CustomException("保存文件记录到数据库失败");
        }

        return fileRecord;
    }

    @Override
    public IPage<File> getMyPersonalFiles(int page, int size, Long userId) {
        if (userId == null) {
            throw new CustomException("用户ID不能为空");
        }
        Page<File> pageRequest = new Page<>(page, size);
        // 使用 Mapper 中定义的方法查询个人文件
        return baseMapper.selectPersonalFilePage(pageRequest, userId);
    }

    @Override
    public IPage<File> getCourseResourcesForStudent(int page, int size, Long studentId, Long courseId) {
        if (studentId == null) {
            throw new CustomException("学生ID不能为空");
        }
        Page<File> pageRequest = new Page<>(page, size);
        // 使用 Mapper 中定义的方法查询学生可见的课程资源
        return baseMapper.selectCourseResourcePageForStudent(pageRequest, studentId, courseId);
    }

    @Override
    public File getFileRecordById(Long fileId) {
        if (fileId == null) {
            return null;
        }
        // 考虑 Mybatis Plus 逻辑删除: 如果配置了，它会自动添加 status=1
        // 如果未配置，需要手动添加条件
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getId, fileId)
                .eq(File::getStatus, 1); // 显式添加状态条件
        return baseMapper.selectOne(queryWrapper);
        // return baseMapper.selectById(fileId); // 如果配置了逻辑删除插件
    }

    @Override
    @Transactional
    public boolean deleteFileRecord(Long fileId, Long userId) {
        if (fileId == null || userId == null) {
            throw new CustomException("文件ID或用户ID不能为空");
        }
        File file = this.getFileRecordById(fileId);

        if (file == null) {
            log.warn("尝试删除的文件记录不存在或已被删除: fileId={}, userId={}", fileId, userId);
            return true;
        }

        // 权限校验：
        if ("personal".equals(file.getContextType())) {
            if (!Objects.equals(file.getUserId(), userId)) {
                log.warn("无权删除文件: fileId={}, ownerId={}, operatorId={}", fileId, file.getUserId(), userId);
                throw new CustomException("无权删除该文件");
            }
        } else if ("course".equals(file.getContextType())) {
            // 2. 课程资料：允许上传者删除，或课程的授课教师删除
            boolean canDelete = Objects.equals(file.getUserId(), userId);
            // Check if operator is the uploader
            // Check if operator is the teacher of the course
            if (!canDelete && file.getContextId() != null) {
                try {
                    Course course = courseService.getCourseById(file.getContextId());
                    // Assuming Course entity has a teacherId field or similar
                    if (course != null && Objects.equals(course.getTeacherId(), userId)) {
                        canDelete = true;
                    } else {
                        log.warn("无法验证课程资料删除权限，课程 {} 不存在或教师 ID ({}) 不匹配操作者 ID ({})",
                                file.getContextId(), course != null ? course.getTeacherId() : "null", userId);
                    }
                } catch (Exception e) {
                    log.error("校验课程资料删除权限时获取课程信息出错, courseId: {}, operatorId: {}",
                            file.getContextId(), userId, e);
                    // Fallback: Do not allow deletion if course info check fails
                }
            }

            if (!canDelete) {
                log.warn("无权删除课程资料: fileId={}, uploaderId={}, operatorId={}, courseId={}",
                        fileId, file.getUserId(), userId, file.getContextId());
                throw new CustomException("无权删除该课程资料");
            }
        } else {
            // 其他类型的文件，根据需要添加权限校验
            log.warn("未知的 contextType 文件删除权限校验: {}", file.getContextType());
            if (!Objects.equals(file.getUserId(), userId)) {
                throw new CustomException("无权删除该类型文件");
            }
        }

        // 软删除：更新状态为 0
        file.setStatus(0);
        file.setUploadTime(null);
        boolean updated = this.updateById(file);

        if (updated) {
            log.info("文件记录软删除成功: fileId={}, operatorId={}", fileId, userId);
        } else {
            log.error("文件记录软删除失败: fileId={}, operatorId={}", fileId, userId);
        }
        return updated;
    }

    @Override
    public Long countTotalFiles() {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getStatus, 1); // 只统计状态正常的文件
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public Long sumTotalFileSize() {
        return baseMapper.sumTotalFileSize();
    }

    @Override
    public Map<String, Long> countFilesByType() {
        return baseMapper.countFilesByType();
    }

    @Override
    public Long countRecentUploads(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = calendar.getTime();

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getStatus, 1)
                .ge(File::getUploadTime, startDate);
        return baseMapper.selectCount(queryWrapper);
    }

    /**
     * 获取文件列表（基础实现，不带过滤条件）
     * 注意：此方法不是接口定义的方法，仅供内部使用
     *
     * @param page 页码
     * @param size 每页数量
     * @return 文件记录分页结果
     */
    public IPage<File> getFileList(int page, int size) {
        Page<File> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getStatus, 1) // 只查询状态正常的文件
                .orderByDesc(File::getUploadTime); // 按上传时间降序排列
        return baseMapper.selectPage(pageRequest, queryWrapper);
    }

    /**
     * 获取文件列表（管理员视图，带过滤条件）
     *
     * @param page         页码
     * @param size         每页数量
     * @param filename     文件名关键词（可选）
     * @param uploaderName 上传者名称（可选）
     * @param fileType     文件类型（可选）
     * @return 文件记录分页结果
     */
    @Override
    public IPage<File> getFileList(int page, int size, String filename, String uploaderName, String fileType) {
        Page<File> pageRequest = new Page<>(page, size);
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();

        // 基础条件：只查询状态正常的文件
        queryWrapper.eq(File::getStatus, 1);

        // 添加过滤条件
        if (filename != null && !filename.isEmpty()) {
            queryWrapper.like(File::getFilename, filename);
        }

        // 如果需要根据上传者名称过滤，这里可能需要关联查询用户表
        // 这里简化处理，实际实现应根据数据库结构调整
        // TODO: 优化为关联查询用户表

        if (fileType != null && !fileType.isEmpty()) {
            queryWrapper.like(File::getFileType, fileType);
        }

        // 按上传时间降序排列
        queryWrapper.orderByDesc(File::getUploadTime);

        return baseMapper.selectPage(pageRequest, queryWrapper);
    }

    /**
     * 文件统计信息DTO
     */
    @Override
    public Map<String, Object> getFileStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 获取总文件数
        statistics.put("totalFiles", this.countTotalFiles());

        // 获取总文件大小
        statistics.put("totalSize", this.sumTotalFileSize());

        // 获取文件类型分布
        statistics.put("fileTypes", this.countFilesByType());

        // 获取最近一周上传数量
        statistics.put("recentUploads", this.countRecentUploads(7));

        return statistics;
    }

    /**
     * 下载文件
     *
     * @param fileId   文件ID
     * @param response HTTP响应对象
     * @throws IOException 如果文件不存在或无法读取
     */
    @Override
    public void downloadFile(Long fileId, HttpServletResponse response) throws IOException {
        File fileRecord = this.getFileRecordById(fileId);
        if (fileRecord == null) {
            throw new CustomException("文件不存在或已被删除");
        }

        java.io.File file = new java.io.File(fileRecord.getFilePath());
        if (!file.exists()) {
            throw new CustomException("文件不存在或已被物理删除");
        }

        // 设置响应头
        response.setContentType(fileRecord.getFileType() != null ?
                fileRecord.getFileType() : "application/octet-stream");
        response.setContentLengthLong(fileRecord.getFileSize());

        // 设置文件名，处理中文文件名编码问题
        String filename = URLEncoder.encode(fileRecord.getFilename(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);

        // 使用缓冲流读取文件并写入响应
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             OutputStream os = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("下载文件时发生错误: {}", e.getMessage(), e);
            throw new CustomException("文件下载失败: " + e.getMessage());
        }
    }
} 