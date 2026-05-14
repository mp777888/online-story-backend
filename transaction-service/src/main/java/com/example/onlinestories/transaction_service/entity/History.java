package com.example.onlinestories.transaction_service.entity;

import com.onlinestories.common.transaction.enums.HistoryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class History {
    @Id
    String historyId;
    String userId;
    HistoryStatus status;
    int tokenChange;
    LocalDateTime createdAt;
}
