package com.campus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Notice;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知公告数据访问接口
 */
@Repository
public interface NoticeDao extends BaseMapper<Notice> {
    
    /**
     * 根据ID查询通知
     * @param id 通知ID
     * @return 通知对象
     */
    Notice selectNoticeById(Long id);
    
    /**
     * 获取所有通知
     * @return 通知列表
     */
    List<Notice> selectAllNotices();
    
    /**
     * 根据类型查询通知
     * @param noticeType 通知类型
     * @return 通知列表
     */
    List<Notice> selectNoticesByType(Integer noticeType);
    
    /**
     * 根据状态查询通知
     * @param status 通知状态
     * @return 通知列表
     */
    List<Notice> selectNoticesByStatus(Integer status);
    
    /**
     * 获取最近的通知
     * @param limit 数量限制
     * @return 通知列表
     */
    List<Notice> selectRecentNotices(@Param("limit") Integer limit);
    
    /**
     * 获取置顶通知
     * @return 通知列表
     */
    List<Notice> selectTopNotices();
    
    /**
     * 根据发布者ID查询通知
     * @param publisherId 发布者ID
     * @return 通知列表
     */
    List<Notice> selectNoticesByPublisherId(Long publisherId);
    
    /**
     * 插入通知
     * @param notice 通知对象
     * @return 影响行数
     */
    int insertNotice(Notice notice);
    
    /**
     * 更新通知
     * @param notice 通知对象
     * @return 影响行数
     */
    int updateNotice(Notice notice);
    
    /**
     * 更新通知状态
     * @param id 通知ID
     * @param status 通知状态
     * @return 影响行数
     */
    int updateNoticeStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 更新阅读次数
     * @param id 通知ID
     * @return 影响行数
     */
    int incrementViewCount(Long id);
    
    /**
     * 删除通知
     * @param id 通知ID
     * @return 影响行数
     */
    int deleteNotice(Long id);
    
    /**
     * 批量删除通知
     * @param ids ID数组
     * @return 影响行数
     */
    int batchDeleteNotices(@Param("ids") Long[] ids);
} 