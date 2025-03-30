package com.post.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostDTO {
    private Long id; // 帖子的唯一标识符
    private String title; // 帖子标题
    private Integer uid; // 作者的用户ID
    private Integer channelId; // 所属频道的ID
    private Integer flag; // 帖子是否置顶的标志，例如0为非置顶，1为置顶
    private Integer type; // 帖子类型，例如0代表文章，1代表图文，2代表视频
    private Integer provinceId; // 发布帖子的省份ID
    private Integer cityId; // 发布帖子的城市ID
    private Integer countyId; // 发布帖子的区县ID
    private LocalDateTime createTime; // 帖子创建的时间
    private String ll; // 帖子发布的经纬度

    private String content; //帖子的内容

    private List<String> mediaUrls;//帖子图片或者视频集合

    // 与帖子相关的统计信息
    private Integer likesCount; // 帖子的点赞数
    private Integer sharesCount; // 帖子的转发数
    private Integer commentsCount; // 帖子的评论数

    // 构造器、getter 和 setter 方法

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getCountyId() {
        return countyId;
    }

    public void setCountyId(Integer countyId) {
        this.countyId = countyId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getLl() {
        return ll;
    }

    public void setLl(String ll) {
        this.ll = ll;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getSharesCount() {
        return sharesCount;
    }

    public void setSharesCount(Integer sharesCount) {
        this.sharesCount = sharesCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }
}