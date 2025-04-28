package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.FileRecordDao;
import com.campus.entity.FileRecord;
import com.campus.entity.Course;
import com.campus.exception.CustomException;
import com.campus.service.FileService;
import com.campus.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileRecordDao, FileRecord> implements FileService {

    // 从配置文件读取上传目录，需要你在 application.yml 或 properties 中配置
    @Value("${file.upload-dir:./uploads}") // 默认为项目根目录下的 uploads 文件夹
    private String uploadDir;

    @Autowired
    private CourseService courseService;

    @Override
    @Transactional
    public FileRecord uploadFile(MultipartFile file, Long userId, String contextType, Long contextId) throws IOException {
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
        FileRecord fileRecord = FileRecord.builder()
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
    public IPage<FileRecord> getMyPersonalFiles(int page, int size, Long userId) {
        if (userId == null) {
            throw new CustomException("用户ID不能为空");
        }
        Page<FileRecord> pageRequest = new Page<>(page, size);
        // 使用 Mapper 中定义的方法查询个人文件
        return baseMapper.selectPersonalFilePage(pageRequest, userId);
    }

    @Override
    public IPage<FileRecord> getCourseResourcesForStudent(int page, int size, Long studentId, Long courseId) {
        if (studentId == null) {
            throw new CustomException("学生ID不能为空");
        }
        Page<FileRecord> pageRequest = new Page<>(page, size);
        // 使用 Mapper 中定义的方法查询学生可见的课程资源
        return baseMapper.selectCourseResourcePageForStudent(pageRequest, studentId, courseId);
    }

    @Override
    public FileRecord getFileRecordById(Long fileId) {
        if (fileId == null) {
            return null;
        }
        // 考虑 Mybatis Plus 逻辑删除: 如果配置了，它会自动添加 status=1
        // 如果未配置，需要手动添加条件
        LambdaQueryWrapper<FileRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileRecord::getId, fileId)
                .eq(FileRecord::getStatus, 1); // 显式添加状态条件
        return baseMapper.selectOne(queryWrapper);
        // return baseMapper.selectById(fileId); // 如果配置了逻辑删除插件
    }

    @Override
    @Transactional
    public boolean deleteFileRecord(Long fileId, Long userId) {
        if (fileId == null || userId == null) {
            throw new CustomException("文件ID或用户ID不能为空");
        }
        FileRecord fileRecord = this.getFileRecordById(fileId);

        if (fileRecord == null) {
            log.warn("尝试删除的文件记录不存在或已被删除: fileId={}, userId={}", fileId, userId);
            return true;
        }

        // 权限校验：
        if ("personal".equals(fileRecord.getContextType())) {
            if (!Objects.equals(fileRecord.getUserId(), userId)) {
                log.warn("无权删除文件: fileId={}, ownerId={}, operatorId={}", fileId, fileRecord.getUserId(), userId);
                throw new CustomException("无权删除该文件");
            }
        } else if ("course".equals(fileRecord.getContextType())) {
            // 2. 课程资料：允许上传者删除，或课程的授课教师删除
            boolean canDelete = false;
            // Check if operator is the uploader
            if (Objects.equals(fileRecord.getUserId(), userId)) {
                canDelete = true;
            }
            // Check if operator is the teacher of the course
            if (!canDelete && fileRecord.getContextId() != null) {
                try {
                    Course course = courseService.getCourseById(fileRecord.getContextId());
                    // Assuming Course entity has a teacherId field or similar
                    if (course != null && Objects.equals(course.getTeacherId(), userId)) {
                        canDelete = true;
                    } else {
                        log.warn("无法验证课程资料删除权限，课程 {} 不存在或教师 ID ({}) 不匹配操作者 ID ({})",
                                fileRecord.getContextId(), course != null ? course.getTeacherId() : "null", userId);
                    }
                } catch (Exception e) {
                    log.error("校验课程资料删除权限时获取课程信息出错, courseId: {}, operatorId: {}",
                            fileRecord.getContextId(), userId, e);
                    // Fallback: Do not allow deletion if course info check fails
                }
            }

            if (!canDelete) {
                log.warn("无权删除课程资料: fileId={}, uploaderId={}, operatorId={}, courseId={}",
                        fileId, fileRecord.getUserId(), userId, fileRecord.getContextId());
                throw new CustomException("无权删除该课程资料");
            }
        } else {
            // 其他类型的文件，根据需要添加权限校验
            log.warn("未知的 contextType 文件删除权限校验: {}", fileRecord.getContextType());
            if (!Objects.equals(fileRecord.getUserId(), userId)) {
                throw new CustomException("无权删除该类型文件");
            }
        }

        // 软删除：更新状态为 0
        fileRecord.setStatus(0);
        fileRecord.setUploadTime(null);
        boolean updated = this.updateById(fileRecord);

        if (updated) {
            log.info("文件记录软删除成功: fileId={}, operatorId={}", fileId, userId);
        } else {
            log.error("文件记录软删除失败: fileId={}, operatorId={}", fileId, userId);
        }
        return updated;
    }

    // Utility method to delete the physical file (potentially unsafe if file is shared)
    // private void deletePhysicalFile(String filePath) {
    //     try {
    //         Path path = Paths.get(filePath);
    //         Files.deleteIfExists(path);
    //     } catch (IOException e) {
    //         log.error("Error deleting physical file: {}", filePath, e);
    //     }
    // }
} 