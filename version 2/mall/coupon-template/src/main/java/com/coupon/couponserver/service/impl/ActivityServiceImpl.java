package com.coupon.couponserver.service.impl;

import com.coupon.couponserver.mapper.ActivityMapper;
import com.coupon.couponserver.model.Activity;
import com.coupon.couponserver.service.ActivityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Resource
    private ActivityMapper activityMapper;

    @Override
    public void createActivity(Activity activity) {
        activityMapper.insert(activity);
    }

    @Override
    public void updateActivity(Activity activity) {
        activityMapper.update(activity);
    }

    @Override
    public void deleteActivity(int activityId) {
        activityMapper.delete(activityId);
    }

    @Override
    public Activity getActivityById(int activityId) {
        return null;
    }

    @Override
    public List<Activity> getAllActivities() {
        return null;
    }
}

