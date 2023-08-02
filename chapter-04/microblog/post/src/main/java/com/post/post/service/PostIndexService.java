package com.post.post.service;

import com.post.post.model.PostIndex;

import java.util.List;

public interface PostIndexService {
    PostIndex getPostIndexById(int id);

    void createPostIndex(PostIndex postIndex);

    void updatePostIndex(PostIndex postIndex);

    void deletePostIndex(int id);

    /**
     * 分页查询
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    List<PostIndex> getPostIndexByUserId(int userId, int page, int pageSize);

    int getPostIndexCountByUserId(int userId);

    /**
     * 判断用户是否给一个帖子点赞过
     * @param userId
     * @param postId
     * @return
     */
    boolean isPostLikedByUser(int userId, int postId , int likeCount);

    /**
     * 判断用户是否给多个帖子点赞过
     * @param userId
     * @param postId
     * @return
     */
    List<Integer> isPostLikedByUser(int userId, List<Object>postId , int likeCount);
}

