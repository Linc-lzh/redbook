package com.post.server.mapper;

import com.post.server.model.PostLike;
import org.apache.ibatis.annotations.*;

public interface PostLikeMapper {

    /**
     * 插入点赞记录
     * @param postLike 点赞信息对象
     * @return 插入的行数
     */
    int insertPostLike(PostLike postLike);

    /**
     * 根据ID删除点赞记录
     * @param id 点赞记录的ID
     * @return 删除的行数
     */
    int deletePostLike(Long id);

    /**
     * 更新点赞记录
     * @param postLike 更新后的点赞信息对象
     * @return 更新的行数
     */
    int updatePostLike(PostLike postLike);

    /**
     * 根据ID查询点赞记录
     * @param id 点赞记录的ID
     * @return 查询到的点赞信息对象
     */
    PostLike getPostLike(Long id);
}

