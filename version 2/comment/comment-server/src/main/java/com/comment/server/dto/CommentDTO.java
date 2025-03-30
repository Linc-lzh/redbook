package com.comment.server.dto;

import java.util.Date;

public class CommentDTO {

    // 从CommentIndex实体类中选取的字段
    private Long id; // 主键
    private Integer objId; // 对象id
    private Integer type; // 对象类型(1:帖子，2:视频，3:商品）
    private Integer memberId; // 发表者id
    private Integer root; // 根评论id，不为0是回复评论
    private Integer parent; // 父评论id，为0是root评论
    private Integer floor; // 评论楼层
    private Integer like; // 点赞数
    private Integer state; // 状态，0：正常；1：隐藏
    private Integer attrs; // 属性，0：不置顶；1：置顶
    private Integer count; // 父评论下的所有子评论的总数
    private Date createTime; // 创建时间
    private Date updateTime; // 修改时间

    // 从CommentContent实体类中选取的字段
    private String message; // 内容，存储评论的文本内容

    // 新增字段表示是否点赞
    private boolean liked; // 是否点赞

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getObjId() {
        return objId;
    }

    public void setObjId(Integer objId) {
        this.objId = objId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getRoot() {
        return root;
    }

    public void setRoot(Integer root) {
        this.root = root;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getAttrs() {
        return attrs;
    }

    public void setAttrs(Integer attrs) {
        this.attrs = attrs;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
