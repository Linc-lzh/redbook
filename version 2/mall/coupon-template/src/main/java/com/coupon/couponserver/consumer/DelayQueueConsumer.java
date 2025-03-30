package com.coupon.couponserver.consumer;

import com.coupon.couponserver.mapper.CouponTemplateMapper;
import com.coupon.couponserver.model.CouponTemplate;
import com.coupon.couponserver.model.DelayedCouponTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.DelayQueue;

@Component
public class DelayQueueConsumer {

    @Resource
    private CouponTemplateMapper couponTemplateMapper;



    public void consume(DelayedCouponTemplate delayedCouponTemplate) {
        // 处理优惠券模板的逻辑
        CouponTemplate couponTemplate = new CouponTemplate();
        couponTemplate.setId(delayedCouponTemplate.getTemplateId());
        couponTemplate.setExpired(1);
        couponTemplateMapper.update(couponTemplate);

        // 执行处理逻辑
        //System.out.println("消费优惠券模板：templateId=" + templateId + ", 过期时间=" + expireDate);
    }
}

