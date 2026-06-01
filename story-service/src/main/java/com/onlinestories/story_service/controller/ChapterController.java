package com.onlinestories.story_service.controller;

import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.common.utils.JwtUtils;
import com.onlinestories.story_service.dto.request.CreateChapterRequest;
import com.onlinestories.story_service.dto.request.ListChapterRequest;
import com.onlinestories.story_service.dto.request.PublishRequest;
import com.onlinestories.story_service.dto.request.UpdateChapterRequest;
import com.onlinestories.story_service.dto.response.*;

import com.onlinestories.story_service.service.ReadingService;
import com.onlinestories.story_service.service.WritingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/stories/chapters")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChapterController {
    WritingService writingService;
    ReadingService readingService;

    @PostMapping
    public ApiResponse<ChapterResponse> createNewChapter(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart CreateChapterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img){
        String userId = jwt.getSubject();
        log.info("Received request to create new chapter: {}", request.getTitle());
        return ApiResponse.<ChapterResponse>builder()
                .code(200)
                .message("Chapter created successfully")
                .result(writingService.creteNewChapter(userId, request, img))
                .build();
    }

    @PostMapping("/publish")
    public ApiResponse<ChapterResponse> publishChapter(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PublishRequest request){
        String userId = jwt.getSubject();
        log.info("Received request to publish chapter ID: {}", request.getChapterId());
        return ApiResponse.<ChapterResponse>builder()
                .code(200)
                .message("Chapter published successfully")
                .result(writingService.publishChapter(userId, request))
                .build();
    }

    @GetMapping("/details")
    public ApiResponse<ChapterResponse> getChapterDetails(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String chapterId){
        String userId = JwtUtils.getSubject(jwt);
        log.info("Received request to fetch chapter details for chapter ID: {}", chapterId);
        return ApiResponse.<ChapterResponse>builder()
                .code(200)
                .message("Chapter details fetched successfully")
                .result(readingService.getChapterDetails(userId,chapterId))
                .build();
    }

    @GetMapping("/id")
    public ApiResponse<ChapterResponse> getChapterById(@RequestParam String chapterId){
        log.info("Received request to fetch chapter by ID: {}", chapterId);
        return ApiResponse.<ChapterResponse>builder()
                .code(200)
                .message("Chapter details fetched successfully")
                .result(readingService.getChapterById(chapterId))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<ChapterResponse> updateChapter(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart UpdateChapterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img){
        String userId = jwt.getSubject();
        boolean isAdmin = JwtUtils.isAdmin(jwt);
        log.info("Received request to update chapter: {}", request.getChapterId());
        return ApiResponse.<ChapterResponse>builder()
                .code(200)
                .message("Chapter updated successfully")
                .result(writingService.updateChapter(userId, isAdmin, request, img))
                .build();
    }

    @DeleteMapping
    public ApiResponse<String> deleteChapter(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String chapterId){
        String userId = jwt.getSubject();
        log.info("Received request to delete chapter ID: {}", chapterId);
        writingService.deleteChapter(userId,chapterId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Chapter deleted successfully")
                .result("Chapter with ID " + chapterId + " has been deleted.")
                .build();
    }

    @PostMapping("/draft")
    public ApiResponse<DraftResponse> createChapterDraft(@RequestParam String chapterId){
        log.info("Received request to create draft for chapter ID: {}", chapterId);
        return ApiResponse.<DraftResponse>builder()
                .code(200)
                .message("Draft created successfully")
                .result(writingService.createDraft(chapterId))
                .build();
    }

    @GetMapping("/draft")
    public ApiResponse<DraftResponse> getChapterDraft(@RequestParam String chapterId){
        log.info("Received request to fetch draft for chapter ID: {}", chapterId);
        return ApiResponse.<DraftResponse>builder()
                .code(200)
                .message("Draft fetched successfully")
                .result(writingService.getChapterDraft(chapterId))
                .build();
    }

    @PutMapping("/draft")
    public ApiResponse<String> autosaveChapterDraft(@RequestParam String chapterId, @RequestParam String content){
        log.info("Received request to auto-save draft for chapter ID: {}", chapterId);
        writingService.autoSaveDraft(chapterId, content);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Draft auto-saved successfully")
                .result("Draft for chapter ID " + chapterId + " has been auto-saved.")
                .build();
    }

    @PostMapping("/version")
    public ApiResponse<VersionResponse> createChapterVersion(
            @RequestParam String chapterId,
            @RequestParam String versionName){
        log.info("Received request to create version for chapter ID: {}", chapterId);
        return ApiResponse.<VersionResponse>builder()
                .code(200)
                .message("Version created successfully")
                .result(writingService.createChapterVersionSnapshot(chapterId, versionName))
                .build();
    }

    @PostMapping("/import-word")
    public ApiResponse<VersionResponse> importWordToDraft(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String versionName,
            @RequestParam String chapterId,
            @RequestPart("file") MultipartFile file) {

        String userId = jwt.getSubject();
        return ApiResponse.<VersionResponse>builder()
                .code(200)
                .message("Word imported to draft successfully")
                .result(writingService.importFile(userId, versionName, chapterId, file))
                .build();
    }


    @GetMapping("/version")
    public ApiResponse<VersionResponse> getChapterVersion(@RequestParam String versionId){
        log.info("Received request to fetch version details for version ID: {}", versionId);
        return ApiResponse.<VersionResponse>builder()
                .code(200)
                .message("Version details fetched successfully")
                .result(writingService.getChapterVersion(versionId))
                .build();
    }

    @PutMapping("/version")
    public ApiResponse<VersionResponse> updateChapterVersion(
            @RequestParam String versionId,
            @RequestParam String versionName,
            @RequestParam String content){
        log.info("Received request to update version ID: {}", versionId);
        return ApiResponse.<VersionResponse>builder()
                .code(200)
                .message("Version updated successfully")
                .result(writingService.updateChapterVersion(versionId, versionName, content))
                .build();
    }

    @DeleteMapping("/version")
    public ApiResponse<String> deleteChapterVersion(@RequestParam String versionId){
        log.info("Received request to delete version ID: {}", versionId);
        writingService.deleteChapterVersion(versionId);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Version deleted successfully")
                .result("Version with ID " + versionId + " has been deleted.")
                .build();
    }

    @GetMapping("/list-versions")
    public ApiResponse<Page<VersionResponse>> listChapterVersions(
            @RequestParam String chapterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        log.info("Received request to list versions for chapter ID: {}", chapterId);
        return ApiResponse.<Page<VersionResponse>>builder()
                .code(200)
                .message("Chapter versions listed successfully")
                .result(writingService.getChapterVersionList(chapterId, page, size))
                .build();
    }

    @PostMapping("/read")
    public ApiResponse<ReadingHistoryResponse> readChapterAndUpdateHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId,
            @RequestParam String chapterId,
            @RequestParam Float progress){
        String userId = JwtUtils.getSubject(jwt);
        log.info("Received request to read chapter ID: {} for user ID: {}", chapterId, userId);
        return ApiResponse.<ReadingHistoryResponse>builder()
                .code(200)
                .message("Chapter read and history updated successfully")
                .result(readingService.readChapter(userId, storyId, chapterId, progress))
                .build();
    }

    @GetMapping("/reading-history")
    public ApiResponse<Page<ReadingHistoryResponse>> getReadingHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        String userId = jwt.getSubject();
        log.info("Received request to fetch reading history for user ID: {}", userId);
        return ApiResponse.<Page<ReadingHistoryResponse>>builder()
                .code(200)
                .message("Reading history fetched successfully")
                .result(readingService.getReadingHistory(userId, page, size))
                .build();
    }

    @GetMapping("/latest-chapter")
    public ApiResponse<ReadingHistoryResponse> getLatestChapter(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String storyId){
        String userId = JwtUtils.getSubject(jwt);
        log.info("Received request to fetch latest chapter for story ID: {}", storyId);
        return ApiResponse.<ReadingHistoryResponse>builder()
                .code(200)
                .message("Latest chapter fetched successfully")
                .result(readingService.getLatestChapter(userId,storyId))
                .build();
    }

    @PutMapping("/free-preview-chapters")
    public ApiResponse<String> choosingFreePreviewChapters(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ListChapterRequest request) {
        String authorId = JwtUtils.getSubject(jwt);
        log.info("Received request to set free preview chapters for story ID: {}, chapter IDs: {}", request.getStoryId(), request.getChapterIds());
        writingService.chooseFreePreviewChapters(authorId, request);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Free preview chapters set successfully")
                .result("Free preview chapters updated successfully")
                .build();
    }

}
