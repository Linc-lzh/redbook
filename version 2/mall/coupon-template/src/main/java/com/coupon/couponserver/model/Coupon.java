package com.coupon.couponserver.model;

import java.util.Date;

public class Coupon {
    // 自增主键
    private int id;
    // 关联优惠券模板的主键
    private int templateId;
    // 领取用户
    private long userId;
    // 优惠券码
    private String couponCode;
    // 领取时间
    private Date assignTime;
    // 优惠券的状态: 0-可使用, 1-不可使用
    private int status;

    private CouponTemplate couponTemplate;

    public Coupon() {
    }

    public Coupon(int id, int templateId, long userId, String couponCode, Date assignTime, int status) {
        this.id = id;
        this.templateId = templateId;
        this.userId = userId;
        this.couponCode = couponCode;
        this.assignTime = assignTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Date getAssignTime() {
        return assignTime;
    }

    public void setAssignTime(Date assignTime) {
        this.assignTime = assignTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public CouponTemplate getCouponTemplate() {
        return couponTemplate;
    }

    public void setCouponTemplate(CouponTemplate couponTemplate) {
        this.couponTemplate = couponTemplate;
    }
}

