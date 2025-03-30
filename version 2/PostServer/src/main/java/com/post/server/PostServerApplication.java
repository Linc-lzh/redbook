package com.post.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PostServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostServerApplication.class, args);
	}

}
