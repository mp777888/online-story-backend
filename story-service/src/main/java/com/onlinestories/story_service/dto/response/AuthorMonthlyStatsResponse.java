package com.onlinestories.story_service.dto.response;

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
    long totalPublishedStories;
    long totalPublishedChapters;
    double estimatedIncome;
    List<TopStoryStat> topStories;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopStoryStat {
        String storyId;
        String title;
        long periodViews;
        int numberOfChapters;
    }
}