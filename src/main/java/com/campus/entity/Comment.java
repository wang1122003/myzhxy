package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论实体类
 */
@Data
@TableName("comment")
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 作者ID
     */
    @TableField("author_id")
    private Long userId;

    /**
     * 父评论ID (用于嵌套评论)
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creationTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 状态：1-正常，0-已删除
     */
    @TableField("status")
    private String status;
    
    /**
     * 回复JSON字符串
     */
    @TableField("replies")
    private String replies;

    // 关联作者信息
    @TableField(exist = false)
    private User author;

    // 嵌套回复 (如果需要)
    @TableField(exist = false)
    private List<Comment> repliesList;
}