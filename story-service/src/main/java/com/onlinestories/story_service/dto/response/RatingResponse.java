package com.onlinestories.story_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {
    String voteId;
    String userId;
    String storyId;
    double ratingScore;
    String comment;
    LocalDateTime ratedAt;

}
