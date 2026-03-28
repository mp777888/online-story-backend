package com.onlinestories.user_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String notificationId;
    String userId;
    String message;
    boolean isRead;
    LocalDateTime createdAt;
}
