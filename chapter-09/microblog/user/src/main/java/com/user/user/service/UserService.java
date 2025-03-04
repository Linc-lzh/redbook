package com.user.user.service;


import com.common.result.Result;
import com.user.user.model.User;

import java.util.List;

/**
 * userService接口
 */
public interface UserService {

    Result<User> getUserInfo(Integer id);

    Result<List<Integer>> getFollowers(Integer uid);

    Result<List<Integer>> getAttentions(Integer uid);

}
