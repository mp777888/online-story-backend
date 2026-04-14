package com.onlinestories.common.kafka;

public class KafkaTopics {
    private KafkaTopics() {}

    public static final String STORY_UPDATED = "story.updated.v1";
    public static final String STORY_DELETED = "story.deleted.v1";
    public static final String STORY_METRICS_UPDATED = "story.metrics-updated.v1";

    public static final String CHAPTER_CREATED = "chapter.created.v1";
    public static final String CHAPTER_PUBLISHED = "chapter.published.v1";
    public static final String CHAPTER_DELETED = "chapter.deleted.v1";

    public static final String COMMENT_ADDED = "interaction.comment-added.v1";
    public static final String RATING_ADDED = "interaction.rating-added.v1";

    public static final String CHAPTER_READ = "analytics.chapter-read.v1";

    public static final String REPORT_RESPONDED = "report.responded.v1";

    public static final String USER_CREATED = "user.created.v1";
    public static final String USER_UPDATED = "user.updated.v1";

}
