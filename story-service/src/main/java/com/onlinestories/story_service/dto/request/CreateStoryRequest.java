package com.onlinestories.story_service.dto.request;

import com.onlinestories.story_service.enums.Language;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateStoryRequest {
    String title;
    String description;
    Set<String> genreIds;
    Set<String> tags;
    Language language;
    boolean premium = false;
    int unlockPrice = 0;
}
