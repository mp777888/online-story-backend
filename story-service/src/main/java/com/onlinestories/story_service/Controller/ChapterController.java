package com.onlinestories.story_service.Controller;

import com.onlinestories.story_service.DTO.Request.CreateChapterRequest;
import com.onlinestories.story_service.DTO.Request.PublishRequest;
import com.onlinestories.story_service.DTO.Request.UpdateChapterRequest;
import com.onlinestories.story_service.DTO.Response.ChapterResponse;
import com.onlinestories.story_service.DTO.Response.DraftResponse;
import com.onlinestories.story_service.DTO.Response.ReadingHistoryResponse;
import com.onlinestories.story_service.DTO.Response.VersionResponse;
import com.onlinestories.story_service.Service.ReadingService;
import com.onlinestories.story_service.Service.WritingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/stories/chapters")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChapterController {
    WritingService writingService;
    ReadingService readingService;

    @PostMapping
    public ResponseEntity<?> createNewChapter(
            @RequestPart CreateChapterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img){
        log.info("Received request to create new chapter: {}", request.getTitle());
        return writingService.creteNewChapter(request,img);
    }

    @PostMapping("/publish")
    public ResponseEntity<ChapterResponse> publishChapter(
            @RequestBody PublishRequest request){
        log.info("Received request to publish chapter ID: {}", request.getChapterId());
        return writingService.publishChapter(request);
    }

    @GetMapping("/details")
    public ResponseEntity<ChapterResponse> getChapterDetails(@RequestParam String chapterId){
        log.info("Received request to fetch chapter details for chapter ID: {}", chapterId);
        return writingService.getChapterDetails(chapterId);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateChapter(
            @RequestPart UpdateChapterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile img){
        log.info("Received request to update chapter: {}", request.getChapterId());
        return writingService.updateChapter(request, img);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteChapter(@RequestParam String chapterId){
        log.info("Received request to delete chapter ID: {}", chapterId);
        return writingService.deleteChapter(chapterId);
    }

    @PostMapping("/draft")
    public ResponseEntity<DraftResponse> createChapterDraft(@RequestParam String chapterId){
        log.info("Received request to create draft for chapter ID: {}", chapterId);
        return writingService.createDraft(chapterId);
    }

    @GetMapping("/draft")
    public ResponseEntity<DraftResponse> getChapterDraft(@RequestParam String chapterId){
        log.info("Received request to fetch draft for chapter ID: {}", chapterId);
        return writingService.getChapterDraft(chapterId);
    }

    @PutMapping("/draft")
    public ResponseEntity<String> autosaveChapterDraft(@RequestParam String chapterId, @RequestParam String content){
        log.info("Received request to auto-save draft for chapter ID: {}", chapterId);
        return writingService.autoSaveDraft(chapterId, content);
    }

    @PostMapping("/version")
    public ResponseEntity<VersionResponse> createChapterVersion(
            @RequestParam String chapterId,
            @RequestParam String versionName){
        log.info("Received request to create version for chapter ID: {}", chapterId);
        return writingService.createChapterVersionSnapshot(chapterId, versionName);
    }

    @GetMapping("/read")
    public ResponseEntity<VersionResponse> readChapter(@RequestParam String chapterId){
        log.info("Received request to read chapter ID: {}", chapterId);
        return readingService.getContentForReading(chapterId);
    }

    @GetMapping("/version")
    public ResponseEntity<VersionResponse> getChapterVersion(@RequestParam String versionId){
        log.info("Received request to fetch version details for version ID: {}", versionId);
        return writingService.getChapterVersion(versionId);
    }

    @PutMapping("/version")
    public ResponseEntity<VersionResponse> updateChapterVersion(
            @RequestParam String versionId,
            @RequestParam String versionName,
            @RequestParam String content){
        log.info("Received request to update version ID: {}", versionId);
        return writingService.updateChapterVersion(versionId, versionName, content);
    }

    @DeleteMapping("/version")
    public ResponseEntity<String> deleteChapterVersion(@RequestParam String versionId){
        log.info("Received request to delete version ID: {}", versionId);
        return writingService.deleteChapterVersion(versionId);
    }

    @GetMapping("/list-versions")
    public ResponseEntity<Page<VersionResponse>> listChapterVersions(
            @RequestParam String chapterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        log.info("Received request to list versions for chapter ID: {}", chapterId);
        return writingService.getChapterVersionList(chapterId, page, size);
    }

    @PostMapping("/read")
    public ResponseEntity<ReadingHistoryResponse> readChapterAndUpdateHistory(
            @RequestParam String userId,
            @RequestParam String storyId,
            @RequestParam String chapterId){
        log.info("Received request to read chapter ID: {} for user ID: {}", chapterId, userId);
        return ResponseEntity.ok().body(readingService.readChapter(userId, storyId, chapterId));
    }

//    @GetMapping
//    public String getAudioUrl(@RequestParam String text, @RequestParam String language){
//        log.info("Received request to get audio URL for text in language: {}", language);
//        return chapterService.getAudioUrl(text,language); // Placeholder
//    }

}
