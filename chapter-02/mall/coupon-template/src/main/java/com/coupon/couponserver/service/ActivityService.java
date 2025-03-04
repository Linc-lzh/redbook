package com.coupon.couponserver.service;

import com.coupon.couponserver.model.Activity;

import java.util.List;

public interface ActivityService {

    void createActivity(Activity activity);

    void updateActivity(Activity activity);

    void deleteActivity(int activityId);

    Activity getActivityById(int activityId);

    List<Activity> getAllActivities();
}

