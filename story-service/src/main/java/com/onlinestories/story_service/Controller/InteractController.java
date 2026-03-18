package com.onlinestories.story_service.Controller;

import com.onlinestories.story_service.DTO.Request.CommentRequest;
import com.onlinestories.story_service.DTO.Response.CommentResponse;
import com.onlinestories.story_service.Exception.ApiResponse;
import com.onlinestories.story_service.Service.InteractService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/stories/interact")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InteractController {
    InteractService interactService;

    @PostMapping("/comment")
    public ApiResponse<CommentResponse> addComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CommentRequest request) {
        String userId = jwt.getSubject();
        log.info("Received add comment request: {}", request);
        return ApiResponse.<CommentResponse>builder()
                .code(200)
                .message("Comment added successfully")
                .result(interactService.addComment(userId,request))
                .build();
    }

    @GetMapping("/comment")
    public ApiResponse<CommentResponse> getComments(@RequestParam String commentId) {
        log.info("Received get comment request for commentId: {}", commentId);
        return ApiResponse.<CommentResponse>builder()
                .code(200)
                .message("Comments retrieved successfully")
                .result(interactService.getCommentById(commentId))
                .build();
    }

    @GetMapping("/chapter-comments")
    public ApiResponse<Page<CommentResponse>> getChapterComments(
            @RequestParam String chapterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received get chapter comments request for chapterId: {}, page: {}, size: {}",
                chapterId, page, size);
        return ApiResponse.<Page<CommentResponse>>builder()
                .code(200)
                .message("Chapter comments retrieved successfully")
                .result(interactService.getCommentsByChapterId(chapterId, page, size))
                .build();
    }

    @DeleteMapping("/comment")
    public ApiResponse<String> deleteComment(@RequestParam String commentId) {
        log.info("Received delete comment request for commentId: {}", commentId);
        interactService.deleteComment(commentId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Comment deleted successfully")
                .result("Comment with id " + commentId + " has been deleted")
                .build();
    }
}
