package com.post.server.mapper;

import com.post.server.model.PostStats;

import java.util.List;

public interface PostStatsMapper {

    /**
     * 插入帖子统计信息
     * @param postStats 帖子统计信息对象
     * @return 插入的行数
     */
    int insertPostStats(PostStats postStats);

    /**
     * 更新帖子统计信息
     * @param postStats 更新后的帖子统计信息对象
     * @return 更新的行数
     */
    int updatePostStats(PostStats postStats);

    /**
     * 根据帖子ID查询统计信息
     * @param postId 帖子ID
     * @return 查询到的帖子统计信息对象
     */
    PostStats getPostStats(Long postId);

    /**
     * 批量插入帖子统计信息
     * @param postStatsList 帖子统计信息列表
     * @return 插入的行数
     */
    int batchInsertPostStats(List<PostStats> postStatsList);
}
