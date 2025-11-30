package com.example.onlinestories.transaction_service.Controller;

import com.example.onlinestories.transaction_service.Service.MomoService;
import com.example.onlinestories.transaction_service.Service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionController {
    VNPayService vnPayService;
    MomoService momoService;

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

}
