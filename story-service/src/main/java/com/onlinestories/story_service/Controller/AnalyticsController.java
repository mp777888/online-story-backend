package com.onlinestories.story_service.Controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.story_service.DTO.Response.ChapterStatsResponse;
import com.onlinestories.story_service.DTO.Response.StoryDailyViewResponse;

import com.onlinestories.story_service.DTO.Response.TrendStatisticResponse;
import com.onlinestories.story_service.Service.AnalyticsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("api/stories/analytics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AnalyticsController {
    AnalyticsService analyticsService;

    @GetMapping("/story-daily-views")
    public ApiResponse<Page<StoryDailyViewResponse>> getStoryDailyViews(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = jwt.getSubject();
        log.info("Received request to fetch daily views for story ID: {}, page: {}, size: {}",
                storyId, page, size);
        return ApiResponse.<Page<StoryDailyViewResponse>>builder()
                .code(200)
                .message("Story daily views fetched successfully")
                .result(analyticsService.getStoryDailyViews(userId, storyId, page, size))
                .build();
    }

    @GetMapping("/chapter-stats")
    public ApiResponse<Page<ChapterStatsResponse>> getChapterStats(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = jwt.getSubject();
        log.info("Received request to fetch chapter stats for story ID: {}, page: {}, size: {}",
                storyId, page, size);
        return ApiResponse.<Page<ChapterStatsResponse>>builder()
                .code(200)
                .message("Chapter stats fetched successfully")
                .result(analyticsService.getChapterStats(userId, storyId, page, size))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/trends/categories")
    public ApiResponse<List<TrendStatisticResponse>> getCategoryTrends(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "10") int limit) {

        return ApiResponse.<List<TrendStatisticResponse>>builder()
                .code(200)
                .message("Category trends fetched successfully")
                .result(analyticsService.getTopTrendingCategories(startDate, endDate, limit))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/trends/tags")
    public ApiResponse<List<TrendStatisticResponse>> getTagTrends(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "10") int limit) {

        return ApiResponse.<List<TrendStatisticResponse>>builder()
                .code(200)
                .message("Tag trends fetched successfully")
                .result(analyticsService.getTopTrendingTags(startDate, endDate, limit))
                .build();
    }
}
