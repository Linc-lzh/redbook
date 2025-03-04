package com.user.dto;

import java.util.List;

public class UserRequest {

    private List<Integer> followerIds;
    private Integer userId;

    // 构造函数、getters 和 setters

    public UserRequest() {
    }

    public UserRequest(List<Integer> followerIds, Integer userId) {
        this.followerIds = followerIds;
        this.userId = userId;
    }

    public List<Integer> getFollowerIds() {
        return followerIds;
    }

    public void setFollowerIds(List<Integer> followerIds) {
        this.followerIds = followerIds;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
