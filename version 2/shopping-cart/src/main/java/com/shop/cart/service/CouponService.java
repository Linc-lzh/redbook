package com.shop.cart.service;

import com.shop.cart.model.Coupon;

import java.util.List;

public interface CouponService {
    Coupon findById(Long id);

    List<Coupon> findCouponsByProductIdAndShopId(Long productId, Long shopId);
}

