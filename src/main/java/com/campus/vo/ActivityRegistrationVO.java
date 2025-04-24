package com.campus.vo;

import com.campus.entity.ActivityRegistration;
import com.campus.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 活动报名视图对象，包含报名者信息
 */
@Data
@NoArgsConstructor
public class ActivityRegistrationVO {

    private Long id;
    private Long activityId;
    private Long userId;
    private Date registrationTime;
    private Integer status;

    // 用户信息
    private String username;
    private String realName;
    private String avatarUrl;
    private String phone; // 可选：添加其他用户字段，如手机号/邮箱
    private String email;

    /**
     * 构造函数，用于合并报名信息和用户信息
     *
     * @param registration 报名记录实体
     * @param user         报名用户实体
     */
    public ActivityRegistrationVO(ActivityRegistration registration, User user) {
        if (registration != null) {
            BeanUtils.copyProperties(registration, this);
        }
        if (user != null) {
            this.username = user.getUsername();
            this.realName = user.getRealName();
            this.avatarUrl = user.getAvatarUrl();
            this.phone = user.getPhone();
            this.email = user.getEmail();
        }
    }
}