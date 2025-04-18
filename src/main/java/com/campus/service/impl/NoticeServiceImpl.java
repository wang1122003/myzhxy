package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.NoticeDao;
import com.campus.dto.NoticeDTO;
import com.campus.entity.Notice;
import com.campus.service.FileService;
import com.campus.service.NoticeService;
import com.campus.utils.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 通知公告服务实现类
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeDao, Notice> implements NoticeService {

    private static final Logger log = LoggerFactory.getLogger(NoticeServiceImpl.class);

    @Autowired
    private NoticeDao noticeDao;
    // Get Base Upload Directory from FileUtils
    private final String baseUploadDir = FileUtils.getBaseUploadDir();
    // Inject ObjectMapper for JSON processing
    @Autowired
    private ObjectMapper objectMapper;
    // Inject FileService to handle physical file deletion
    @Autowired
    private FileService fileService;
    // Inject URL prefix to parse file paths from URLs
    @Value("${file.upload.url.prefix:/uploads}") // Default to /uploads if not set
    private String uploadUrlPrefix;

    @Override
    public Notice getNoticeById(Long id) {
        // This method returns the raw entity. Consider if a DTO version is needed.
        return this.getById(id);
    }

    @Override
    public Notice getNoticeWithAttachments(Long id) {
        // This method currently returns the raw entity including the JSON string.
        // If you need a DTO with parsed attachments, create a separate method
        // or modify this one to return NoticeDTO.
        Notice notice = this.getById(id);
        // You might want to parse the attachmentsJson here if returning a DTO
        return notice;
    }

    @Override
    public List<Notice> getAllNotices() {
        return this.list();
    }

    @Override
    public List<Notice> getNoticesByType(Integer noticeType) {
        return this.list(new LambdaQueryWrapper<Notice>().eq(Notice::getNoticeType, noticeType));
    }

    @Override
    public List<Notice> getNoticesByStatus(Integer status) {
        return this.list(new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, status));
    }

    @Override
    public List<Notice> getRecentNotices(Integer limit) {
        // 创建分页对象，查询第一页，每页 limit 条
        Page<Notice> page = new Page<>(1, limit);
        // 构建查询条件，按创建时间降序
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getCreateTime);
        // 执行分页查询
        Page<Notice> resultPage = this.page(page, queryWrapper);
        // 返回查询结果记录
        return resultPage.getRecords();
    }

    @Override
    public List<Notice> getTopNotices() {
        return this.list(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getIsTop, 1)
                .orderByDesc(Notice::getUpdateTime) // 或其他排序逻辑
        );
    }

    @Override
    public List<Notice> getNoticesByPublisherId(Long publisherId) {
        return this.list(new LambdaQueryWrapper<Notice>().eq(Notice::getPublisherId, publisherId));
    }

    @Override
    @Transactional
    public boolean addNotice(NoticeDTO noticeDto) {
        if (noticeDto == null) {
            return false;
        }
        Notice notice = convertDtoToEntityForSave(noticeDto);
        return this.save(notice);
    }

    @Override
    @Transactional
    public boolean updateNotice(NoticeDTO noticeDto) {
        if (noticeDto == null || noticeDto.getId() == null) {
            return false;
        }
        Notice noticeToUpdate = convertDtoToEntityForUpdate(noticeDto);
        return this.updateById(noticeToUpdate);
    }

    @Override
    @Transactional
    public boolean updateNoticeStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        Notice notice = new Notice();
        notice.setId(id);
        notice.setStatus(status);
        notice.setUpdateTime(new Date());
        LambdaQueryWrapper<Notice> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(Notice::getId, id);
        return this.update(notice, updateWrapper);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        if (id != null) {
            baseMapper.incrementViewCount(id);
        }
    }

    @Override
    public int getNoticeCount() {
        return (int) this.count();
    }

    @Override
    public IPage<Notice> getNoticesByPage(int pageNum, int pageSize) {
        IPage<Notice> page = new Page<>(pageNum, pageSize);
        return this.page(page);
    }

    @Override
    public List<Notice> searchNotices(String keyword) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Notice::getTitle, keyword).or().like(Notice::getContent, keyword);
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public IPage<Notice> searchNoticesPaged(String keyword, int pageNum, int pageSize) {
        IPage<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Notice::getTitle, keyword).or().like(Notice::getContent, keyword);
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public boolean deleteNotice(Long id) {
        if (id == null) {
            return false;
        }

        Notice notice = this.getById(id);
        List<NoticeDTO.AttachmentInfo> attachmentsToDelete = parseAttachments(notice);

        boolean dbDeleted = this.removeById(id);

        if (dbDeleted && !attachmentsToDelete.isEmpty()) {
            deletePhysicalAttachments(attachmentsToDelete);
        }

        return dbDeleted;
    }

    @Override
    @Transactional
    public boolean batchDeleteNotices(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        List<Long> idList = List.of(ids);

        List<Notice> notices = this.listByIds(idList);
        List<NoticeDTO.AttachmentInfo> allAttachmentsToDelete = new ArrayList<>();
        for (Notice notice : notices) {
            allAttachmentsToDelete.addAll(parseAttachments(notice));
        }

        boolean dbDeleted = this.removeByIds(idList);

        if (dbDeleted && !allAttachmentsToDelete.isEmpty()) {
            deletePhysicalAttachments(allAttachmentsToDelete);
        }

        return dbDeleted;
    }

    private List<NoticeDTO.AttachmentInfo> parseAttachments(Notice notice) {
        if (notice != null && notice.getAttachmentsJson() != null && !notice.getAttachmentsJson().isEmpty()) {
            try {
                return objectMapper.readValue(
                        notice.getAttachmentsJson(),
                        new TypeReference<List<NoticeDTO.AttachmentInfo>>() {
                        }
                );
            } catch (JsonProcessingException e) {
                log.error("Error deserializing attachments JSON for notice ID {} during delete", notice.getId(), e);
            }
        }
        return Collections.emptyList();
    }

    private void deletePhysicalAttachments(List<NoticeDTO.AttachmentInfo> attachments) {
        for (NoticeDTO.AttachmentInfo attachment : attachments) {
            if (attachment.getUrl() != null && attachment.getUrl().startsWith(uploadUrlPrefix)) {
                String pathSuffix = attachment.getUrl().substring(uploadUrlPrefix.length());
                String cleanedBaseDir = baseUploadDir.endsWith("/") ? baseUploadDir : baseUploadDir + "/";
                String relativePath = (cleanedBaseDir + pathSuffix).replace("//", "/");
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                try {
                    log.info("Attempting to delete attachment file relative to workspace: {}", relativePath);
                    boolean deleted = fileService.deleteFile(relativePath);
                    if (!deleted) {
                        log.warn("Failed to delete attachment file or file not found: {}", relativePath);
                    }
                } catch (Exception e) {
                    log.error("Error occurred while calling fileService.deleteFile for path: {}", relativePath, e);
                }
            } else {
                log.warn("Skipping attachment deletion due to invalid or non-local URL: {}", attachment.getUrl());
            }
        }
    }

    private Notice convertDtoToEntityForSave(NoticeDTO noticeDto) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeDto, notice, "attachments", "isTop");

        serializeAttachments(noticeDto, notice);

        notice.setIsTop(Boolean.TRUE.equals(noticeDto.getIsTop()) ? 1 : 0);

        if (notice.getStatus() == null) notice.setStatus(0);
        if (notice.getViewCount() == null) notice.setViewCount(0);
        Date now = new Date();
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        return notice;
    }

    private Notice convertDtoToEntityForUpdate(NoticeDTO noticeDto) {
        Notice noticeToUpdate = new Notice();
        BeanUtils.copyProperties(noticeDto, noticeToUpdate, "attachments", "isTop");

        serializeAttachments(noticeDto, noticeToUpdate);

        noticeToUpdate.setIsTop(Boolean.TRUE.equals(noticeDto.getIsTop()) ? 1 : 0);

        noticeToUpdate.setUpdateTime(new Date());
        return noticeToUpdate;
    }

    private void serializeAttachments(NoticeDTO noticeDto, Notice notice) {
        if (!CollectionUtils.isEmpty(noticeDto.getAttachments())) {
            try {
                String attachmentsJson = objectMapper.writeValueAsString(noticeDto.getAttachments());
                notice.setAttachmentsJson(attachmentsJson);
            } catch (JsonProcessingException e) {
                log.error("Error serializing attachments for notice title/ID: {}", noticeDto.getTitle() != null ? noticeDto.getTitle() : noticeDto.getId(), e);
                throw new RuntimeException("Failed to serialize attachments", e);
            }
        } else {
            notice.setAttachmentsJson(null);
        }
    }

    private NoticeDTO convertToDtoWithAttachments(Notice notice) {
        if (notice == null) return null;
        NoticeDTO dto = new NoticeDTO();
        BeanUtils.copyProperties(notice, dto, "attachments", "isTop");
        dto.setIsTop(notice.getIsTop() != null && notice.getIsTop() == 1);

        if (notice.getAttachmentsJson() != null && !notice.getAttachmentsJson().isEmpty()) {
            try {
                List<NoticeDTO.AttachmentInfo> attachments = objectMapper.readValue(
                        notice.getAttachmentsJson(),
                        new TypeReference<List<NoticeDTO.AttachmentInfo>>() {
                        }
                );
                dto.setAttachments(attachments);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing attachments for notice ID: {}", notice.getId(), e);
                dto.setAttachments(Collections.emptyList());
            }
        } else {
            dto.setAttachments(Collections.emptyList());
        }
        return dto;
    }
} 