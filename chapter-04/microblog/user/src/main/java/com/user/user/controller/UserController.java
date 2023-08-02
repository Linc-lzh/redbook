package com.user.user.controller;

import com.common.result.Result;
import com.user.user.model.Attention;
import com.user.user.model.Follower;
import com.user.user.model.User;
import com.user.user.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


    @Resource
    private UserService userService;

    @RequestMapping("/get/info")
    public Result<User> getUserInfo(Integer id) {
        return userService.getUserInfo(id);
    }

    @RequestMapping("/get/followers")
    public Result<List<Integer>> getFollowers(Integer uid) {
        return userService.getFollowers(uid);
    }

    @RequestMapping("/get/attentions")
    public Result<List<Integer>> getAttentions(Integer uid) {
        return userService.getAttentions(uid);
    }

    @RequestMapping("/set/attention")
    public Result<List<Integer>> setAttention(@RequestBody Follower follower) {
        return userService.setFollowers(follower);
    }

}
