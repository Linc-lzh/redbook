package com.post.post.mapper;

import com.post.post.model.PostContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostContentMapper {
    // 根据id查询帖子内容
    PostContent findById(@Param("id") int id);

    // 创建帖子内容
    void createPostContent(PostContent postContent);

    // 更新帖子内容
    void updatePostContent(PostContent postContent);

    // 删除帖子内容
    void deletePostContent(@Param("id") int id);

    // 其他自定义的查询方法...
}
