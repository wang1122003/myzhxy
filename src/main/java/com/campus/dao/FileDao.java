package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 文件记录 DAO 接口
 */
@Mapper
@Repository
public interface FileDao extends BaseMapper<File> {

    /**
     * 分页查询课程资源列表 (学生视角)
     * - 关联课程表和用户表获取名称
     * - 关联选课表确保学生已选该课程
     *
     * @param page      分页对象
     * @param studentId 学生ID
     * @param courseId  课程ID (可选, 为null则查询所有已选课程的资源)
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT fr.*, c.course_name as courseName, u.username as uploaderName " +
            "FROM file fr " +
            "LEFT JOIN course c ON fr.context_id = c.id AND fr.context_type = 'course' " +
            "LEFT JOIN user u ON fr.user_id = u.id " +
            "JOIN course_selection cs ON fr.context_id = cs.course_id AND cs.student_id = #{studentId} " +
            "WHERE fr.status = 1 AND fr.context_type = 'course' " +
            "<if test='courseId != null'> AND fr.context_id = #{courseId} </if>" +
            "ORDER BY fr.upload_time DESC" +
            "</script>")
    IPage<File> selectCourseResourcePageForStudent(Page<File> page, @Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 文件列表查询（关联用户表获取上传者信息）
     *
     * @param page   分页对象
     * @param params 查询参数（filename、uploaderName、fileType等）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT f.*, u.username as uploaderName, u.real_name as uploaderRealName " +
            "FROM file f " +
            "LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE f.status = 1 " +
            "<if test='params.filename != null and params.filename != \"\"'> AND f.filename LIKE CONCAT('%', #{params.filename}, '%') </if>" +
            "<if test='params.uploaderName != null and params.uploaderName != \"\"'> AND (u.username LIKE CONCAT('%', #{params.uploaderName}, '%') OR u.real_name LIKE CONCAT('%', #{params.uploaderName}, '%')) </if>" +
            "<if test='params.fileType != null and params.fileType != \"\"'> AND f.file_type LIKE CONCAT('%', #{params.fileType}, '%') </if>" +
            "ORDER BY f.upload_time DESC" +
            "</script>")
    IPage<File> getFileListWithUserInfo(Page<File> page, @Param("params") Map<String, Object> params);

    /**
     * 分页查询个人文件列表
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    @Select("SELECT * FROM file WHERE user_id = #{userId} AND context_type = 'personal' AND status = 1 ORDER BY upload_time DESC")
    IPage<File> selectPersonalFilePage(Page<File> page, @Param("userId") Long userId);

    /**
     * 计算所有有效文件的总大小
     *
     * @return 文件总大小（字节）
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM file WHERE status = 1")
    Long sumTotalFileSize();

    /**
     * 统计各种文件类型的数量
     *
     * @return 文件类型及其数量的映射
     */
    @Select("SELECT file_type as fileType, COUNT(*) as count FROM file WHERE status = 1 GROUP BY file_type")
    List<Map<String, Object>> countFilesByTypeRaw();

    /**
     * 按文件类型统计数量
     *
     * @return 文件类型对应的数量
     */
    default Map<String, Long> countFilesByType() {
        List<Map<String, Object>> rawResults = countFilesByTypeRaw();
        Map<String, Long> result = new java.util.HashMap<>();

        for (Map<String, Object> row : rawResults) {
            String fileType = (String) row.get("fileType");
            if (fileType == null || fileType.isEmpty()) {
                fileType = "unknown";
            }
            Long count = ((Number) row.get("count")).longValue();
            result.put(fileType, count);
        }

        return result;
    }
} 