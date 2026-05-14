package com.onlinestories.story_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
		"com.onlinestories.story_service",
		"com.onlinestories.common"
})
@EnableFeignClients(basePackages = "com.onlinestories.story_service.client")
@EnableDiscoveryClient
@EnableScheduling
public class StoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoryServiceApplication.class, args);
	}

}
