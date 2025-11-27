package com.onlinestories.story_service.Controller;

import com.onlinestories.story_service.DTO.Request.CreateStoryRequest;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.Service.StoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/stories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StoryController {
    StoryService storyService;

    @PostMapping
    public ResponseEntity<StoryResponse> createNewStory(@RequestBody CreateStoryRequest request){
        log.info("Received request to create new story: {}", request.getTitle());
        return storyService.createNewStory(request);
    }
}
