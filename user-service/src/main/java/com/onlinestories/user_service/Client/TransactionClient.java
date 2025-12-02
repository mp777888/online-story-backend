package com.onlinestories.user_service.Client;

import com.onlinestories.user_service.DTO.Response.WalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-service")
public interface TransactionClient {

    @PostMapping("/api/transactions/wallet")
    WalletResponse createWallet(@RequestBody String userId);
}
