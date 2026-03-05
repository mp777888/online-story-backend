package com.onlinestories.user_service.Controller;

import com.onlinestories.user_service.DTO.Request.PackageRegisterRequest;
import com.onlinestories.user_service.DTO.Response.PackageResponse;
import com.onlinestories.user_service.Service.PackageService;
import com.onlinestories.user_service.Service.RecommendationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> addCategories(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<String> categories) {
        String userId = jwt.getSubject();
        log.info("Received request to add categories for userId={}, categories={}", userId, categories);
        return recommendationService.addCategories(userId, categories);
    }

    @GetMapping("/categories")
    public ResponseEntity<Boolean> isCategoriesEmpty(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to check if categories are empty for userId={}", userId);
        Boolean isEmpty = recommendationService.isCategoriesEmpty(userId);
        return ResponseEntity.ok(isEmpty);
    }

    @GetMapping("/categories/list")
    public ResponseEntity<List<String>> getUserCategories(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get user categories for userId={}", userId);
        return recommendationService.getUserCategories(userId);
    }
}
