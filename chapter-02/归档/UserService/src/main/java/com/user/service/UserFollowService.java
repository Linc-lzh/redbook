package com.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.user.mapper.UserFollowMapper;
import com.user.model.User;
import com.user.model.UserFollow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface UserFollowService {

    Map<String, Object> getFollowingsByUserId(Integer userId, int pageNum, int pageSize) ;

    Map<String, Object> getFollowersByUserId(Integer userId, int pageNum, int pageSize) ;

    List<Integer> getFollowerIds(Integer userId);

    /**
     * 根据userId查询用户信息
     * @param userIds
     * @return
     */
    Map<Long, User> fetchUsersData(List<Long> userIds);

    /**
     * 获取用户粉丝数和关注数
     * @param userId
     * @return
     */
    Map<String, Integer> getUserFollowData(Long userId);


    /**
     * 添加关注
     *
     * @param userId 用户ID
     * @param followerId 关注者（粉丝）的用户ID
     * @return 成功返回true，失败返回false
     */
    void addFollower(int userId, int followerId);

    /**
     * 取消关注
     *
     * @param userId 用户ID
     * @param followerId 关注者（粉丝）的用户ID
     * @return 成功返回true，失败返回false
     */
    void unfollow(int userId, int followerId);

    List<Integer> getAllFollowers(int userId);
}

