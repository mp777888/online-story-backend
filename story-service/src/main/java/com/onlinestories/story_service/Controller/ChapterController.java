package com.onlinestories.story_service.Controller;

import com.onlinestories.story_service.DTO.Request.CreateChapterRequest;
import com.onlinestories.story_service.DTO.Request.PublishRequest;
import com.onlinestories.story_service.DTO.Request.UpdateChapterRequest;
import com.onlinestories.story_service.DTO.Response.*;
import com.onlinestories.story_service.Enum.Period;
import com.onlinestories.story_service.Exception.ApiResponse;
import com.onlinestories.story_service.Service.ReadingService;
import com.onlinestories.story_service.Service.WritingService;
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
            @RequestPart CreateChapterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img){
        log.info("Received request to create new chapter: {}", request.getTitle());
        return ApiResponse.<ChapterResponse>builder()
                .code(200)
                .message("Chapter created successfully")
                .result(writingService.creteNewChapter(request, img))
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
        String userId = jwt.getSubject();
        log.info("Received request to fetch chapter details for chapter ID: {}", chapterId);
        return ApiResponse.<ChapterResponse>builder()
                .code(200)
                .message("Chapter details fetched successfully")
                .result(readingService.getChapterDetails(userId,chapterId))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<ChapterResponse> updateChapter(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart UpdateChapterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img){
        String userId = jwt.getSubject();
        log.info("Received request to update chapter: {}", request.getChapterId());
        return ApiResponse.<ChapterResponse>builder()
                .code(200)
                .message("Chapter updated successfully")
                .result(writingService.updateChapter(userId, request, img))
                .build();
    }

    @DeleteMapping
    public ApiResponse<String> deleteChapter(@RequestParam String chapterId){
        log.info("Received request to delete chapter ID: {}", chapterId);
        writingService.deleteChapter(chapterId);
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
            @RequestParam String userId,
            @RequestParam String storyId,
            @RequestParam String chapterId){
        log.info("Received request to read chapter ID: {} for user ID: {}", chapterId, userId);
        return ApiResponse.<ReadingHistoryResponse>builder()
                .code(200)
                .message("Chapter read and history updated successfully")
                .result(readingService.readChapter(userId, storyId, chapterId))
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

//    @GetMapping
//    public String getAudioUrl(@RequestParam String text, @RequestParam String language){
//        log.info("Received request to get audio URL for text in language: {}", language);
//        return chapterService.getAudioUrl(text,language); // Placeholder
//    }

}
