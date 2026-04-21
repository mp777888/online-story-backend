package com.onlinestories.ai_service.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "story-service")
public interface StoryClient {
}
