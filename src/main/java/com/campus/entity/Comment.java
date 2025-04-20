package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
    private Long authorId;

    /**
     * 父评论ID (用于嵌套评论)
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
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
    private Integer status;
    
    /**
     * 回复JSON字符串
     */
    @TableField("replies")
    private String replies;
}