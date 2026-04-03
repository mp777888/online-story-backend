package com.example.onlinestories.transaction_service.Controller;

import com.example.onlinestories.transaction_service.Service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InternalController {
    WalletService walletService;

    @GetMapping("/check-unlock")
    public Boolean checkUnlockStatus(String userId, String chapterId) {
        log.info("Received check unlock status request for userId: {} and chapterId: {}", userId, chapterId);
        return walletService.checkUnlockStoryExistence(userId, chapterId);
    }
}
