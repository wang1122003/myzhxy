package com.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.utils.Result;
import com.campus.entity.FileRecord;
import com.campus.entity.User;
import com.campus.service.FileService;
import com.campus.service.AuthService;
import com.campus.service.CourseService;
import com.campus.service.CourseSelectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import com.campus.entity.Course;

/**
 * 文件管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/file") // 统一前缀
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private AuthService authService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseSelectionService courseSelectionService;

    /**
     * 通用文件上传接口
     * 前端需要指定 contextType，对于课程资料还需指定 courseId
     * @param file 上传的文件
     * @param contextType 上下文类型 (如 personal, course)
     * @param contextId   关联ID (如 courseId)
     * @param user        当前登录用户
     * @return 上传结果，包含文件记录信息
     */
    @PostMapping("/upload")
    public Result<FileRecord> uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("contextType") String contextType,
                                         @RequestParam(value = "contextId", required = false) Long contextId,
                                         @AuthenticationPrincipal User user) {
        if (user == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            // 校验 contextType
            if (!("personal".equals(contextType) || "course".equals(contextType))) {
                // 可以根据需要扩展支持的类型
                return Result.error(400, "无效的文件上下文类型");
            }
            if ("course".equals(contextType) && contextId == null) {
                return Result.error(400, "上传课程资料必须指定课程ID");
            }
            if ("personal".equals(contextType)) {
                contextId = null; // 个人文件不需要 contextId
            }

            FileRecord record = fileService.uploadFile(file, user.getId(), contextType, contextId);
            return Result.success(record);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传处理失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传时发生未知错误", e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的个人文件列表
     * @param page 页码
     * @param size 每页数量
     * @param user 当前登录用户
     * @return 文件列表分页数据
     */
    @GetMapping("/my")
    public Result<IPage<FileRecord>> getMyFiles(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @AuthenticationPrincipal User user) {
        if (user == null) {
            return Result.error(401, "用户未登录");
        }
        IPage<FileRecord> filePage = fileService.getMyPersonalFiles(page, size, user.getId());
        return Result.success(filePage);
    }

    /**
     * 获取学生可见的课程资源列表
     *
     * @param page     页码
     * @param size     每页数量
     * @param courseId 课程ID (可选, 筛选特定课程)
     * @param user     当前登录用户 (必须是学生)
     * @return 课程资源列表分页数据
     */
    @GetMapping("/resource/list") // 复用前端已有的路径
    public Result<IPage<FileRecord>> getCourseResources(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) Long courseId,
                                                        @AuthenticationPrincipal User user) {
        if (user == null) {
            return Result.error(401, "用户未登录");
        }
        // 假设学生的用户类型枚举值为 UserType.STUDENT 或类似
        // 转换为字符串进行比较
        if (!"student".equalsIgnoreCase(String.valueOf(user.getUserType()))) {
            return Result.error(403, "只有学生可以查看课程资源");
        }

        IPage<FileRecord> resourcePage = fileService.getCourseResourcesForStudent(page, size, user.getId(), courseId);
        return Result.success(resourcePage);
    }

    /**
     * 删除文件记录 (软删除)
     *
     * @param id   文件记录ID
     * @param user 当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteFile(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return Result.error(401, "用户未登录");
        }
        try {
            boolean deleted = fileService.deleteFileRecord(id, user.getId());
            if (deleted) {
                return Result.success("文件删除成功");
            } else {
                // 这种情况可能是文件已删除或不存在，但也可能更新失败
                log.warn("删除文件记录操作返回 false: fileId={}, userId={}", id, user.getId());
                return Result.error(404, "文件不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败: fileId={}, userId={}", id, user.getId(), e);
            return Result.error(500, "删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * @param id 文件记录ID
     * @param user 当前登录用户
     * @return 文件流或错误响应
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) {
            // 或者重定向到登录页
            return ResponseEntity.status(401).build();
        }
        try {
            FileRecord fileRecord = fileService.getFileRecordById(id);
            if (fileRecord == null) {
                return ResponseEntity.notFound().build();
            }

            // --- 权限校验 --- 
            // 1. 个人文件：仅限本人下载
            if ("personal".equals(fileRecord.getContextType()) && !Objects.equals(fileRecord.getUserId(), user.getId())) {
                log.warn("无权下载个人文件: fileId={}, ownerId={}, operatorId={}", id, fileRecord.getUserId(), user.getId());
                return ResponseEntity.status(403).build();
            }
            // 2. 课程资料：需要校验学生是否选了该课 (或者教师是否是授课教师)
            if ("course".equals(fileRecord.getContextType())) {
                // TODO: 实现学生选课或教师授课的权限校验逻辑
                // 简化处理：暂时允许所有登录用户下载课程资料 (需要修复)
                log.warn("课程资料下载权限校验未实现: fileId={}, courseId={}", id, fileRecord.getContextId());
                // boolean hasPermission = checkCoursePermission(user, fileRecord.getContextId());
                // if (!hasPermission) { return ResponseEntity.status(403).build(); }
            }
            // --- 权限校验结束 --- 

            Path filePath = Paths.get(fileRecord.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 尝试获取文件MIME类型，如果获取不到，使用通用类型
                String contentType = null;
                try {
                    contentType = Files.probeContentType(filePath);
                } catch (IOException e) {
                    log.warn("无法获取文件类型: {}", filePath, e);
                }
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        // 设置 Content-Disposition 让浏览器提示下载，并使用原始文件名
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileRecord.getFilename() + "\"")
                        .body(resource);
            } else {
                log.error("无法读取文件或文件不存在: fileId={}, path={}", id, filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("文件路径格式错误: fileId={}", id, e);
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            log.error("下载文件时发生错误: fileId={}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 上传课程资料
     *
     * @param file     文件
     * @param courseId 课程ID
     * @return 上传结果
     */
    @PostMapping("/upload/course/{courseId}")
    public Result<FileRecord> uploadCourseResource(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long courseId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return Result.error(HttpStatus.UNAUTHORIZED.value(), "请登录");
        }

        boolean hasPermission = false;
        try {
            Course course = courseService.getCourseById(courseId);
            if (course != null && Objects.equals(course.getTeacherId(), currentUser.getId())) {
                hasPermission = true;
            }
            // if (!hasPermission && courseSelectionService.isStudentEnrolled(currentUser.getId(), courseId)) {
            //     // hasPermission = true; // Uncomment if students can upload
            // }
        } catch (Exception e) {
            log.error("检查上传课程资料权限时出错, courseId: {}, userId: {}", courseId, currentUser.getId(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "权限校验失败: " + e.getMessage());
        }

        if (!hasPermission) {
            return Result.error(HttpStatus.FORBIDDEN.value(), "无权上传该课程的资料");
        }

        try {
            FileRecord record = fileService.uploadFile(file, currentUser.getId(), "course", courseId);
            return Result.success("上传成功", record);
        } catch (IOException e) {
            log.error("上传课程资料失败, courseId: {}, userId: {}", courseId, currentUser.getId(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("处理课程资料上传时出错, courseId: {}, userId: {}", courseId, currentUser.getId(), e);
            return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "处理上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载课程资料
     *
     * @param fileId   文件记录ID
     * @param response HttpServletResponse 用于文件下载
     */
    @GetMapping("/download/course/{fileId}")
    public void downloadCourseResource(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        User currentUser = authService.getCurrentAuthenticatedUser();
        FileRecord fileRecord = fileService.getFileRecordById(fileId);

        if (fileRecord == null || !"course".equals(fileRecord.getContextType())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "课程文件未找到");
            return;
        }

        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请登录");
            return;
        }

        boolean hasPermission = false;
        Long courseId = fileRecord.getContextId();
        try {
            Course course = courseService.getCourseById(courseId);
            if (course != null && Objects.equals(course.getTeacherId(), currentUser.getId())) {
                hasPermission = true;
            }
            if (!hasPermission && courseSelectionService.isCourseTaken(currentUser.getId(), courseId, "current_term")) {
                hasPermission = true;
            }
        } catch (Exception e) {
            log.error("检查下载课程资料权限时出错, courseId: {}, userId: {}", courseId, currentUser.getId(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "权限校验失败");
            return;
        }

        if (!hasPermission) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权下载该文件");
            return;
        }

        Path filePath = Paths.get(fileRecord.getFilePath());
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件在服务器上不存在或不可读");
            return;
        }

        response.setContentType(fileRecord.getFileType() != null ? fileRecord.getFileType() : "application/octet-stream");
        response.setContentLengthLong(fileRecord.getFileSize());
        String headerValue = String.format("attachment; filename=\"%s\"",
                URLEncoder.encode(fileRecord.getFilename(), "UTF-8").replace("+", "%20"));
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, headerValue);

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            log.error("下载文件时发生IO错误: fileId={}, path={}", fileId, filePath, e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件下载失败");
            }
        }
    }

    // TODO: 实现课程资料下载权限校验方法
    // private boolean checkCoursePermission(User user, Long courseId) { ... }
} 