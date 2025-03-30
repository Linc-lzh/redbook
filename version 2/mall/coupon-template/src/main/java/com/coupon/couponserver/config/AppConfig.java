package com.coupon.couponserver.config;

import com.coupon.couponserver.model.DelayedCouponTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.DelayQueue;

@Configuration
public class AppConfig {

    @Bean
    public DelayQueue<DelayedCouponTemplate> delayQueue() {
        return new DelayQueue<>();
    }

    // 其他配置和 bean 定义...
}



