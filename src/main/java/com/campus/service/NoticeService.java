package com.campus.service;

import java.util.List;

import com.campus.entity.Notice;

/**
 * 通知公告服务接口
 */
public interface NoticeService {
    
    /**
     * 根据ID获取通知
     * @param id 通知ID
     * @return 通知对象
     */
    Notice getNoticeById(Long id);
    
    /**
     * 获取所有通知
     * @return 通知列表
     */
    List<Notice> getAllNotices();
    
    /**
     * 根据类型获取通知
     * @param noticeType 通知类型
     * @return 通知列表
     */
    List<Notice> getNoticesByType(Integer noticeType);
    
    /**
     * 根据状态获取通知
     * @param status 通知状态
     * @return 通知列表
     */
    List<Notice> getNoticesByStatus(Integer status);
    
    /**
     * 获取最近的通知
     * @param limit 数量限制
     * @return 通知列表
     */
    List<Notice> getRecentNotices(Integer limit);
    
    /**
     * 获取置顶通知
     * @return 通知列表
     */
    List<Notice> getTopNotices();
    
    /**
     * 根据发布者ID获取通知
     * @param publisherId 发布者ID
     * @return 通知列表
     */
    List<Notice> getNoticesByPublisherId(Long publisherId);
    
    /**
     * 添加通知
     * @param notice 通知对象
     * @return 是否成功
     */
    boolean addNotice(Notice notice);
    
    /**
     * 更新通知
     * @param notice 通知对象
     * @return 是否成功
     */
    boolean updateNotice(Notice notice);
    
    /**
     * 更新通知状态
     * @param id 通知ID
     * @param status 通知状态
     * @return 是否成功
     */
    boolean updateNoticeStatus(Long id, Integer status);
    
    /**
     * 增加阅读次数
     * @param id 通知ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);
    
    /**
     * 删除通知
     * @param id 通知ID
     * @return 是否成功
     */
    boolean deleteNotice(Long id);
    
    /**
     * 批量删除通知
     * @param ids ID数组
     * @return 是否成功
     */
    boolean batchDeleteNotices(Long[] ids);
} 