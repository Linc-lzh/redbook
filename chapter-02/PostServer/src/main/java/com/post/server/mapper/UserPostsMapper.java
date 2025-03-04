package com.post.server.mapper;

import com.post.server.model.UserPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPostsMapper {

    /**
     * 插入一条用户帖子记录
     *
     * @param userPost 要插入的用户帖子对象
     */
    void insertUserPost(UserPost userPost);

    /**
     * 更新用户帖子记录
     *
     * @param userPost 要更新的用户帖子对象
     */
    void updateUserPost(UserPost userPost);

    /**
     * 根据ID删除用户帖子记录
     *
     * @param id 要删除的帖子ID
     */
    void deleteUserPostById(Long id);

    /**
     * 根据ID查询用户帖子记录
     *
     * @param id 要查询的帖子ID
     * @return 返回查询到的用户帖子记录
     */
    UserPost selectUserPostById(Long id);

    /**
     * 根据用户ID查询所有帖子，并按创建时间降序排序
     *
     * @param userId 要查询的用户ID
     * @return 返回按创建时间降序排序的用户帖子列表
     */
    List<UserPost> findAllPostsByUserIdOrderByCreationTime(@Param("userId") Long userId);

    /**
     * 批量插入或更新用户帖子记录。
     * 如果帖子记录已存在（基于主键或唯一索引），则更新该记录；
     * 如果不存在，插入新的记录。
     *
     * @param userPosts 要插入或更新的用户帖子列表。
     */
    void batchInsertOrUpdate(List<UserPost> userPosts);
}
