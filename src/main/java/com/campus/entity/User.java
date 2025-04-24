package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable, UserDetails {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;
    
    /**
     * 性别：0-女，1-男
     */
    private Integer gender;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;
    
    /**
     * 用户类型 (Student, Teacher, Admin)
     */
    @TableField("user_type")
    private String userType;
    
    /**
     * 状态 (Active, Inactive)
     */
    private String status;
    
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
     * 学生信息 (非数据库字段, 当 userType 为 Student 时)
     */
    @TableField(exist = false)
    private Student studentInfo;

    /**
     * 教师信息 (非数据库字段, 当 userType 为 Teacher 时)
     */
    @TableField(exist = false)
    private Teacher teacherInfo;

    // --- UserDetails 实现 --- 

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 根据 userType 返回对应的角色权限
        String roleName = null;
        if ("Admin".equals(this.userType)) {
            roleName = "ROLE_ADMIN";
        } else if ("Student".equals(this.userType)) {
            roleName = "ROLE_STUDENT";
        } else if ("Teacher".equals(this.userType)) {
            roleName = "ROLE_TEACHER";
        }

        if (roleName != null) {
            return Collections.singletonList(new SimpleGrantedAuthority(roleName));
        }
        return Collections.emptyList(); // 或者根据 roles 字段返回更复杂的权限
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 账户永不过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 账户永不锁定 (可以根据需要添加锁定逻辑)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 凭证永不过期
    }

    @Override
    public boolean isEnabled() {
        // 根据 status 字段判断账户是否启用
        return "Active".equalsIgnoreCase(this.status);
    }
}