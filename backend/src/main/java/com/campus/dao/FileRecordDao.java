package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 文件记录 DAO 接口
 */
@Mapper
@Repository
public interface FileRecordDao extends BaseMapper<FileRecord> {

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
            "FROM file_record fr " +
            "LEFT JOIN course c ON fr.context_id = c.id AND fr.context_type = 'course' " +
            "LEFT JOIN user u ON fr.user_id = u.id " +
            "JOIN course_selection cs ON fr.context_id = cs.course_id AND cs.student_id = #{studentId} " +
            "WHERE fr.status = 1 AND fr.context_type = 'course' " +
            "<if test='courseId != null'> AND fr.context_id = #{courseId} </if>" +
            "ORDER BY fr.upload_time DESC" +
            "</script>")
    IPage<FileRecord> selectCourseResourcePageForStudent(Page<FileRecord> page, @Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 分页查询个人文件列表
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    @Select("SELECT * FROM file_record WHERE user_id = #{userId} AND context_type = 'personal' AND status = 1 ORDER BY upload_time DESC")
    IPage<FileRecord> selectPersonalFilePage(Page<FileRecord> page, @Param("userId") Long userId);
} 