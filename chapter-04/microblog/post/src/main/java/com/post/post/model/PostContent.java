package com.post.post.model;

import java.util.Date;

public class PostContent {
    /**
     * 帖子主键
     */
    private int id;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 计数
     */
    private PostStatistics postStatistics;

    // 默认构造函数
    public PostContent() {
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public PostStatistics getPostStatistics() {
        return postStatistics;
    }

    public void setPostStatistics(PostStatistics postStatistics) {
        this.postStatistics = postStatistics;
    }
}

