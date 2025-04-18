package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 论坛板块实体类
 */
@Data
@TableName("forum")
public class Forum implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 板块名称
     */
    private String name;

    /**
     * 板块类型标识符
     */
    private String type;

    /**
     * 板块颜色代码，用于前端显示
     */
    private String color;

    /**
     * 板块描述
     */
    private String description;

    /**
     * 板块图标
     */
    private String icon;

    /**
     * 排序序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
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