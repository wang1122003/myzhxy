package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 论坛评论实体类
 */
@Data
@TableName(value = "comment", autoResultMap = true)
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
    private Long postId;

    /**
     * 评论作者ID
     */
    private Long authorId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 父评论ID (用于回复功能)
     */
    private Long parentId;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 回复JSON字符串
     */
    @JsonIgnore
    @TableField(value = "replies")
    private String repliesJson;

    /**
     * 回复列表 (反序列化后的对象)
     */
    @TableField(exist = false)
    private List<Comment> replies;

    /**
     * 评论作者信息
     */
    @TableField(exist = false)
    private User author;

    /**
     * 帖子信息
     */
    @TableField(exist = false)
    private Post post;
}