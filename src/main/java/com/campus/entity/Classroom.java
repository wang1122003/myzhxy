package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 教室实体类
 */
@Data
@TableName("classroom")
public class Classroom implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 教室ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 教室编号
     */
    @TableField("name")
    private String name;
    
    /**
     * 教学楼
     */
    @TableField("building")
    private String building;
    
    /**
     * 教室容量
     */
    @TableField("capacity")
    private Integer capacity;
    
    /**
     * 状态：0-禁用，1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 教室类型：1-普通教室，2-多媒体教室，3-实验室
     */
    @TableField("room_type")
    private Integer roomType;
    
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
}