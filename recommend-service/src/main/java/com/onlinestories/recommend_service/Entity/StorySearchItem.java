package com.onlinestories.recommend_service.Entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StorySearchItem {
    String storyId;
    String title;
    String description;
    String authorId;
    String authorName;
    List<String> genres;
    List<String> tags;
    String coverImg;
    String status;
    int numberOfChapters;
    int numberOfViews;
    double averageRatingScore;
    int totalRatingCount;
    boolean premium;
    int unlockPrice;
    float[] embedding; // Vector từ ngữ nghĩa (AI Model)
}
