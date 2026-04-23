package com.onlinestories.story_service.Controller;

import com.onlinestories.common.chapter.dto.ChapterDTOResponse;
import com.onlinestories.common.story.dto.StoryDTOResponse;
import com.onlinestories.story_service.Service.InternalService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/stories/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InternalController {
    InternalService internalService;

    @GetMapping("/check-story")
    public StoryDTOResponse checkStoryExistence(@RequestParam String storyId) {
        log.info("Received check story existence request for storyId: {}", storyId);
        return internalService.checkStoryExistence(storyId);
    }

    @GetMapping("/check-chapter")
    public ChapterDTOResponse checkChapterExistence(@RequestParam String chapterId) {
        log.info("Received check chapter existence request for chapterId: {}", chapterId);
        return internalService.checkChapterExistence(chapterId);
    }

    @GetMapping("/check-comment")
    public Boolean checkCommentExistence(@RequestParam String commentId) {
        log.info("Received check comment existence request for commentId: {}", commentId);
        return internalService.checkCommentExistence(commentId);
    }

    @GetMapping("/get-chapter-content")
    public ChapterDTOResponse getChapterData(@RequestParam String chapterId) {
        log.info("Received get chapter content request for chapterId: {}", chapterId);
        return internalService.getChapterData(chapterId);
    }
}
