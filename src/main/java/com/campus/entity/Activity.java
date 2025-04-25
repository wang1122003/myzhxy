package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动实体类
 */
@Data
@TableName("activity")
public class Activity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 活动ID (主键)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 活动标题
     */
    @TableField("title")
    private String title;

    /**
     * 活动描述
     */
    @TableField("description")
    private String description;

    /**
     * 活动类型 (例如: "讲座", "社团", "竞赛" 等)
     */
    @TableField("type")
    private String type;

    /**
     * 活动地点
     */
    @TableField("location")
    private String location;

    /**
     * 活动开始时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 活动结束时间
     */
    @TableField("end_time")
    private Date endTime;

    /**
     * 组织者ID (关联用户表)
     */
    @TableField("organizer_id")
    private Long organizerId;

    /**
     * 活动联系方式
     */
    @TableField("contact")
    private String contact;

    /**
     * 活动海报图片URL
     */
    @TableField("poster_url")
    private String posterUrl;

    /**
     * 最大参与人数限制
     */
    @TableField("max_participants")
    private Integer maxParticipants;

    /**
     * 活动状态 (例如: 1-正常/已发布, 0-已取消, 2-进行中, 3-已结束等)
     */
    @TableField("status")
    private String status;

    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 报名截止时间
     */
    @TableField("registration_deadline")
    private Date registrationDeadline;

    /**
     * 设置活动海报URL (显式setter)
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}