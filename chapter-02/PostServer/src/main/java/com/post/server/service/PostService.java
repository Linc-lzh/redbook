package com.post.server.service;

import com.post.server.dto.PostDTO;
import com.post.server.model.PostIndex;

import java.util.List;

public interface PostService {

    /**
     * 新增文章
     *
     * @param postDTO 文章对象
     * @return 插入结果
     */
    int insertPostIndex(PostDTO postDTO);

    /**
     * 查询帖子的内容
     * @param postId
     * @return
     */
    PostDTO getPostById(Long postId);

    /**
     * 根据多个帖子ID获取帖子列表
     *
     * @param postIds 帖子ID列表
     * @return 帖子列表
     */
    List<PostDTO> getPostsByIds(List<Long> postIds);
}
