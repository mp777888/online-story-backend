package com.example.onlinestories.transaction_service.Service;

import com.example.onlinestories.transaction_service.Client.UserClient;
import com.example.onlinestories.transaction_service.DTO.Response.UserResponse;
import com.example.onlinestories.transaction_service.Entity.PendingPayment;
import com.example.onlinestories.transaction_service.Enums.PaymentStatus;
import com.example.onlinestories.transaction_service.Repostiory.PendingPaymentRepository;
import com.example.onlinestories.transaction_service.util.MomoUtil;
import com.example.onlinestories.transaction_service.util.VNPayUtil;
import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MomoService {
    @Value("${payment.momo.partner-code}")
    private String partnerCode;

    @Value("${payment.momo.access-key}")
    private String accessKey;

    @Value("${payment.momo.secret-key}")
    private String secretKey;

    @Value("${payment.momo.endpoint}")
    private String momoEndpoint;

    @Value("${payment.momo.redirect-url}")
    private String redirectUrl;

    @Value("${payment.momo.ipn-url}")
    private String ipnUrl;

    private final PendingPaymentRepository pendingPaymentRepository;
    private final UserClient userClient;

    public MomoService(PendingPaymentRepository pendingPaymentRepository, UserClient userClient) {
        this.pendingPaymentRepository = pendingPaymentRepository;
        this.userClient = userClient;
    }

    public Map<String, Object> createPayment(String orderId, String amount, String orderInfo) throws Exception {

        // 1. Các tham số cơ bản
        String requestId = String.valueOf(System.currentTimeMillis());
        String requestType = "captureWallet";
        String extraData = ""; // Lưu ý: Encode Base64 nếu có dữ liệu

        // 2. Tạo chuỗi raw data để hash (QUAN TRỌNG: Phải đúng thứ tự a-z)
        // Format: accessKey=$&amount=$&extraData=$&ipnUrl=$&orderId=$&orderInfo=$&partnerCode=$&redirectUrl=$&requestId=$&requestType=$
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        // 3. Tạo chữ ký
        String signature = MomoUtil.hmacSHA256(rawHash, secretKey);

        // 4. Tạo JSON body để gửi đi
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("requestType", requestType);
        requestBody.put("extraData", extraData);
        requestBody.put("lang", "vi");
        requestBody.put("signature", signature);

        // 5. Gửi request đến MoMo
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Nhận kết quả
        Map<String, Object> response = restTemplate.postForObject(momoEndpoint, request, Map.class);

        return response; // Trong này sẽ chứa payUrl
    }

    public String createMomoTopupPayment(HttpServletRequest request) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();

        UserResponse userResponse = userClient.getUserById(userId);
        if (userResponse == null) {
            log.error("Author with ID {} not found", userId);
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        long amount = Long.parseLong(request.getParameter("amount"));
        String orderId = VNPayUtil.getRandomNumber(9);
        String orderInfo = "Thanh toan don hang: " + orderId;

        PendingPayment pendingPayment = PendingPayment.builder()
                .txnRef(orderId)
                .userId(userId)
                .amount((int) amount)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        pendingPaymentRepository.save(pendingPayment);

        Map<String, Object> response = createPayment(orderId, String.valueOf(amount), orderInfo);

        if (response != null && response.containsKey("payUrl")) {
            return response.get("payUrl").toString();
        }
        throw new AppException(ErrorCode.TOP_UP_ERROR);
    }
}
