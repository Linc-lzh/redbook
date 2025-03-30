package com.post.server.model;

import java.util.Date;

public class UserPost {

    private Long id; // 对应表中的主键ID
    private Long postId; // 对应帖子ID
    private Long userId; // 对应用户ID
    private Date creationTime; // 对应创建时间

    // 构造函数
    public UserPost() {
    }

    // 带参数的构造函数
    public UserPost(Long id, Long postId, Long userId, Date creationTime) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.creationTime = creationTime;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    // toString方法，用于打印对象信息
    @Override
    public String toString() {
        return "UserPost{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", creationTime=" + creationTime +
                '}';
    }
}

