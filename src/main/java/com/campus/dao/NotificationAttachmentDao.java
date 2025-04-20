package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.NotificationAttachment;
import org.springframework.stereotype.Repository;

/**
 * 通知附件关联 数据访问接口
 */
@Repository
public interface NotificationAttachmentDao extends BaseMapper<NotificationAttachment> {
    // 继承 BaseMapper 已包含基本的 CRUD 方法
    // 如果需要特殊查询，可以在此添加，并在对应的 Mapper XML 中实现
} 