package com.coupon.couponserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableEurekaClient
public class CouponServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponServerApplication.class, args);
	}

}
