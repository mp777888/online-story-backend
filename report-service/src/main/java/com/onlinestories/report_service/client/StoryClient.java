package com.onlinestories.report_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "story-service")
public interface StoryClient {

    @GetMapping("/api/stories/internal/check-story")
    Boolean checkStoryExistence(@RequestParam String storyId);

    @GetMapping("/api/stories/internal/check-chapter")
    Boolean checkChapterExistence(@RequestParam String chapterId);

    @GetMapping("/api/stories/internal/check-comment")
    Boolean checkCommentExistence(@RequestParam String commentId);

}
