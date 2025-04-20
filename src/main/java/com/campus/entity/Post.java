package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 论坛帖子实体类 (已集成标签和论坛板块功能)
 */
@Data
@TableName(value = "post", autoResultMap = true)
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
    @TableField("author_id")
    private Long authorId;
    
    /**
     * 板块类型
     * 例如：学习交流、校园生活、招聘信息等
     */
    @TableField("forum_type")
    private String forumType;

    /**
     * 板块颜色代码
     * 用于前端显示
     */
    @TableField("forum_color")
    private String forumColor;

    /**
     * 标签列表
     * 使用JSON数组形式存储
     */
    @TableField(value = "tags", typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    
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
     * 状态：1-正常，0-禁用
     */
    @TableField("status")
    private Integer status;
    
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
     * 论坛版块名称（非数据库字段）
     */
    @TableField(exist = false)
    private String forumTypeName;
}