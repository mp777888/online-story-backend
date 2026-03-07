package com.onlinestories.story_service.Controller;

import com.onlinestories.story_service.DTO.Request.CreateStoryRequest;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.Service.StoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/stories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StoryController {
    StoryService storyService;

    @PostMapping
    public ResponseEntity<StoryResponse> createNewStory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("request") CreateStoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img) {
        String authorId = jwt.getSubject();
        log.info("Received request to create new story: {}", request.getTitle());
        return storyService.createNewStory(request, authorId, img);
    }

    @GetMapping("/my-stories")
    public ResponseEntity<Page<StoryResponse>> getMyStories(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String authorId = jwt.getSubject();
        log.info("Received request to fetch stories for author: {}", authorId);
        return storyService.getMyStories(authorId, page, size);
    }

    @GetMapping("/user-stories")
    public ResponseEntity<Page<StoryResponse>> getUserStories(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to fetch stories for user: {}", userId);
        return storyService.getUserStories(userId, page, size);
    }
}
