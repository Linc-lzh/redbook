package com.post.server.model;

import java.util.Date;

public class PostLike {
    // 主键ID
    private Long id;

    // 帖子ID
    private Integer postId;

    // 点赞用户ID
    private Integer userId;

    // 点赞时间
    private Date likeTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getLikeTime() {
        return likeTime;
    }

    public void setLikeTime(Date likeTime) {
        this.likeTime = likeTime;
    }
}

