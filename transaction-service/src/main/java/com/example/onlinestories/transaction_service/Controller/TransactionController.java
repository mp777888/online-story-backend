package com.example.onlinestories.transaction_service.Controller;

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
    public ResponseEntity<String> createVNPayTransaction(HttpServletRequest request){
        log.info("Received request to create VNPay transaction");
        return ResponseEntity.ok(vnPayService.createPaymentUrl(request));
    }

    @GetMapping("/vn-pay-call-back")
    public ResponseEntity<String> handleVNPayCallback(HttpServletRequest request){
        log.info("Received VNPay callback");
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            return ResponseEntity.ok("Payment Successful");
        } else {
            return ResponseEntity.badRequest().body("Payment Failed");
        }
    }

    @GetMapping("/momo")
    public ResponseEntity<?> createMomoPayment() throws Exception {
        // Giả lập dữ liệu đơn hàng
        String orderId = "ORDER_" + System.currentTimeMillis(); // Bắt buộc unique
        String amount = "50000";
        String orderInfo = "Thanh toan don hang test";
        String returnUrl = "http://localhost:8084/success"; // URL user quay lại sau khi thanh toán
        String notifyUrl = "http://localhost:8084/api/payment/callback"; // URL MoMo gọi về (Cần deploy public mới nhận được)

        Map<String, Object> response = momoService.createPayment(orderId, amount, orderInfo, returnUrl, notifyUrl);

        return ResponseEntity.ok(response);
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
}
