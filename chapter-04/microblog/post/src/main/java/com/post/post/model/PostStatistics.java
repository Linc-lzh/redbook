package com.post.post.model;

/**
 * 帖子的各种计数
 */
public class PostStatistics {
    /**
     * 评论数
     */
    private int commentCount;

    /**
     * 转发数
     */
    private int repostCount;

    /**
     * 点赞数
     */
    private int likeCount;

    /**
     * 浏览数
     */
    private int viewCount;

    // 默认构造函数
    public PostStatistics() {
    }

    // Getters and Setters

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(int repostCount) {
        this.repostCount = repostCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}

