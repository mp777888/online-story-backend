package com.onlinestories.story_service.Controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.story_service.DTO.Request.CommentRequest;
import com.onlinestories.story_service.DTO.Request.RatingRequest;
import com.onlinestories.story_service.DTO.Response.CommentResponse;
import com.onlinestories.story_service.DTO.Response.RatingResponse;
import com.onlinestories.story_service.Service.InteractService;
import com.onlinestories.story_service.Service.InternalService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/stories/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InternalController {
    InternalService internalService;

    @GetMapping("/check-story")
    public Boolean checkStoryExistence(@RequestParam String storyId) {
        log.info("Received check story existence request for storyId: {}", storyId);
        return internalService.checkStoryExistence(storyId);
    }

    @GetMapping("/check-chapter")
    public Boolean checkChapterExistence(@RequestParam String chapterId) {
        log.info("Received check chapter existence request for chapterId: {}", chapterId);
        return internalService.checkChapterExistence(chapterId);
    }

    @GetMapping("/check-comment")
    public Boolean checkCommentExistence(@RequestParam String commentId) {
        log.info("Received check comment existence request for commentId: {}", commentId);
        return internalService.checkCommentExistence(commentId);
    }
}
