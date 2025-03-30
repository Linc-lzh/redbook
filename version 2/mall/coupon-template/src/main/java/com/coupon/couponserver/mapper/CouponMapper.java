package com.coupon.couponserver.mapper;

import com.coupon.couponserver.model.Coupon;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 优惠券
 */
@Mapper
public interface CouponMapper {

    //查询优惠券
    List<Coupon> all(Coupon coupon);

    //领取优惠券
    void receive(List<Coupon> coupons);

    //核销优惠券
    void use(Coupon coupon);

}
