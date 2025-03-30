package com.shop.cart.service.impl;

import com.shop.cart.init.CouponDataInitializer;
import com.shop.cart.model.Coupon;
import com.shop.cart.service.CouponService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    @Override
    public Coupon findById(Long id) {
        return CouponDataInitializer.getCouponById(id);
    }

    @Override
    public List<Coupon> findCouponsByProductIdAndShopId(Long productId, Long shopId) {
        // 获取所有优惠券
        List<Coupon> allCoupons = CouponDataInitializer.getAllCoupons();

        // 筛选适用的优惠券
        return allCoupons.stream()
                .filter(coupon ->
                        (coupon.getProductId() == null && coupon.getShopId() == null) || // 适用于所有商品和商家
                                (coupon.getProductId() != null && coupon.getProductId().equals(productId)) || // 适用于指定商品
                                (coupon.getShopId() != null && coupon.getShopId().equals(shopId)) // 适用于指定商家
                )
                .collect(Collectors.toList());
    }
}

