package com.onlinestories.story_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@FeignClient(name = "transaction-service")
public interface TransactionClient {
    @GetMapping("/api/transactions/internal/check-unlock")
    boolean checkIfStoryUnlocked(
            @RequestParam String userId,
            @RequestParam String storyId
    );

    @PostMapping("/api/transactions/internal/update-writing-tokens")
    void updateWritingTokens(
            @RequestParam String userId,
            @RequestParam int tokens
    );

    @GetMapping("/api/transactions/internal/unlock-counts")
    Map<String, Long> getUnlockCountsForStories(
            @RequestParam("storyIds") List<String> storyIds,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    );

}
