package com.coupon.couponserver.task;

import com.coupon.couponserver.mapper.CouponTemplateMapper;
import com.coupon.couponserver.model.CouponTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
public class CouponTemplateTask {

    @Resource
    private CouponTemplateMapper couponTemplateMapper;

    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨12点执行一次
    public void updateTemplate() {
        // 获取当前时间
        Date now = new Date();
        //查询所有有固定结束时间的优惠券模板
        List<CouponTemplate> all = couponTemplateMapper.getCouponTemplatesByValidityMode(0);
        // 循环遍历优惠券模板列表
        for (CouponTemplate couponTemplate : all) {
            // 判断结束时间是否小于等于当前时间
            if (couponTemplate.getEndTime().compareTo(now)<=0) {
                System.out.println("优惠券模板已过期：" + couponTemplate.getName());
            }
        }
    }
}
