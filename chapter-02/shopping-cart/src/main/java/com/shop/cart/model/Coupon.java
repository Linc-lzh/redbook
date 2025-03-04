package com.shop.cart.model;

import java.time.LocalDateTime;

public class Coupon {
    private Long id; // 优惠券ID
    private String description; // 描述
    private Double discount; // 折扣金额或折扣率
    private LocalDateTime validUntil; // 有效期至
    private Long shopId; // 适用的商家ID
    private Long productId; // 适用的商品ID

    // 构造函数
    public Coupon() {
    }

    public Coupon(Long id, String description, Double discount, LocalDateTime validUntil, Long shopId, Long productId) {
        this.id = id;
        this.description = description;
        this.discount = discount;
        this.validUntil = validUntil;
        this.shopId = shopId;
        this.productId = productId;
    }

    // getter和setter方法

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
