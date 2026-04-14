package com.onlinestories.story_service.Utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoryHelper {
    static String DIRTY_STORIES_KEY = "dirty_stories_metrics";
    StringRedisTemplate stringRedisTemplate;

    public void markStoryAsDirty(String storyId) {
        stringRedisTemplate.opsForSet().add(DIRTY_STORIES_KEY, storyId);
    }
}
