package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 院系实体类
 * 用于表示学校的院系信息
 */
@Data
@TableName("department")
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 院系ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 院系名称
     */
    @TableField("name")
    private String name;

    /**
     * 院系简介
     */
    @TableField("introduction")
    private String introduction;

    /**
     * 院系主任ID
     */
    @TableField("director_id")
    private Long directorId;

    /**
     * 院系办公室地址
     */
    @TableField("office_location")
    private String officeLocation;

    /**
     * 院系联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 院系联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;

    /**
     * 院系官网URL
     */
    @TableField("website_url")
    private String websiteUrl;

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

    // --- 非数据库字段 --- 

    /**
     * 院系主任姓名 (非数据库字段，通过关联查询获取)
     */
    @TableField(exist = false)
    private String directorName;

    /**
     * 院系包含的专业列表 (非数据库字段，通过关联查询获取)
     */
    @TableField(exist = false)
    private List<Major> majors;

    /**
     * 院系下的教师列表 (非数据库字段，通过关联查询获取)
     */
    @TableField(exist = false)
    private List<Teacher> teachers;

    /**
     * 院系下的学生人数 (非数据库字段，通过统计查询获取)
     */
    @TableField(exist = false)
    private Integer studentCount;

    /**
     * 院系下的课程数量 (非数据库字段，通过统计查询获取)
     */
    @TableField(exist = false)
    private Integer courseCount;

    // --- 可能需要的其他辅助字段 --- 

    /**
     * 临时存储字段，用于特定业务场景
     */
    @TableField(exist = false)
    private String tempInfo;

    /**
     * 标记字段，例如用于标记是否选中等
     */
    @TableField(exist = false)
    private Boolean selected;

    /**
     * 排序字段
     */
    @TableField(exist = false)
    private Integer sortOrder;

    /**
     * 扩展属性，用于存储JSON或其他格式的附加信息
     */
    @TableField(exist = false)
    private String extendedProperties;

    // Major 和 Teacher 类需要单独定义

    /**
     * 专业实体类 (内部类示例，实际项目中建议单独创建文件)
     */
    @Data
    public static class Major implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String name;
        private String description;
        private Long departmentId;
    }

    /**
     * 教师实体类 (内部类示例，实际项目中建议单独创建文件)
     */
    @Data
    public static class Teacher implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String name;
        private String title;
        private Long departmentId;
    }
} 