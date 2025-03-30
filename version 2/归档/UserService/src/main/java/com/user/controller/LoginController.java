package com.user.controller;

import com.user.dto.LoginRequest;
import com.user.dto.UserDTO;
import com.user.model.User;
import com.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/user")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        System.out.println(user.getUsername());

        // 检查用户是否存在且登录成功
        if (user != null) {
            // 将用户信息保存到 HttpSession
            session.setAttribute("loginUser", user);
        }

        return user;
    }

    //验证邮箱
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w\\.-]+@[\\w\\.-]+\\.[a-z]{2,}$");

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {

        user.setAvatar("https://img2.baidu.com/it/u=205555820,2277643118&fm=253&fmt=auto&app=138&f=JPG? w=500&h=500");

        // 验证user对象的属性，例如，检查密码、用户名和邮箱是否为空
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPasswordHash()) || StringUtils.isEmpty(user.getEmail())) {
            return ResponseEntity.badRequest().body("用户名，密码，邮箱不能为空");
        }

        // 检验邮箱地址是否正确
        Matcher matcher = EMAIL_PATTERN.matcher(user.getEmail());
        if (!matcher.matches()) {
            return ResponseEntity.badRequest().body("邮箱格式不正确");
        }

        // 调用注册服务
        boolean success = userService.register(user);

        // 根据注册结果返回相应的响应
        if (success) {
            return ResponseEntity.ok("注册成功");
        } else {
            return ResponseEntity.badRequest().body("用户注册失败");
        }
    }
}
