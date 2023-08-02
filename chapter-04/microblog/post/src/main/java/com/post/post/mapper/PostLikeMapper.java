package com.post.post.mapper;

import com.post.post.model.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostLikeMapper {

    // 根据id查询点赞记录
    PostLike findById(@Param("id") int id);

    // 创建点赞记录
    void createPostLike(PostLike postLike);

    // 更新点赞记录
    void updatePostLike(PostLike postLike);

    // 删除点赞记录
    void deletePostLike(@Param("id") int id);

    /**
     * 根据帖子id查询所有点赞的用户
     * @param postId
     * @return
     */
    List<Integer> selectUserIdsByPostId(@Param("postId") Integer postId);

    /**
     * 根据用户id查询所有点赞的帖子
     * @param userId
     * @return
     */
    List<Integer> selectPostIdsByUserId(@Param("userId") Integer userId);

    /**
     * 根据uid查询最近点赞的帖子
     * @param uid
     * @param limit
     * @return
     */
    List<PostLike> selectByUserId(@Param("uid") int uid, @Param("limit") int limit);

    /**
     * 判断用户是否给帖子点赞过
     * @param userId
     * @param postId
     * @return
     */
    int countByUserIdAndPostId(@Param("userId") int userId, @Param("postId") int postId);

    /**
     * 查询user点赞帖子集合
     * @param userId
     * @param postIds
     * @return
     */
    List<Integer> selectPostIdsByUserIdAndPostIds(@Param("userId") int userId, @Param("postIds") List<Integer> postIds);
}

