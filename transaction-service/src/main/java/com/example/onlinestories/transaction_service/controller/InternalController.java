package com.example.onlinestories.transaction_service.controller;

import com.onlinestories.common.transaction.enums.HistoryStatus;
import com.example.onlinestories.transaction_service.service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InternalController {
    WalletService walletService;

    @GetMapping("/check-unlock")
    public Boolean checkUnlockStatus(
            @RequestParam String userId,
            @RequestParam String storyId) {
        log.info("Received request to check unlock status for userId: {} and storyId: {}", userId, storyId);
        return walletService.checkUnlockStoryExistence(userId, storyId);
    }

    @PostMapping("/check-in")
    public void addTokensForCheckIn(@RequestParam String userId, @RequestParam int tokens) {
        walletService.addCheckInTokens(userId, tokens);
    }

    @PostMapping("/update-writing-tokens")
    public void addTokens(
            @RequestParam String userId,
            @RequestParam int tokens) {
        walletService.updateWritingTokens(userId, tokens, HistoryStatus.EARNED);
    }
}
