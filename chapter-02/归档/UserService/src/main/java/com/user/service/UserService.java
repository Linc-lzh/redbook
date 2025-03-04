package com.user.service;

import com.user.dto.UserDTO;
import com.user.model.User;

import java.util.List;

public interface UserService {

    /**
     * 注册用户
     * @param user
     * @return
     */
    boolean register(User user);

    /**
     * 登录用户
     * @param username
     * @param password
     * @return
     */
    User login(String username, String password);

    /**
     * 模糊搜索用户
     * @param username
     * @return
     */
    List<UserDTO> searchUsersByUsername(String username);

    /**
     * 根据粉丝id批量获取用户信息
     * @param followerIds
     * @return
     */
    List<UserDTO> getUsersInfoByFollowerIds(List<Integer> followerIds , Integer userId);

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    User getUserInfo(Long userId);
}
