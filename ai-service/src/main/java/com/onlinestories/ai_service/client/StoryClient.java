package com.onlinestories.ai_service.client;

import com.onlinestories.common.chapter.dto.ChapterDTOResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "story-service")
public interface StoryClient {
    @GetMapping("api/stories/internal/get-chapter-content")
    ChapterDTOResponse getChapterData(@RequestParam("chapterId") String chapterId);
}
