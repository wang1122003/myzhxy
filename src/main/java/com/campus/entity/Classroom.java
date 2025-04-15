package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private String roomNo;
    
    /**
     * 教室名称
     */
    private String roomName;
    
    /**
     * 教学楼ID
     */
    private Long buildingId;
    
    /**
     * 教学楼名称
     */
    private String buildingName;
    
    /**
     * 楼层
     */
    private Integer floor;
    
    /**
     * 教室类型：1-普通教室，2-多媒体教室，3-实验室，4-会议室
     */
    private Integer roomType;
    
    /**
     * 容量
     */
    private Integer capacity;
    
    /**
     * 设备描述
     */
    private String equipment;
    
    /**
     * 状态：0-不可用，1-可用
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