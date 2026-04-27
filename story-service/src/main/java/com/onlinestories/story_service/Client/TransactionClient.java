package com.onlinestories.story_service.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
