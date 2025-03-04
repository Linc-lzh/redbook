package com.user.security;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // 从请求中获取session
//        Object user = request.getSession().getAttribute("user");
//
//        if (user == null) {
//            // 用户未登录，返回“用户不存在”的错误消息
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
//            try (PrintWriter writer = response.getWriter()) {
//                writer.write("User not logged in");
//            }
//            return false;
//        }
//
//        // 用户已登录，放行请求
//        return true;
        return true;
    }
}

