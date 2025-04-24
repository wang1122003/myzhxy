package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dao.CommentDao;
import com.campus.dao.UserDao;
import com.campus.dto.PageResult;
import com.campus.entity.Comment;
import com.campus.entity.User;
import com.campus.service.CommentService;
import com.campus.service.PostService;
import com.campus.vo.CommentVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 论坛评论服务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentDao, Comment> implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Comment getCommentById(Long id) {
        Comment comment = commentDao.getCommentById(id);
        if (comment != null && StringUtils.isNotBlank(comment.getReplies())) {
            parseAndSetReplies(comment);
        }
        return comment;
    }

    @Override
    public List<Comment> getAllComments() {
        List<Comment> comments = commentDao.getAllComments();
        comments.forEach(this::parseAndSetReplies);
        return comments;
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentDao.getCommentsByPostId(postId);
        comments.forEach(this::parseAndSetReplies);
        return comments;
    }

    @Override
    public List<Comment> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentDao.getCommentsByAuthorId(userId, null);
        comments.forEach(this::parseAndSetReplies);
        return comments;
    }

    @Override
    public List<Comment> getCommentsByParentId(Long parentId) {
        List<Comment> comments = commentDao.getCommentsByParentId(parentId);
        comments.forEach(this::parseAndSetReplies);
        return comments;
    }

    @Override
    public List<Comment> getRootCommentsByPostId(Long postId) {
        List<Comment> comments = commentDao.getRootCommentsByPostId(postId);
        comments.forEach(this::parseAndSetReplies);
        return comments;
    }

    /**
     * 处理评论回复，将JSON字符串转换为对象列表
     */
    private void parseAndSetReplies(Comment comment) {
        if (comment != null && StringUtils.isNotBlank(comment.getReplies())) {
            try {
                List<Comment> replies = objectMapper.readValue(comment.getReplies(), new TypeReference<List<Comment>>() {
                });
                // comment.setRepliesList(replies); // 实体类中已无 setRepliesList
            } catch (JsonProcessingException e) {
                log.error("解析评论回复失败, commentId: {}", comment.getId(), e);
                // comment.setRepliesList(Collections.emptyList()); // 实体类中已无 setRepliesList
            }
        }
    }

    @Override
    @Transactional
    public boolean addComment(Comment comment) {
        // 设置初始值
        Date now = new Date();
        comment.setCreationTime(now);
        comment.setUpdateTime(now);
        
        // 初始化点赞次数
        comment.setLikeCount(0);
        
        // 如果没有设置状态，默认为正常
        if (comment.getStatus() == null) {
            comment.setStatus("Active");
        }

        boolean result = commentDao.addComment(comment) > 0;
        
        // 增加帖子评论次数
        if (result && comment.getPostId() != null) {
            postService.incrementCommentCount(comment.getPostId());
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean updateComment(Comment comment) {
        // 设置更新时间
        comment.setUpdateTime(new Date());

        return commentDao.updateComment(comment) > 0;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long id) {
        return commentDao.deleteComment(id) > 0;
    }

    @Override
    @Transactional
    public boolean batchDeleteComments(Long[] ids) {
        return commentDao.batchDeleteComments(ids) > 0;
    }

    @Override
    @Transactional
    public boolean deleteCommentsByPostId(Long postId) {
        return commentDao.deleteCommentsByPostId(postId) > 0;
    }

    @Override
    @Transactional
    public boolean updateCommentStatus(Long id, String status) {
        return commentDao.updateCommentStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean incrementLikeCount(Long id) {
        return commentDao.incrementLikeCount(id) > 0;
    }

    @Override
    @Transactional
    public boolean decrementLikeCount(Long id) {
        return commentDao.decrementLikeCount(id) > 0;
    }

    @Override
    @Transactional
    public boolean likeComment(Long commentId) {
        return incrementLikeCount(commentId);
    }

    @Override
    @Transactional
    public boolean cancelLikeComment(Long commentId) {
        // Implement logic to decrement like count by calling decrementLikeCount
        return decrementLikeCount(commentId);
    }

    @Override
    public PageResult<CommentVO> getAllCommentsPaginated(int pageNo, int pageSize, Long postId, Long authorId, String keyword) {
        // 创建分页对象
        IPage<Comment> page = new Page<>(pageNo, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        // 添加筛选条件
        if (postId != null) {
            wrapper.eq(Comment::getPostId, postId);
        }
        if (authorId != null) {
            wrapper.eq(Comment::getUserId, authorId);
        }
        if (StringUtils.isNotBlank(keyword)) {
            // 假设 keyword 搜索评论内容
            wrapper.like(Comment::getContent, keyword);
        }

        // 添加排序，例如按创建时间降序
        wrapper.orderByDesc(Comment::getCreationTime);

        // 执行分页查询
        IPage<Comment> resultPage = commentDao.selectPage(page, wrapper);

        // 获取原始评论列表
        List<Comment> comments = resultPage.getRecords();
        List<CommentVO> commentVOList;

        if (comments.isEmpty()) {
            commentVOList = Collections.emptyList();
        } else {
            // 提取作者ID列表
            List<Long> authorIds = comments.stream()
                    .map(Comment::getUserId)
                    .filter(id -> id != null) // 过滤掉可能存在的 null ID
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询作者信息
            Map<Long, User> userMap = Collections.emptyMap();
            if (!authorIds.isEmpty()) {
                List<User> users = userDao.selectBatchIds(authorIds);
                userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            }

            // 转换成 CommentVO 列表并填充作者信息
            Map<Long, User> finalUserMap = userMap; // Effectively final map for lambda
            commentVOList = comments.stream().map(comment -> {
                CommentVO commentVO = new CommentVO();
                BeanUtils.copyProperties(comment, commentVO); // 复制基础属性

                User author = finalUserMap.get(comment.getUserId());
                if (author != null) {
                    commentVO.setUsername(author.getUsername());
                    commentVO.setAvatar(author.getAvatarUrl()); // 使用 getAvatarUrl()
                } else {
                    // 处理找不到作者的情况，可以设置默认值或记录日志
                    commentVO.setUsername("未知用户");
                    // commentVO.setAvatar(DEFAULT_AVATAR_URL);
                }
                return commentVO;
            }).collect(Collectors.toList());
        }

        // 封装成分页结果对象
        return new PageResult<CommentVO>(
                resultPage.getTotal(),
                commentVOList,
                resultPage.getCurrent(),
                resultPage.getSize()
        );
    }

    @Override
    public List<Comment> getReplies(Long parentId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, parentId)
                .orderByAsc(Comment::getCreationTime); // Order replies by time
        return commentDao.selectList(wrapper);
    }

    @Override
    public Page<Map<String, Object>> getAllCommentsManaged(int page, int size, Integer status, String keyword) {
        // TODO: Implement actual logic to fetch all comments with filtering and pagination
        // Placeholder implementation:
        log.warn("getAllCommentsManaged is not fully implemented yet."); 
        Page<Map<String, Object>> emptyPage = new Page<>(page, size);
        emptyPage.setRecords(new ArrayList<>());
        emptyPage.setTotal(0);
        return emptyPage;
    }

    @Override
    public Page<Map<String, Object>> getCommentsByUserIdWithPostInfo(Long userId, int page, int size) {
        // TODO: Implement actual logic to fetch user comments with post info
        // Placeholder implementation:
        log.warn("getCommentsByUserIdWithPostInfo is not fully implemented yet.");
        Page<Map<String, Object>> emptyPage = new Page<>(page, size);
        emptyPage.setRecords(new ArrayList<>());
        emptyPage.setTotal(0);
        return emptyPage;
    }

    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        // Basic implementation: Set creation time and save
        // TODO: Add logic to update post comment count
        comment.setCreationTime(new Date()); 
        boolean saved = this.save(comment);
        if (saved && comment.getParentId() == null) {
             // Increment post comment count if it's a top-level comment
             postService.incrementCommentCount(comment.getPostId());
         }
        return saved ? comment : null;
    }
}