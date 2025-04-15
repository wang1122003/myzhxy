package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 论坛帖子实体类
 */
@Data
@TableName("post")
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
    private String title;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 作者ID
     */
    private Long authorId;
    
    /**
     * 作者名称
     */
    private String authorName;
    
    /**
     * 作者头像
     */
    private String authorAvatar;
    
    /**
     * 板块ID
     */
    private Long forumId;
    
    /**
     * 板块名称
     */
    private String forumName;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 评论次数
     */
    private Integer commentCount;
    
    /**
     * 点赞次数
     */
    private Integer likeCount;
    
    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;
    
    /**
     * 是否精华：0-否，1-是
     */
    private Integer isEssence;
    
    /**
     * 状态：0-草稿，1-已发布，2-已删除
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