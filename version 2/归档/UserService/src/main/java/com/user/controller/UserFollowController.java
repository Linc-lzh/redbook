package com.user.controller;

import com.user.model.User;
import com.user.model.UserFollow;
import com.user.service.UserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

@RestController
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    @GetMapping("/followings/{userId}")
    public ResponseEntity<Map<String, Object>> getFollowingsByUserId(@PathVariable Integer userId,
                                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Map<String, Object> followingsByUserId = userFollowService.getFollowingsByUserId(userId, pageNum, pageSize);
            if (followingsByUserId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(followingsByUserId, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<Map<String, Object>> getFollowersByUserId(@PathVariable Integer userId,
                                                                 @RequestParam(defaultValue = "1") int pageNum,
                                                                 @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Map<String, Object> followersByUserId = userFollowService.getFollowersByUserId(userId, pageNum, pageSize);
            if (followersByUserId.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(followersByUserId, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取指定用户的粉丝ID列表。
     * 请求方法：GET
     * 请求路径：/userId/followers/ids
     * 请求参数：用户ID (通过路径参数传递)
     * 响应体：用户的粉丝ID列表
     *
     * @param userId 路径变量，指定要获取粉丝列表的用户ID
     * @return 如果找到粉丝ID，则返回这些ID的列表；如果未找到，则返回404状态；如果出现异常，则返回500状态
     */
    @GetMapping("/{userId}/followers/ids")
    public ResponseEntity<List<Integer>> getFollowerIds(@PathVariable Integer userId) {
        try {
            // 从服务层获取指定用户的粉丝ID列表
            List<Integer> followerIds = userFollowService.getFollowerIds(userId);

            // 如果没有找到任何粉丝ID，返回404状态
            if (followerIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // 如果找到了粉丝ID，返回这些ID的列表
            return ResponseEntity.ok(followerIds);
        } catch (Exception e) {
            // 如果处理过程中出现异常，返回500状态
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 通过用户ID列表批量获取用户数据。
     * 请求方法：GET
     * 请求路径：/fetch
     * 请求参数：用户ID列表 (通过请求参数传递)
     * 响应体：用户数据的映射，映射键为用户ID，值为User对象
     *
     * @param userIds 请求参数，用户ID列表
     * @return 返回一个映射，包含用户ID与相应的User对象；如果出现异常，则返回500状态
     */
    @GetMapping("/fetch")
    public ResponseEntity<Map<Long, User>> fetchUsersData(@RequestParam List<Long> userIds) {
        try {
            // 从服务层获取用户数据
            Map<Long, User> usersData = userFollowService.fetchUsersData(userIds);

            // 返回获取到的用户数据
            return ResponseEntity.ok(usersData);
        } catch (Exception e) {
            // 如果处理过程中出现异常，返回500状态
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 处理GET请求，通过URL中的userId来获取用户的所有粉丝
    @GetMapping("/followers/all/{userId}")
    public List<Integer> getAllFollowers(@PathVariable int userId) {
        // 调用Service层的方法，返回特定用户的所有粉丝ID列表
        return userFollowService.getAllFollowers(userId);
    }
}

