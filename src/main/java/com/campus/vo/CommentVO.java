package com.campus.vo;

import com.campus.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论视图对象，包含评论者信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends Comment {

    /**
     * 评论者用户名
     */
    private String username;

    /**
     * 评论者头像URL (如果需要)
     */
    private String avatar;

    // 可以根据需要添加其他关联信息，例如回复的目标用户名等
} 