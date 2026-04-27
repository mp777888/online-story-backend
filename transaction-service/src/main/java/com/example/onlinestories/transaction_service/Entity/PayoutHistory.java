package com.example.onlinestories.transaction_service.Entity;

import com.example.onlinestories.transaction_service.Enums.PayoutStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@CompoundIndex(name = "author_month_idx", def = "{'authorId': 1, 'payoutMonth': 1}", unique = true)
public class PayoutHistory {
    @Id
    String payId;
    String authorId;
    String payoutMonth; // Format: "YYYY-MM"
    long totalViews;
    double earnedTokens;
    PayoutStatus status;
    LocalDateTime executedAt;
}
