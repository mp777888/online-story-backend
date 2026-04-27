package com.example.onlinestories.transaction_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayoutResponse {
    String payId;
    String authorId;
    String payoutMonth; // Format: "YYYY-MM"
    long totalViews;
    double earnedTokens;
    String status;
    LocalDateTime executedAt;
}
