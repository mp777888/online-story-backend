package com.onlinestories.recommend_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"com.onlinestories.recommend_service",
		"com.onlinestories.common"
})
@EnableFeignClients(basePackages = "com.onlinestories.recommend_service.client")
@EnableDiscoveryClient
@EnableCaching
public class RecommendServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecommendServiceApplication.class, args);
	}

}
