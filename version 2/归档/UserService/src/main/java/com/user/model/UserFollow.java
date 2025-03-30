package com.user.model;

import java.util.Date;

public class UserFollow {
    private Integer id; //主键
    private Integer followerId; //关注者的用户ID (粉丝)
    private Integer userId;
    private Integer followingId; //被关注者的用户ID
    private Date followDate; //关注日期
    private Byte status; //关注状态，0表示正在关注，1表示已取关

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    // followerId
    public Integer getFollowerId() {
        return followerId;
    }
    public void setFollowerId(Integer followerId) {
        this.followerId = followerId;
    }

    // followingId
    public Integer getFollowingId() {
        return followingId;
    }
    public void setFollowingId(Integer followingId) {
        this.followingId = followingId;
    }

    // followDate
    public Date getFollowDate() {
        return followDate;
    }
    public void setFollowDate(Date followDate) {
        this.followDate = followDate;
    }

    // status
    public Byte getStatus() {
        return status;
    }
    public void setStatus(Byte status) {
        this.status = status;
    }
}

