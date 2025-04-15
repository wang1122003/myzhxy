package com.campus.service.impl;

import com.campus.dao.NoticeDao;
import com.campus.entity.Notice;
import com.campus.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Date;

/**
 * 通知公告服务实现类
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeDao noticeDao;

    @Override
    public Notice getNoticeById(Long id) {
        return noticeDao.selectNoticeById(id);
    }

    @Override
    public List<Notice> getAllNotices() {
        return noticeDao.selectAllNotices();
    }

    @Override
    public List<Notice> getNoticesByType(Integer noticeType) {
        return noticeDao.selectNoticesByType(noticeType);
    }

    @Override
    public List<Notice> getNoticesByStatus(Integer status) {
        return noticeDao.selectNoticesByStatus(status);
    }

    @Override
    public List<Notice> getRecentNotices(Integer limit) {
        return noticeDao.selectRecentNotices(limit);
    }

    @Override
    public List<Notice> getTopNotices() {
        return noticeDao.selectTopNotices();
    }

    @Override
    public List<Notice> getNoticesByPublisherId(Long publisherId) {
        return noticeDao.selectNoticesByPublisherId(publisherId);
    }

    @Override
    @Transactional
    public boolean addNotice(Notice notice) {
        if (notice == null) {
            return false;
        }
        
        // 设置初始值
        if (notice.getStatus() == null) {
            notice.setStatus(0); // 默认为草稿状态
        }
        if (notice.getIsTop() == null) {
            notice.setIsTop(0); // 默认不置顶
        }
        if (notice.getViewCount() == null) {
            notice.setViewCount(0); // 默认阅读次数为0
        }
        
        // 设置时间
        Date now = new Date();
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        
        return noticeDao.insertNotice(notice) > 0;
    }

    @Override
    @Transactional
    public boolean updateNotice(Notice notice) {
        if (notice == null || notice.getId() == null) {
            return false;
        }
        
        // 设置更新时间
        notice.setUpdateTime(new Date());
        
        return noticeDao.updateNotice(notice) > 0;
    }

    @Override
    @Transactional
    public boolean updateNoticeStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        
        return noticeDao.updateNoticeStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        if (id == null) {
            return false;
        }
        
        return noticeDao.incrementViewCount(id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteNotice(Long id) {
        if (id == null) {
            return false;
        }
        
        return noticeDao.deleteNotice(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteNotices(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        
        return noticeDao.batchDeleteNotices(ids) > 0;
    }
} 