package com.example.onlinestories.transaction_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingPaymentResponse {
    String penId;
    String txnRef;
    String userId;
    int amount;
    String status;
    LocalDateTime createdAt;
    LocalDateTime paidAt;
}
