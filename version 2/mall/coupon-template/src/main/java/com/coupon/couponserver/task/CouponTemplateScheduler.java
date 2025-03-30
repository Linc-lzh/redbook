package com.coupon.couponserver.task;

import com.coupon.couponserver.consumer.DelayQueueConsumer;
import com.coupon.couponserver.model.CouponTemplate;
import com.coupon.couponserver.model.DelayedCouponTemplate;
import com.coupon.couponserver.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.DelayQueue;

@Component
public class CouponTemplateScheduler {

    @Resource
    private CouponService couponTemplateService;

    @Autowired
    private DelayQueueConsumer delayQueueConsumer;

    @Resource
    private DelayQueue<DelayedCouponTemplate> delayQueue; // 延迟队列对象

    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行一次
    public void checkExpiringTemplates() {
        List<CouponTemplate> expiringTemplates = couponTemplateService.getExpiringTemplates();

        for (CouponTemplate template : expiringTemplates) {
            long currentTime = System.currentTimeMillis();
            long expireTime = template.getEndTime().getTime();
            long remainingTime = expireTime - currentTime;

            if (remainingTime > 0) {
                DelayedCouponTemplate delayedTemplate = new DelayedCouponTemplate(template.getId(), remainingTime);
                delayQueue.offer(delayedTemplate);
            }
        }
    }


}

