package com.coupon.couponserver.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

public class CouponTemplate {
    /**
     * 自增主键
     */
    private int id;

    /**
     * 是否可用；true: 可用, false: 不可用
     */
    private int available;

    /**
     * 是否过期；true: 是, false: 否
     */
    private int expired;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 优惠券描述
     */
    private String intro;

    /**
     * 关联对象表主键
     */
    private int objId;

    /**
     * 关联对象类型，如商家、类目、品牌、商品
     */
    private int objType;

    /**
     * 总数
     */
    private int couponCount;

    /**
     * 剩余数量
     */
    private int remainingCount;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date createTime;

    /**
     * 创建用户
     */
    private long userId;

    /**
     * 优惠券模板编码
     */
    private String templateKey;

    /**
     * 目标用户
     */
    private int target;

    /**
     * 优惠券类型，0: 满减券, 1: 折扣券
     */
    private int couponType;

    /**
     * 优惠额度，满减券为具体金额，折扣券为折扣比例
     */
    private BigDecimal discount;

    /**
     * 最低使用额度
     */
    private BigDecimal minAmount;

    /**
     * 最高使用额度
     */
    private BigDecimal maxAmount;

    /**
     * 是否可叠加使用；true: 可以叠加, false: 不可叠加
     */
    private int canStack;

    /**
     * 是否平台券；true: 平台券, false: 商家券
     */
    private int isPlatformCoupon;

    /**
     * 关联活动id
     */
    private int activityId;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date endTime;

    /**
     * 时间类型
     */
    private int validityMode;

    /**
     * 失效时间
     */
    private int validityDays;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getObjId() {
        return objId;
    }

    public void setObjId(int objId) {
        this.objId = objId;
    }

    public int getObjType() {
        return objType;
    }

    public void setObjType(int objType) {
        this.objType = objType;
    }

    public int getCouponCount() {
        return couponCount;
    }

    public void setCouponCount(int couponCount) {
        this.couponCount = couponCount;
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(int remainingCount) {
        this.remainingCount = remainingCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getCouponType() {
        return couponType;
    }

    public void setCouponType(int couponType) {
        this.couponType = couponType;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public int getActivityId() {
        return activityId;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getExpired() {
        return expired;
    }

    public void setExpired(int expired) {
        this.expired = expired;
    }

    public int getCanStack() {
        return canStack;
    }

    public void setCanStack(int canStack) {
        this.canStack = canStack;
    }

    public int getIsPlatformCoupon() {
        return isPlatformCoupon;
    }

    public void setIsPlatformCoupon(int isPlatformCoupon) {
        this.isPlatformCoupon = isPlatformCoupon;
    }

    public int getValidityMode() {
        return validityMode;
    }

    public void setValidityMode(int validityMode) {
        this.validityMode = validityMode;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(int validityDays) {
        this.validityDays = validityDays;
    }

    @Override
    public String toString() {
        return "CouponTemplate{" +
                "id=" + id +
                ", available=" + available +
                ", expired=" + expired +
                ", name='" + name + '\'' +
                ", intro='" + intro + '\'' +
                ", objId=" + objId +
                ", objType=" + objType +
                ", couponCount=" + couponCount +
                ", remainingCount=" + remainingCount +
                ", createTime=" + createTime +
                ", userId=" + userId +
                ", templateKey='" + templateKey + '\'' +
                ", target=" + target +
                ", couponType=" + couponType +
                ", discount=" + discount +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", canStack=" + canStack +
                ", isPlatformCoupon=" + isPlatformCoupon +
                ", activityId=" + activityId +
                ", endTime=" + endTime +
                ", validityMode=" + validityMode +
                '}';
    }
}

