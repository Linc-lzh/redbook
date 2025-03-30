package com.coupon.couponserver.mapper;

import com.coupon.couponserver.model.Activity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActivityMapper {

    void insert(Activity activity);

    void update(Activity activity);

    void delete(int activityId);

    Activity getById(int activityId);

    List<Activity> getAll();
}

