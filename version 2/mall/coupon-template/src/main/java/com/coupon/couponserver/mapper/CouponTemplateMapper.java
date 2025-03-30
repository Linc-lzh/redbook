package com.coupon.couponserver.mapper;

import com.coupon.couponserver.model.CouponTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponTemplateMapper {
    int insertCouponTemplate(CouponTemplate couponTemplate);

    int update(CouponTemplate couponTemplate);

    int delete(int id);

    CouponTemplate findById(int id);

    CouponTemplate findMaxCouponId();

    List<CouponTemplate> findAll();

    List<CouponTemplate> getCouponTemplatesByValidityMode(@Param("validityMode") int validityMode);
}

