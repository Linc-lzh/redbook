package com.coupon.couponserver.service;

import com.coupon.couponserver.model.Coupon;
import com.coupon.couponserver.model.CouponTemplate;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    /**
     * 新增优惠券模板
     * @param couponTemplate
     * @return
     */
    int insert(CouponTemplate couponTemplate);

    /**
     * 修改优惠券
     * @param couponTemplate
     * @return
     */
    int update(CouponTemplate couponTemplate);

    /**
     * 删除优惠券
     * @param id
     * @return
     */
    int delete(int id);

    /**
     * 根据id查询优惠券
     * @param id
     * @return
     */
    CouponTemplate findById(int id);

    /**
     * 查询所有优惠券
     * @return
     */
    List<CouponTemplate> findAll();

    /**
     * 查询用户所有优惠券
     * @param userId
     * @return
     */
    List<Coupon> findAll(int userId , int type);

    /**
     * 查询用户所有优惠券
     * @param userId
     * @return
     */
    List<Coupon> findAll(int userId , int type , BigDecimal amount ,
                         int brandId , int merchantId , int commodityId);

    /**
     * 领取优惠券
     * @param coupon
     */
    void receive(Coupon coupon);

    /**
     * 批量领取优惠券
     * @param coupons
     */
    void receives(List<Coupon> coupons);

    /**
     * 获取所有即将过期的优惠券
     * @return
     */
    List<CouponTemplate> getExpiringTemplates();


}
