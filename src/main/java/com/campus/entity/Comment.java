package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 论坛评论实体类
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
    private Long postId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论者ID
     */
    private Long userId;
    
    /**
     * 评论者名称
     */
    private String userName;
    
    /**
     * 评论者头像
     */
    private String userAvatar;
    
    /**
     * 父评论ID，如果是一级评论则为0
     */
    private Long parentId;
    
    /**
     * 回复的评论ID
     */
    private Long replyId;
    
    /**
     * 回复的用户ID
     */
    private Long replyUserId;
    
    /**
     * 回复的用户名称
     */
    private String replyUserName;
    
    /**
     * 点赞次数
     */
    private Integer likeCount;
    
    /**
     * 状态：0-已删除，1-正常
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
}