package com.onlinestories.recommend_service.Controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.recommend_service.Entity.StorySearchItem;
import com.onlinestories.recommend_service.Service.RecommendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse<List<Object>> searchAll(@RequestParam String query) {
        return ApiResponse.<List<Object>>builder()
                .code(200)
                .message("Search results for query: " + query)
                .result(recommendService.searchAll(query))
                .build();
    }

    @GetMapping("/by-genres")
    public ApiResponse<List<StorySearchItem>> recommendByGenres(
            @RequestParam List<String> genres,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.<List<StorySearchItem>>builder()
                .code(200)
                .message("Recommendations based on genres: " + String.join(", ", genres))
                .result(recommendService.recommendStoriesByGenres(genres, limit))
                .build();
    }
}
