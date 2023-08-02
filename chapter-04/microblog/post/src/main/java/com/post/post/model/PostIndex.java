package com.post.post.model;

import java.util.Date;

/**
 * 帖子索引
 */
public class PostIndex {
    /**
     * 主键
     */
    private int id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 作者id
     */
    private int uid;

    /**
     * 所属频道id
     */
    private int channelId;

    /**
     * 所属频道名称
     */
    private String channelName;

    /**
     * 是否置顶文章
     */
    private int flag;

    /**
     * 分类：0文章，1图文，2视频
     */
    private int type;

    /**
     * 图片地址（一般规定，图片最多18张，用逗号隔开），视频地址（一般规定，视频只有一个）
     */
    private String address;

    /**
     * 发布省
     */
    private int provinceId;

    /**
     * 发布市区
     */
    private int cityId;

    /**
     * 发布区县
     */
    private int countyId;

    /**
     * 标签（规定最多5个标签）
     */
    private String labels;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除：0存在，1删除
     */
    private int isDelete;

    /**
     * 经纬度
     */
    private String ll;

    private String content;

    private PostStatistics postStatistics;

    // 默认构造函数
    public PostIndex() {
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getCountyId() {
        return countyId;
    }

    public void setCountyId(int countyId) {
        this.countyId = countyId;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
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

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }

    public String getLl() {
        return ll;
    }

    public void setLl(String ll) {
        this.ll = ll;
    }

    public PostStatistics getPostStatistics() {
        return postStatistics;
    }

    public void setPostStatistics(PostStatistics postStatistics) {
        this.postStatistics = postStatistics;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

