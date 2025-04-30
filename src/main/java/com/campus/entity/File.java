package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件记录实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("file")
public class File implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上传者用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 原始文件名
     */
    @TableField("filename")
    private String filename;

    /**
     * 文件存储路径或唯一标识符
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小 (bytes)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件MIME类型或扩展名
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private Date uploadTime;

    /**
     * 文件上下文类型 (personal, course, avatar, activity_poster, etc.)
     */
    @TableField("context_type")
    private String contextType;

    /**
     * 关联上下文的ID (如 course_id)
     */
    @TableField("context_id")
    private Long contextId;

    /**
     * 文件状态：0-已删除, 1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 存储类型 (local, s3, etc.)
     */
    @TableField("storage_type")
    private String storageType;

    // --- 非数据库字段 ---

    /**
     * 上传者用户名 (查询时JOIN)
     */
    @TableField(exist = false)
    private String uploaderName;

    /**
     * 课程名称 (查询课程资源时JOIN)
     */
    @TableField(exist = false)
    private String courseName;
} 