package com.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.dto.NoticeDTO;
import com.campus.entity.Notice;

import java.util.List;

/**
 * 通知公告服务接口
 */
public interface NoticeService {
    
    /**
     * 根据ID获取通知 (不一定包含附件)
     * @param id 通知ID
     * @return 通知对象
     */
    Notice getNoticeById(Long id);

    /**
     * 根据ID获取通知 (包含附件列表)
     * @param id 通知ID
     * @return 通知对象 (包含附件信息)
     */
    Notice getNoticeWithAttachments(Long id);

    /**
     * 查询所有通知
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
     * 添加通知 (使用 DTO)
     * @param noticeDto 通知信息 DTO
     * @return 是否成功
     */
    boolean addNotice(NoticeDTO noticeDto);
    
    /**
     * 更新通知 (使用 DTO)
     * @param noticeDto 通知信息 DTO
     * @return 是否成功
     */
    boolean updateNotice(NoticeDTO noticeDto);
    
    /**
     * 更新通知状态
     * @param id 通知ID
     * @param status 通知状态
     * @return 是否成功
     */
    boolean updateNoticeStatus(Long id, Integer status);
    
    /**
     * 删除通知
     * @param id 通知ID
     * @return 是否成功
     */
    boolean deleteNotice(Long id);
    
    /**
     * 批量删除通知
     * @param ids 通知ID数组
     * @return 是否成功
     */
    boolean batchDeleteNotices(Long[] ids);

    /**
     * 增加通知阅读次数
     *
     * @param id 通知ID
     */
    void incrementViewCount(Long id);

    /**
     * 获取通知总数
     *
     * @return 通知数量
     */
    int getNoticeCount();

    /**
     * 分页查询通知
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 通知分页结果 (IPage)
     */
    IPage<Notice> getNoticesByPage(int pageNum, int pageSize);

    /**
     * 搜索通知
     *
     * @param keyword 关键词
     * @return 通知列表
     */
    List<Notice> searchNotices(String keyword);

    /**
     * 分页搜索通知
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 通知分页结果 (IPage)
     */
    IPage<Notice> searchNoticesPaged(String keyword, int pageNum, int pageSize);
} 