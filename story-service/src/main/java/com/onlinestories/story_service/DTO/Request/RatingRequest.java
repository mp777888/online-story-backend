package com.onlinestories.story_service.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingRequest {
    String storyId;
    double ratingScore;
    String comment;
}
