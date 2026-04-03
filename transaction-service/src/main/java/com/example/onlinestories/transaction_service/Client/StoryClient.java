package com.example.onlinestories.transaction_service.Client;

import com.onlinestories.common.story.dto.StoryDTOResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "story-service")
public interface StoryClient {

    @GetMapping("/api/stories/internal/check-story")
    StoryDTOResponse checkStoryExistence(@RequestParam String storyId);

}
