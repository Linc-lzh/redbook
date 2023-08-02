package com.post.post.service;

import com.post.post.model.PostIndex;
import com.post.post.model.PostLike;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PostLikeService {

    PostLike getPostLikeById(int id);

    /**
     * 创建点赞记录
     * @param postLike
     */
    void createPostLike(PostLike postLike);

    void updatePostLike(PostLike postLike);

    void deletePostLike(int id);

    /**
     * 根据帖子id查询所有点赞的用户
     * @param postId
     * @return
     */
    Set<Integer> selectUserIdsByPostId(Integer postId , long start, long end);

    /**
     * 根据用户id查询所有点赞的帖子
     * @param userId
     * @return
     */
    Map<String, Object> selectPostIdsByUserId(Integer userId);

    /**
     * 查询所有postIndex
     * @param postId
     * @return
     */
    List<PostIndex> selectPostIndex(List<Integer> postId);
}

