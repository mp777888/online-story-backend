package com.onlinestories.ai_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"com.onlinestories.ai_service",
		"com.onlinestories.common"
})
@EnableFeignClients(basePackages = "com.onlinestories.ai_service.client")
@EnableDiscoveryClient
public class AiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiServiceApplication.class, args);
	}

}
