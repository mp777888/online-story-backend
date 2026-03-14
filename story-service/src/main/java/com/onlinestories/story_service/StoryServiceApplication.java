package com.onlinestories.story_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.onlinestories.story_service.Client")
@EnableDiscoveryClient
public class StoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoryServiceApplication.class, args);
	}

}
