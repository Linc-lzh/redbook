package com.post.post.service;

import com.post.post.model.PostContent;

public interface PostContentService {
    PostContent getPostContentById(int id);

    void createPostContent(PostContent postContent);

    void updatePostContent(PostContent postContent);

    void deletePostContent(int id);

    // 其他自定义的业务方法...
}
