package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 论坛帖子实体类 (已集成标签、论坛板块和评论功能)
 */
@Data
@TableName(value = "post", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 帖子标题
     */
    @TableField("title")
    private String title;

    /**
     * 帖子内容
     */
    @TableField("content")
    private String content;

    /**
     * 作者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 评论次数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 点赞次数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论JSON数据
     * 格式: [{id: 1, userId: 2, content: "评论内容", creationTime: "2023-01-01 10:00:00", likeCount: 0, status: 1}]
     */
    @TableField("comments_json")
    private String commentsJson;

    /**
     * 评论列表对象 (非数据库字段，从JSON解析得到)
     */
    @TableField(exist = false)
    private List<Map<String, Object>> comments;

    /**
     * 是否置顶：0-否，1-是
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 是否精华：0-否，1-是
     */
    @TableField("is_essence")
    private Integer isEssence;

    /**
     * 状态：PUBLISHED(已发布), PENDING(待审核), DELETED(已删除), DRAFT(草稿), PRIVATE(私密)
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("creation_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creationTime;

    // Explicit setter for creationTime to resolve potential Lombok/MyBatis issue
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 更新时间
     */
    @TableField("last_update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdateTime;

    /**
     * 作者姓名（非数据库字段，需要Service层填充）
     */
    @TableField(exist = false)
    private String authorName;

    /**
     * 作者头像（非数据库字段）
     */
    @TableField(exist = false)
    private String authorAvatar;

    /**
     * 关联的作者对象 (非数据库字段，由 ResultMap 映射)
     */
    @TableField(exist = false)
    private User author;
}