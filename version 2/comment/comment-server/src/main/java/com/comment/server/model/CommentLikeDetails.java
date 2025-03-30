package com.comment.server.model;

import java.util.Date;

public class CommentLikeDetails {

    /** 自增主键ID */
    private Long id;

    /** 被点赞的评论ID */
    private Long commentId;

    /** 点赞者ID */
    private Long memberId;

    /** 点赞时间 */
    private Date createTime;

    /** 是否取消点赞 */
    private Boolean isCancelled;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getCancelled() {
        return isCancelled;
    }

    public void setCancelled(Boolean cancelled) {
        isCancelled = cancelled;
    }
}
