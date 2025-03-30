package com.user.config;

import com.user.security.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer  {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登录拦截器，并配置拦截路径
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/upload", "/register", "/css/**", "/js/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对于所有的路由都允许CORS
                .allowedOrigins("http://localhost:63343") // 允许的源地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("Content-Type", "Date", "Total-Count", "loginInfo")
                .exposedHeaders("Content-Type", "Date", "Total-Count", "loginInfo")
                .maxAge(3600);
    }
}
