package com.onlinestories.story_service.Controller;

import com.onlinestories.story_service.DTO.Request.CreateStoryRequest;
import com.onlinestories.story_service.DTO.Request.UpdateStoryRequest;
import com.onlinestories.story_service.DTO.Response.ChapterResponse;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.Service.ReadingService;
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
    ReadingService readingService;

    @PostMapping
    public ResponseEntity<StoryResponse> createNewStory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("request") CreateStoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img) {
        String authorId = jwt.getSubject();
        log.info("Received request to create new story: {}", request.getTitle());
        return storyService.createNewStory(request, authorId, img);
    }

    @GetMapping("/details")
    public ResponseEntity<StoryResponse> getStoryDetails(@RequestParam String storyId) {
        log.info("Received request to fetch story details for story ID: {}", storyId);
        return storyService.getStoryDetails(storyId);
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
        return readingService.getUserStories(userId, page, size);
    }

    @GetMapping("/list-chapters")
    public ResponseEntity<Page<ChapterResponse>> getStoryWithChapters(
            @RequestParam String storyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to fetch story with chapters for story ID: {}", storyId);
        return readingService.getChaptersByStoryId(storyId, page, size);
    }

    @GetMapping("/my-story/list-chapters")
    public ResponseEntity<Page<ChapterResponse>> getMyStoryWithChapters(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String authorId = jwt.getSubject();
        log.info("Received request to fetch my story with chapters for story ID: {}", storyId);
        return storyService.getChaptersForManagement(authorId, storyId, page, size);
    }

    @PutMapping("/update")
    public ResponseEntity<StoryResponse> updateStory(
            @RequestPart("request") UpdateStoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img) {
        log.info("Received request to update story: {}", request.getTitle());
        return storyService.updateStory(request, img);
    }
}
