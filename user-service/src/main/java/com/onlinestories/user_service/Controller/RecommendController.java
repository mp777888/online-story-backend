package com.onlinestories.user_service.Controller;

import com.onlinestories.user_service.Exception.ApiResponse;
import com.onlinestories.user_service.Service.RecommendationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/recommend")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecommendController {
    RecommendationService recommendationService;

    @PostMapping("/categories")
    public ApiResponse<String> addCategories(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<String> categories) {
        String userId = jwt.getSubject();
        log.info("Received request to add categories for userId={}, categories={}", userId, categories);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Categories added successfully")
                .result(recommendationService.addCategories(userId, categories))
                .build();
    }

    @GetMapping("/categories")
    public ApiResponse<Boolean> isCategoriesEmpty(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to check if categories are empty for userId={}", userId);
        Boolean isEmpty = recommendationService.isCategoriesEmpty(userId);
        return ApiResponse.<Boolean>builder()
                .code(200)
                .message("Categories empty check completed successfully")
                .result(isEmpty)
                .build();
    }

    @GetMapping("/categories/list")
    public ApiResponse<List<String>> getUserCategories(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get user categories for userId={}", userId);
        return ApiResponse.<List<String>>builder()
                .code(200)
                .message("User categories retrieved successfully")
                .result(recommendationService.getUserCategories(userId))
                .build();
    }
}
