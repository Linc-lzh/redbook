package com.coupon.couponserver.model;
import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedCouponTemplate implements Delayed {
    private Integer templateId;
    private Long expireDate;

    public DelayedCouponTemplate(Integer templateId, Long expireDate) {
        this.templateId = templateId;
        this.expireDate = expireDate;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expireDate, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other == this) {
            return 0;
        }
        long diff = this.getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
        return Long.compare(diff, 0);
    }
}

