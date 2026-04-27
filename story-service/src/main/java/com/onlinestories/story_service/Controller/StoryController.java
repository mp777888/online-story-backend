package com.onlinestories.story_service.Controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.common.utils.JwtUtils;
import com.onlinestories.story_service.DTO.Request.CreateStoryRequest;
import com.onlinestories.story_service.DTO.Request.UpdateStoryRequest;
import com.onlinestories.story_service.DTO.Response.ChapterResponse;
import com.onlinestories.story_service.DTO.Response.FavoriteResponse;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.Enum.Period;

import com.onlinestories.story_service.Service.ReadingService;
import com.onlinestories.story_service.Service.StoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    public ApiResponse<StoryResponse> createNewStory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("request") CreateStoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img) {
        String authorId = jwt.getSubject();
        log.info("Received request to create new story: {}", request.getTitle());
        return ApiResponse.<StoryResponse>builder()
                .code(200)
                .message("Story created successfully")
                .result(storyService.createNewStory(request, authorId, img))
                .build();
    }

    @GetMapping("/details")
    public ApiResponse<StoryResponse> getStoryDetails(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId) {
        String userId = jwt.getSubject();
        log.info("Received request to fetch story details for story ID: {}", storyId);
        return ApiResponse.<StoryResponse>builder()
                .code(200)
                .message("Story details fetched successfully")
                .result(storyService.getStoryDetails(userId,storyId))
                .build();
    }

    @DeleteMapping
    public ApiResponse<Void> deleteStory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId) {
        String authorId = jwt.getSubject();
        log.info("Received request to delete story with ID: {}", storyId);
        storyService.deleteStory(authorId, storyId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Story deleted successfully")
                .build();
    }

    @GetMapping("/my-stories")
    public ApiResponse<Page<StoryResponse>> getMyStories(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String authorId = jwt.getSubject();
        log.info("Received request to fetch stories for author: {}", authorId);
        return ApiResponse.<Page<StoryResponse>>builder()
                .code(200)
                .message("My stories fetched successfully")
                .result(storyService.getMyStories(authorId, page, size))
                .build();
    }

    @GetMapping("/user-stories")
    public ApiResponse<Page<StoryResponse>> getUserStories(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to fetch stories for user: {}", userId);
        return ApiResponse.<Page<StoryResponse>>builder()
                .code(200)
                .message("User stories fetched successfully")
                .result(readingService.getUserStories(userId, page, size))
                .build();
    }

    @GetMapping("/list-chapters")
    public ApiResponse<Page<ChapterResponse>> getStoryWithChapters(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = jwt.getSubject();
        boolean isAdmin = JwtUtils.isAdmin(jwt);
        log.info("Received request to fetch story with chapters for story ID: {}", storyId);
        return ApiResponse.<Page<ChapterResponse>>builder()
                .code(200)
                .message("Story with chapters fetched successfully")
                .result(readingService.getChaptersByStoryId(userId, isAdmin,storyId, page, size))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<StoryResponse> updateStory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("request") UpdateStoryRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img) {
        String userId = jwt.getSubject();
        boolean isAdmin = JwtUtils.isAdmin(jwt);
        log.info("Received request to update story: {}", request.getTitle());
        return ApiResponse.<StoryResponse>builder()
                .code(200)
                .message("Story updated successfully")
                .result(storyService.updateStory(userId, isAdmin, request, img))
                .build();
    }

    @GetMapping("/favorite-stories")
    public ApiResponse<Page<FavoriteResponse>> getFavoriteStories(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = jwt.getSubject();
        log.info("Received request to fetch favorite stories for user: {}", userId);
        return ApiResponse.<Page<FavoriteResponse>>builder()
                .code(200)
                .message("Favorite stories fetched successfully")
                .result(readingService.getFavoriteStories(userId, page, size))
                .build();
    }

    @GetMapping("/top-rating-stories")
    public ApiResponse<Page<StoryResponse>> getTopRatedStories(
            @RequestParam(defaultValue = "ALL_TIME") Period period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received get top rated stories request for page: {}, size: {}", page, size);
        return ApiResponse.<Page<StoryResponse>>builder()
                .code(200)
                .message("Top rated stories retrieved successfully")
                .result(readingService.getTopRatingStories(period, page, size))
                .build();
    }

    @GetMapping("/top-viewed-stories")
    public ApiResponse<Page<StoryResponse>> getTopViewedStories(
            @RequestParam(defaultValue = "ALL_TIME") Period period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received get top viewed stories request for page: {}, size: {}", page, size);
        return ApiResponse.<Page<StoryResponse>>builder()
                .code(200)
                .message("Top viewed stories retrieved successfully")
                .result(readingService.getTopViewedStories(period, page, size))
                .build();
    }

    @GetMapping("/latest-updated-stories")
    public ApiResponse<Page<StoryResponse>> getLatestUpdatedStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received get latest updated stories request for page: {}, size: {}", page, size);
        return ApiResponse.<Page<StoryResponse>>builder()
                .code(200)
                .message("Latest updated stories retrieved successfully")
                .result(readingService.getRecentlyUpdatedStories(page, size))
                .build();
    }

    @GetMapping("/all-stories")
    public ApiResponse<Page<StoryResponse>> getAllStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to fetch all stories for page: {}, size: {}", page, size);
        return ApiResponse.<Page<StoryResponse>>builder()
                .code(200)
                .message("All stories fetched successfully")
                .result(readingService.getAllStories(page, size))
                .build();
    }

    @GetMapping("/unlock-story")
    public ApiResponse<Boolean> checkIfStoryUnlocked(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId) {
        String userId = jwt.getSubject();
        log.info("Received request to check if story is unlocked for user: {}, story ID: {}", userId, storyId);
        return ApiResponse.<Boolean>builder()
                .code(200)
                .message("Unlock status checked successfully")
                .result(readingService.isUnlockStory(userId, storyId))
                .build();
    }
}
