package com.onlinestories.story_service.DTO.Request;

import com.onlinestories.story_service.Enum.Language;
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
