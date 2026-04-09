package com.onlinestories.user_service.Client;

import com.onlinestories.user_service.DTO.Response.WalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "transaction-service")
public interface TransactionClient {

    @PostMapping("/api/transactions/wallet")
    WalletResponse createWallet(@RequestBody String userId);

    @DeleteMapping("/api/transactions/wallet")
    void deleteWallet(@RequestBody String userId);

    @PostMapping("/api/transactions/internal/check-in")
    void addCheckInTokens(@RequestParam String userId, @RequestParam int tokens);
}
