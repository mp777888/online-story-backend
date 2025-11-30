package com.example.onlinestories.transaction_service.Service;

import com.example.onlinestories.transaction_service.util.MomoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


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

    public Map<String, Object> createPayment(String orderId, String amount, String orderInfo, String returnUrl, String notifyUrl) throws Exception {

        // 1. Các tham số cơ bản
        String requestId = String.valueOf(System.currentTimeMillis());
        String requestType = "captureWallet";
        String extraData = ""; // Lưu ý: Encode Base64 nếu có dữ liệu

        // 2. Tạo chuỗi raw data để hash (QUAN TRỌNG: Phải đúng thứ tự a-z)
        // Format: accessKey=$&amount=$&extraData=$&ipnUrl=$&orderId=$&orderInfo=$&partnerCode=$&redirectUrl=$&requestId=$&requestType=$
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
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
        requestBody.put("redirectUrl", returnUrl);
        requestBody.put("ipnUrl", notifyUrl);
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
}
