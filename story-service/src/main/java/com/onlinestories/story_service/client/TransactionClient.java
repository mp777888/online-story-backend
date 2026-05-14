package com.onlinestories.story_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

}
