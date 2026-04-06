package com.onlinestories.story_service.DTO.Request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStoryRequest {
    String storyId;
    String title;
    String description;
    String status;
    Set<String> genreIds;
    Set<String> tags;
    boolean premium;
    int unlockPrice;
}
