package com.comment.server.model;

import java.util.Date;

public class PostLikeSummary {

    /** 自增主键ID */
    private Long id;

    /** 对象ID（帖子ID） */
    private Long objId;

    /** 对象类型(1:帖子，2:视频，3:商品） */
    private Integer type;

    /** 点赞总数 */
    private Long likeCount;

    /** 最后更新时间 */
    private Date updateTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObjId() {
        return objId;
    }

    public void setObjId(Long objId) {
        this.objId = objId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
