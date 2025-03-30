package com.coupon.couponserver.controller;

import com.common.result.Result;
import com.coupon.couponserver.model.Coupon;
import com.coupon.couponserver.model.CouponTemplate;
import com.coupon.couponserver.service.CouponService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@CrossOrigin
public class CouponTemplateController {

    @Resource
    private CouponService couponService;

    /**
     * 新增优惠券模板
     * @param couponTemplate
     * @return
     */
    @RequestMapping("/admin/set")
    public Result set(@RequestBody CouponTemplate couponTemplate){
        couponService.insert(couponTemplate);
        return new Result(200 , "成功" , null);
    }

    /**
     * 领取优惠券
     */
    @RequestMapping("/get/coupon")
    public Result getCoupon(@RequestBody Coupon coupon){
        couponService.receive(coupon);
        return new Result(200 , "成功" , null);
    }
}
