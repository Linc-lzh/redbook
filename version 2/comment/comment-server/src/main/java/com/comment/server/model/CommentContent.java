package com.comment.server.model;
import java.util.Date;

public class CommentContent {

    /** commentId，评论的唯一标识 */
    private Long commentId;

    /** 内容，存储评论的文本内容 */
    private String message;

    /** 创建时间，记录评论的创建时间 */
    private Date createTime;

    /** 修改时间，记录评论的最后修改时间 */
    private Date updateTime;

    // Getters and Setters

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
