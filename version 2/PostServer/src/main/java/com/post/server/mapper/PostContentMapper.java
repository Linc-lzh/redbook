package com.post.server.mapper;

import com.post.server.model.PostContent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostContentMapper {

    /**
     * 插入帖子内容
     * @param postContent 帖子内容对象
     * @return 插入的行数
     */
    int insertPostContent(PostContent postContent);

    /**
     * 根据帖子ID删除帖子内容
     * @param postId 帖子ID
     * @return 删除的行数
     */
    int deletePostContent(Long postId);

    /**
     * 更新帖子内容
     * @param postContent 更新后的帖子内容对象
     * @return 更新的行数
     */
    int updatePostContent(PostContent postContent);

    /**
     * 根据帖子ID查询帖子内容
     * @param postId 帖子ID
     * @return 查询到的帖子内容对象
     */
    PostContent getPostContent(Long postId);
}

