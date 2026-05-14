package com.example.onlinestories.transaction_service.entity;

import com.example.onlinestories.transaction_service.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingPayment {
    @Id
    String penId;
    @Indexed(unique = true)
    String txnRef;
    String userId;
    int amount;
    PaymentStatus status;
    LocalDateTime createdAt;
    LocalDateTime paidAt;
}
