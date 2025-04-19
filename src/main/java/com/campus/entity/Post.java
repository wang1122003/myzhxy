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
    @TableField(exist = false)
    private String authorName;
    
    /**
     * 作者头像
     */
    @TableField(exist = false)
    private String authorAvatar;
    
    /**
     * 论坛板块ID (数据库中不存在此列，已移除)
     */
    // @TableField("forum_id")
    // private Long forumId;
    
    /**
     * 板块名称，从Forum实体填充而来
     */
    @TableField(exist = false)
    private String forumName;

    /**
     * 板块类型
     * 例如：学习交流、校园生活、招聘信息等
     * 从Forum实体集成
     */
    private String forumType;

    /**
     * 板块颜色代码
     * 用于前端显示，从Forum实体集成
     */
    private String forumColor;

    /**
     * 标签列表
     * 使用JSON数组形式存储，从Tag实体集成而来
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    
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
     * 状态：1-正常，0-禁用
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
     * 当前用户是否已点赞，用于前端显示
     */
    @TableField(exist = false)
    private Boolean liked = false;

    /**
     * 关联的作者信息
     */
    @TableField(exist = false)
    private User author;
}