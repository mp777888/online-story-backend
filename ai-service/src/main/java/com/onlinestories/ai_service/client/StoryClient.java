package com.onlinestories.ai_service.client;

import com.onlinestories.common.chapter.dto.ChapterDTOResponse;
import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.common.story.dto.GenreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "story-service")
public interface StoryClient {
    @GetMapping("api/stories/internal/get-chapter-content")
    ChapterDTOResponse getChapterData(@RequestParam("chapterId") String chapterId);

    @GetMapping("api/stories/genres/all")
    ApiResponse<List<GenreResponse>> getAllGenres();
}
