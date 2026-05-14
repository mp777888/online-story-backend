package com.example.onlinestories.transaction_service.service;

import com.example.onlinestories.transaction_service.client.UserClient;
import com.example.onlinestories.transaction_service.config.VNPayConfig;
import com.example.onlinestories.transaction_service.dto.response.UserResponse;
import com.example.onlinestories.transaction_service.entity.PendingPayment;
import com.example.onlinestories.transaction_service.enums.PaymentStatus;
import com.example.onlinestories.transaction_service.repository.PendingPaymentRepository;
import com.example.onlinestories.transaction_service.util.VNPayUtil;
import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class VNPayService {
    VNPayConfig vnPayConfig;
    UserClient userClient;
    PendingPaymentRepository pendingPaymentRepository;

    public String createPaymentUrl(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getSubject();

            // Validate author existence via UserClient
            UserResponse userResponse = userClient.getUserById(userId);
            if (userResponse == null) {
                log.error("Author with ID {} not found", userId);
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            long amount = Integer.parseInt(request.getParameter("amount")) * 100L;


            String bankCode = request.getParameter("bankCode");
            Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
            vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParamsMap.put("vnp_BankCode", bankCode);
            }
            vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
            //build query url
            String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
            String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

            PendingPayment pendingPayment = PendingPayment.builder()
                    .txnRef(vnpParamsMap.get("vnp_TxnRef"))
                    .userId(userId)
                    .amount((int) (amount / 100L))
                    .status(PaymentStatus.PENDING)
                    .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .build();
            pendingPaymentRepository.save(pendingPayment);

            return vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        }
        catch (Exception e) {
            log.error("Error creating payment URL: {}", e.getMessage());
            throw new RuntimeException("Error creating payment URL", e);
        }
    }
}
