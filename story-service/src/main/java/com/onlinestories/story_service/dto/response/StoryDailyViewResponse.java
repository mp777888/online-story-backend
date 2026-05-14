package com.onlinestories.story_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoryDailyViewResponse {
    String id;
    String storyId;
    LocalDate date;
    int viewCount;

}
