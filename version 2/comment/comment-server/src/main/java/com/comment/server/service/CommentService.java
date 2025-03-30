package com.comment.server.service;

import com.comment.server.dto.CommentDTO;

import java.util.List;
import java.util.Set;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 根据帖子ID查询所有评论ID
     * @param objId 帖子ID
     * @return 评论ID列表
     */
    Set<Long> findAllCommentIdsByObjId(Long objId , Integer parent);

    /**
     * 根据父评论ID查询所有子评论ID
     * @param parentId 父评论ID
     * @return 子评论ID列表
     */
    List<Long> findAllChildCommentIdsByParentId(Long parentId);

    /**
     * 根据评论ID列表查询评论索引
     * @param commentIds 评论ID列表
     * @return 评论索引列表
     */
    List<CommentDTO> findCommentIndicesByCommentIds(List<Long> commentIds , Long objId, Integer parent);

    /**
     * 检查并缓存评论ID集合到Redis中。
     * 如果Redis中不存在对应的评论ID集合，则从数据库查询并缓存到Redis中。
     *
     * @param objId 对象ID
     * @param parent 父评论ID
     */
    void cacheCommentIdsIfNeeded(Long objId, Integer parent);

    /**
     * 查询所有的ids然后保存到redis中
     * @param ids
     * @return
     */
    List<CommentDTO> findCommentsByIds(List<Long> ids);

    /**
     * 根据评论ID列表查询对应的点赞数
     * @param commentIds 评论ID列表
     * @return 每个评论ID对应的点赞数列表
     */
    List<Integer> findCommentLikesByIds(List<Long> commentIds, Long objId, Integer parent);

    /**
     * 根据objId和parent查询内容
     * @param objId
     * @param parent
     */
    void findByObjIdAndParent(Long objId, Integer parent);

    /**
     * 根据userId和commentIds查询布隆过滤器判断用户是否点赞过
     * @param userId
     * @param commentIds
     * @return
     */
    List<Boolean> checkLikes(Long userId, List<Long> commentIds);

    /**
     * 根据用户id查询所有的点赞过的评论id，set到布隆过滤器当中
     * @param memberId
     */
    void addLikedCommentIdsToBloomFilter(Long memberId);
}
