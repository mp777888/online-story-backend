package com.example.onlinestories.transaction_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HistoryResponse {
    String historyId;
    String status;
    int tokenChange;
    String createdAt;
}
