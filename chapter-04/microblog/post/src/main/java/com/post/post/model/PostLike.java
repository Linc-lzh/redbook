package com.post.post.model;


import java.util.Date;

public class PostLike {
    /**
     * 主键
     */
    private int id;

    /**
     * 帖子id
     */
    private int tid;

    /**
     * 用户id
     */
    private int uid;

    /**
     * 是否取消点赞
     */
    private int isDelete;

    private Date createTime;

    // 默认构造函数
    public PostLike() {
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

