package com.onlinestories.story_service.Scheduler;

import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.common.story.event.StoryMetricsSyncEvent;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Kafka.Producer.StoryEventProducer;
import com.onlinestories.story_service.Repository.StoryRepository;
import com.onlinestories.story_service.Utils.StoryHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoryMetricsScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final StoryRepository storyRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StoryHelper storyHelper;
    private final StoryEventProducer storyEventProducer;

    private static final String DIRTY_STORIES_KEY = "dirty_stories_metrics";

    // Chạy mỗi 5 phút (300,000 milliseconds)
    @Scheduled(fixedDelay = 300000)
    public void syncDirtyStoryMetrics() {
        // Lấy tất cả ID đang bị thay đổi ra và xoá luôn cái Set đó đi để đón lượt mới
        Set<String> dirtyIds = stringRedisTemplate.opsForSet().members(DIRTY_STORIES_KEY);
        if (dirtyIds == null || dirtyIds.isEmpty()) {
            return;
        }
        
        // Xóa Set cũ để các thao tác mới viết vào 1 Set mới
        stringRedisTemplate.delete(DIRTY_STORIES_KEY);

        log.info("Found {} dirty stories to sync metrics...", dirtyIds.size());

        for (String storyId : dirtyIds) {
            try {
                // Lấy thông tin mới nhất từ Database
                Story story = storyRepository.findById(storyId).orElse(null);
                if (story != null) {
                    // Tạo event chứa các chỉ số
                    StoryMetricsSyncEvent event = StoryMetricsSyncEvent.builder()
                            .storyId(storyId)
                            .numberOfViews(story.getNumberOfViews())
                            .numberOfChapters(story.getNumberOfChapters())
                            .averageRatingScore(story.getAverageRatingScore())
                            .totalRatingCount(story.getTotalRatingCount())
                            .premium(story.isPremium())
                            .unlockPrice(story.getUnlockPrice())
                            .build();

                    // Gửi event qua Kafka
                    storyEventProducer.storyMetricsUpdatedEvent(event);
                }
            } catch (Exception e) {
                log.error("Failed to sync metrics for story: {}", storyId, e);
                storyHelper.markStoryAsDirty(storyId);
            }
        }
    }
}
