package com.user.controller;

import com.user.dto.UserDTO;
import com.user.dto.UserRequest;
import com.user.model.User;
import com.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询用户详细信息controller
 */
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/searchUsers")
    public ResponseEntity<List<UserDTO>> searchUsersByUsername(@RequestParam String username) {
        try {
            List<UserDTO> userDTOS = userService.searchUsersByUsername(username);
            if (userDTOS == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(userDTOS, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getUsersInfoByFollowerIds")
    public ResponseEntity<List<UserDTO>> getUsersInfoByFollowerIds(@RequestParam Integer userId , @RequestParam String followers) {
        try {
            List<Integer> followerIds = Arrays.stream(followers.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            List<UserDTO> users = userService.getUsersInfoByFollowerIds(followerIds, userId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // 返回错误响应
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestParam Long userId) {
        try {
            // 使用传入的userId调用UserService的getUserInfo方法
            User user = userService.getUserInfo(userId);

            if (user == null) {
                return ResponseEntity.status(404).body("未找到用户信息");
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // 日志记录异常信息
            e.printStackTrace();
            // 返回服务器内部错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器内部错误");
        }
    }

}
