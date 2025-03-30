package com.comment.web.request;

import java.util.List;

public class CommentIndicesRequest {
    private List<Long> commentIds;
    private Long objId;
    private Integer parent;

    // Getters and Setters
    public List<Long> getCommentIds() {
        return commentIds;
    }

    public void setCommentIds(List<Long> commentIds) {
        this.commentIds = commentIds;
    }

    public Long getObjId() {
        return objId;
    }

    public void setObjId(Long objId) {
        this.objId = objId;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }
}
