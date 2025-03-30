package com.user.frontend.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.user.frontend.service.client")
public class UserFrontendServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserFrontendServiceApplication.class, args);
	}

}
