package com.campus.service;

// import com.baomidou.mybatisplus.core.metadata.IPage;
// import com.baomidou.mybatisplus.extension.service.IService;
// import com.campus.entity.Comment; // Comment class is missing

// import java.util.List;
// import java.util.Map;

/**
 * 评论服务接口
 * TODO: [评论功能] - Interface commented out due to missing Comment entity.
 */
/*
public interface CommentService extends IService<Comment> {

    // 根据帖子ID获取评论（分页）
    IPage<Comment> getCommentsByPostId(Long postId, int page, int size);

    // 根据用户ID获取评论（分页）
    IPage<Comment> getCommentsByUserId(Long userId, int page, int size);

    // 获取所有评论（分页）
    IPage<Comment> getAllComments(int page, int size);

    // 添加评论
    boolean addComment(Comment comment);

    // 更新评论
    boolean updateComment(Comment comment);

    // 删除评论
    boolean deleteComment(Long commentId);

    // 批量删除评论
    boolean batchDeleteComments(List<Long> commentIds);

    // 点赞评论
    boolean likeComment(Long commentId, Long userId);

    // 取消点赞评论
    boolean unlikeComment(Long commentId, Long userId);

    // 获取评论详情（可能包含点赞信息等）
    Comment getCommentDetail(Long commentId);

    // 搜索评论
    IPage<Comment> searchComments(String keyword, int page, int size);

    // 更新评论状态
    boolean updateCommentStatus(Long commentId, String status);

    // 获取评论总数
    long getTotalCommentCount();

    // 获取指定帖子的评论总数
    long getCommentCountByPostId(Long postId);

    // 获取指定用户的评论总数
    long getCommentCountByUserId(Long userId);

    // 获取热门评论（例如按点赞数排序）
    List<Comment> getHotComments(Long postId, int limit);

    // 获取最新评论
    List<Comment> getRecentComments(int limit);

    // 获取父评论下的子评论
    List<Comment> getReplies(Long parentCommentId);

    // Map形式的CRUD（如果需要）
    Map<String, Object> getMapById(Long id);
    List<Map<String, Object>> getMapsByPostId(Long postId);
    IPage<Map<String, Object>> pageQueryMaps(Long current, Long size, Map<String, Object> params);
    boolean saveByMap(Map<String, Object> commentMap);
    boolean updateByMap(Map<String, Object> commentMap);
}
*/