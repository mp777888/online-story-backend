package com.example.onlinestories.transaction_service.Controller;

import com.example.onlinestories.transaction_service.DTO.Response.HistoryResponse;
import com.example.onlinestories.transaction_service.DTO.Response.WalletResponse;
import com.example.onlinestories.transaction_service.Service.MomoService;
import com.example.onlinestories.transaction_service.Service.VNPayService;
import com.example.onlinestories.transaction_service.Service.WalletService;
import com.onlinestories.common.exception.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionController {
    VNPayService vnPayService;
    MomoService momoService;
    WalletService walletService;

    @PostMapping("/wallet")
    public ResponseEntity<WalletResponse> createWallet(@RequestBody String userId){
        log.info("Received request to create wallet");
        return ResponseEntity.ok(walletService.createWallet(userId));
    }

    @GetMapping("/my-wallet")
    public ApiResponse<WalletResponse> getMyWallet(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Received request to get wallet for userId: {}", userId);
        WalletResponse walletResponse = walletService.getMyWallet(userId);
        return ApiResponse.<WalletResponse>builder()
                .code(200)
                .result(walletResponse)
                .build();
    }

    @DeleteMapping("/wallet")
    public ResponseEntity<Void> deleteWallet(@RequestBody String userId){
        log.info("Received request to delete wallet");
        walletService.deleteWallet(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vn-pay")
    public ApiResponse<String> createVNPayTransaction(HttpServletRequest request){
        log.info("Received request to create VNPay transaction");
        return ApiResponse.<String>builder()
                .code(200)
                .result(vnPayService.createPaymentUrl(request))
                .build();
    }

    @GetMapping("/vn-pay-call-back")
    public ApiResponse<String> handleVNPayCallback(HttpServletRequest request){
        log.info("Received VNPay callback");
        String status = request.getParameter("vnp_ResponseCode");
        String message = "Payment Successful";
        if (status.equals("00")) {
            walletService.topUpReadingTokens(request.getParameter("vnp_TxnRef"));
        } else {
            walletService.updateStatus(request.getParameter("vnp_TxnRef"), "FAILED");
            log.warn("Payment failed for orderId: {}, with response code: {}", request.getParameter("vnp_TxnRef"), status);
            message = "Payment Failed with code: " + status;
        }
        return ApiResponse.<String>builder()
                .code(200)
                .result(message)
                .build();
    }

    @GetMapping("/momo")
    public ApiResponse<String> createMomoTransaction(HttpServletRequest request) throws Exception {
        log.info("Received request to create MoMo transaction");
        return ApiResponse.<String>builder()
                .code(200)
                .result(momoService.createMomoTopupPayment(request))
                .build();
    }

    @GetMapping("/momo-call-back")
    public ApiResponse<String> handleMomoCallback(HttpServletRequest request) {
        log.info("Received MoMo callback");
        String resultCode = request.getParameter("resultCode");
        String message = "Payment Successful";
        if (resultCode.equals("0")) {
            walletService.topUpReadingTokens(request.getParameter("orderId"));
        } else {
            walletService.updateStatus(request.getParameter("orderId"), "FAILED");
            log.warn("Payment failed for orderId: {}, with result code: {}", request.getParameter("orderId"), resultCode);
            message = "Payment Failed with code: " + resultCode;
        }
        return ApiResponse.<String>builder()
                .code(200)
                .result(message)
                .build();
    }

    @PostMapping("/unlock-story")
    public ApiResponse<String> unlockStory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId) {
        String userId = jwt.getSubject();
        log.info("Received request to unlock story for userId: {}, storyId: {}", userId, storyId);
        walletService.createUnlockStory(userId, storyId);
        return ApiResponse.<String>builder()
                .code(200)
                .result("Story unlocked successfully")
                .build();
    }

    @GetMapping("/history")
    public ApiResponse<Page<HistoryResponse>> getTransactionHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam int page,
            @RequestParam int size) {
        String userId = jwt.getSubject();
        log.info("Received request to get transaction history for userId: {}, page: {}, size: {}", userId, page, size);
        return ApiResponse.<Page<HistoryResponse>>builder()
                .code(200)
                .result(walletService.getMyHistory(userId, page, size))
                .build();
    }
}
