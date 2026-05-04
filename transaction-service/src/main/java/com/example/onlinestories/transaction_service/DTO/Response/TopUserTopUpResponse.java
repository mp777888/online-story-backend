package com.example.onlinestories.transaction_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopUserTopUpResponse {
    String id;
    long totalAmount;
}