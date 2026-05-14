package com.onlinestories.recommend_service.controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.recommend_service.entity.StorySearchItem;
import com.onlinestories.recommend_service.Service.RecommendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/recommend")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecommendController {
    RecommendService recommendService;

    @GetMapping("/search")
    public ApiResponse<Page<Object>> searchAll(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.<Page<Object>>builder()
                .code(200)
                .message("Search results for query: " + query)
                .result(recommendService.searchAll(query, page, size))
                .build();
    }

    @GetMapping("/by-genres")
    public ApiResponse<Page<StorySearchItem>> recommendByGenres(
            @RequestParam List<String> genres,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.<Page<StorySearchItem>>builder()
                .code(200)
                .message("Recommendations based on genres: " + String.join(", ", genres))
                .result(recommendService.recommendStoriesByGenres(genres, page, size))
                .build();
    }

    @GetMapping("/similar-stories")
    public ApiResponse<Page<StorySearchItem>> recommendSimilarStories(
            @RequestParam String storyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.<Page<StorySearchItem>>builder()
                .code(200)
                .message("Recommendations for story ID: " + storyId)
                .result(recommendService.recommendSimilarStories(storyId, page, size))
                .build();
    }
}
