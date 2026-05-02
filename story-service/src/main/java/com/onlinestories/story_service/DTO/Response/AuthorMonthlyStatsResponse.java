package com.onlinestories.story_service.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorMonthlyStatsResponse {
    String authorId;
    String month;
    long totalViews;
    long totalPublishedChapters;
    double estimatedIncome;
    List<TopStoryStat> topStories;

    @Getter
    @Setter
    @Builder
    public static class TopStoryStat {
        String storyId;
        String title;
        long periodViews;
        int numberOfChapters;
    }
}