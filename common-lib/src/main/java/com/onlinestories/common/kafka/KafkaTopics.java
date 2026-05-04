package com.onlinestories.common.kafka;

public class KafkaTopics {
    private KafkaTopics() {}

    public static final String STORY_UPDATED = "story.updated.v1";
    public static final String STORY_METRICS_UPDATED = "story.metrics-updated.v1";
    public static final String PAYOUT_PROCESSED = "payout.processed.v1";

    public static final String CHAPTER_PUBLISHED = "chapter.published.v1";
    public static final String CHAPTER_PUBLISHED_APPROVED = "chapter.published.v2";
    public static final String CHAPTER_SYNC = "chapter.sync.v1";

    public static final String REPORT_RESPONDED = "report.responded.v1";

    public static final String USER_CREATED = "user.created.v1";
    public static final String USER_UPDATED = "user.updated.v1";

    public static final String TRANSACTION_EVENT = "transaction.event.v1";
}
