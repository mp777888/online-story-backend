package com.onlinestories.social_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipateResponse {
    String id;
    String contestId;
    String userId;
    boolean hasParticipated;
    LocalDateTime participatedAt;
}
