package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.enums.UserStatus;
import com.campus.enums.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements UserDetails {

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
     * 出生日期
     */
    @TableField("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 用户类型 (Student, Teacher, Admin)
     */
    @TableField(value = "user_type")
    private UserType userType;
    /**
     * 状态 (Active, Inactive)
     */
    @TableField(value = "status")
    private UserStatus status;
    /**
     * 学工号
     */
    @TableField("user_no")
    private String userNo;
    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    /**
     * 记住我选项 (非数据库字段)
     */
    @TableField(exist = false)
    private Boolean remember;

    public Long getId() {
        return this.id;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    // --- UserDetails 实现 --- 

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 根据 userType 返回对应的角色权限
        String roleName = null;
        if (this.userType == UserType.ADMIN) {
            roleName = "ROLE_ADMIN";
        } else if (this.userType == UserType.STUDENT) {
            roleName = "ROLE_STUDENT";
        } else if (this.userType == UserType.TEACHER) {
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
        return this.status == UserStatus.ACTIVE;
    }
}