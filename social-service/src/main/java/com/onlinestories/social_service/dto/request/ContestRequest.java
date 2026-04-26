package com.onlinestories.social_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContestRequest {
    String title;
    String topic;
    String description;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
