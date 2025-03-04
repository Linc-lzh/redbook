package com.shop.cart.init;

import com.shop.cart.model.Coupon;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class CouponDataInitializer implements CommandLineRunner {

    private static final Map<Long, Coupon> couponsMap = new HashMap<>();
    private static final List<Coupon> coupons = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        List<Coupon> initializedCoupons = initializeCouponData();
        initializedCoupons.forEach(coupon -> {
            couponsMap.put(coupon.getId(), coupon);
            coupons.add(coupon); // 将优惠券添加到列表中
        });

        // 打印信息，确认数据已初始化
        initializedCoupons.forEach(coupon -> System.out.println("Coupon: " + coupon.getDescription()));
    }

    private List<Coupon> initializeCouponData() {
        List<Coupon> coupons = new ArrayList<>();

        coupons.add(new Coupon(1L, "全场9折", 0.9, LocalDateTime.now().plusMonths(1), 1L, null));
        coupons.add(new Coupon(2L, "指定商品立减20元", 20.0, LocalDateTime.now().plusMonths(2), null, 2L));
        coupons.add(new Coupon(3L, "节日特惠5折", 0.5, LocalDateTime.now().plusMonths(3), 3L, null));
        coupons.add(new Coupon(4L, "全场满100减20", 20.0, LocalDateTime.now().plusMonths(1), null, null));
        coupons.add(new Coupon(5L, "新品上市8折优惠", 0.8, LocalDateTime.now().plusMonths(2), null, null));
        coupons.add(new Coupon(6L, "会员专享85折", 0.85, LocalDateTime.now().plusMonths(4), null, null));
        coupons.add(new Coupon(7L, "夏季特惠满200减50", 50.0, LocalDateTime.now().plusMonths(1), null, null));
        coupons.add(new Coupon(8L, "购物节特价商品5折", 0.5, LocalDateTime.now().plusMonths(5), null, null));

        return coupons;
    }

    public static Coupon getCouponById(Long id) {
        return couponsMap.get(id);
    }

    public static List<Coupon> getAllCoupons() {
        return Collections.unmodifiableList(coupons);
    }
}

