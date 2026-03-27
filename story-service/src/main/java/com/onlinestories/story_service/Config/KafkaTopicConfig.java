package com.onlinestories.story_service.Config;

import com.onlinestories.story_service.Kafka.Topic.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic storyCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.STORY_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic storyUpdatedTopic() {
        return TopicBuilder.name(KafkaTopics.STORY_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic storyDeletedTopic() {
        return TopicBuilder.name(KafkaTopics.STORY_DELETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic chapterCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.CHAPTER_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic chapterPublishedTopic() {
        return TopicBuilder.name(KafkaTopics.CHAPTER_PUBLISHED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic chapterDeletedTopic() {
        return TopicBuilder.name(KafkaTopics.CHAPTER_DELETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic commentAddedTopic() {
        return TopicBuilder.name(KafkaTopics.COMMENT_ADDED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic ratingAddedTopic() {
        return TopicBuilder.name(KafkaTopics.RATING_ADDED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic chapterReadTopic() {
        return TopicBuilder.name(KafkaTopics.CHAPTER_READ).partitions(3).replicas(1).build();
    }
}
