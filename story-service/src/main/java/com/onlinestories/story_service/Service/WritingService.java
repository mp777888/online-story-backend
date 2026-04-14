package com.onlinestories.story_service.Service;

import com.onlinestories.common.chapter.event.ChapterPublishedEvent;
import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.story_service.Client.MediaClient;
import com.onlinestories.story_service.DTO.Request.CreateChapterRequest;
import com.onlinestories.story_service.DTO.Request.PublishRequest;
import com.onlinestories.story_service.DTO.Request.UpdateChapterRequest;
import com.onlinestories.story_service.DTO.Response.ChapterResponse;
import com.onlinestories.story_service.DTO.Response.DraftResponse;
import com.onlinestories.story_service.DTO.Response.VersionResponse;
import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Entity.ChapterDraft;
import com.onlinestories.story_service.Entity.ChapterVersion;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Enum.ChapterStatus;
import com.onlinestories.story_service.Enum.StoryStatus;


import com.onlinestories.story_service.Kafka.Producer.ChapterEventProducer;
import com.onlinestories.story_service.Repository.ChapterDraftRepository;
import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Repository.ChapterVersionRepository;
import com.onlinestories.story_service.Repository.StoryRepository;
import com.onlinestories.story_service.Utils.ByteArrayMultipartFile;
import com.onlinestories.story_service.Utils.StoryHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WritingService {
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    ChapterVersionRepository chapterVersionRepository;
    ChapterDraftRepository chapterDraftRepository;
    ChapterEventProducer chapterEventProducer;
    AzureTtsService azureTtsService;
    MediaClient mediaClient;
    MongoTemplate mongoTemplate;
    StoryHelper storyHelper;

    // Create new chapters
    public ChapterResponse creteNewChapter(CreateChapterRequest request, MultipartFile img) {
        try{
            log.info("Creating new chapter: {}", request.getTitle());
            if(!storyRepository.existsById(request.getStoryId())){
                log.warn("Story not found with ID: {}", request.getStoryId());
                throw new AppException(ErrorCode.STORY_NOT_FOUND);
            }

            Chapter chapter = Chapter.builder()
                    .storyId(request.getStoryId())
                    .title(request.getTitle())
                    .img(img == null || img.isEmpty()
                            ? null : mediaClient.uploadFile(img,"chapter-img"))
                    .status(ChapterStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .build();

            chapterRepository.save(chapter);
            log.info("Chapter {} created in database with ID: {}", request.getTitle(), chapter.getChapterId());


            return ChapterResponse.builder()
                    .chapterId(chapter.getChapterId())
                    .storyId(chapter.getStoryId())
                    .title(chapter.getTitle())
                    .img(chapter.getImg())
                    .createdAt(chapter.getCreatedAt())
                    .build();
        }
        catch(Exception e){
            log.error("Error creating chapter: {}", e.getMessage());
            throw e;
        }
    }



    public ChapterResponse updateChapter(String userId,UpdateChapterRequest request, MultipartFile img) {
        try{
            log.info("Updating chapter: {}", request.getChapterId());
            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));

            Story story = storyRepository.findById(chapter.getStoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

            if(!story.getAuthorId().equals(userId)){
                log.warn("User with ID: {} is not the author of the story and cannot update chapter details", userId);
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }

            if(request.getTitle() != null && !request.getTitle().isEmpty()) {
                chapter.setTitle(request.getTitle());
            }

            if(request.getStatus() != null && !request.getStatus().isEmpty()) {
                String newStatusStr = request.getStatus().toUpperCase();
                ChapterStatus newStatus = ChapterStatus.valueOf(newStatusStr);

                if(chapter.getStatus() == ChapterStatus.TAKEN_DOWN){
                    log.warn("Chapter with ID: {} is currently taken down, cannot change status except admin", request.getChapterId());
                    throw new AppException(ErrorCode.CHAPTER_IS_TAKEN_DOWN);
                }

                if (chapter.getStatus() == ChapterStatus.PUBLISHED && newStatus != ChapterStatus.PUBLISHED) {
                    story.setNumberOfChapters(story.getNumberOfChapters() - 1);
                    storyRepository.save(story);

                }
                else if (chapter.getStatus() != ChapterStatus.PUBLISHED && newStatus == ChapterStatus.PUBLISHED) {
                    story.setNumberOfChapters(story.getNumberOfChapters() + 1);
                    storyRepository.save(story);
                }

                storyHelper.markStoryAsDirty(story.getStoryId());
                chapter.setStatus(newStatus);
            }


            if(img != null && !img.isEmpty()) {
                if (chapter.getImg() != null && !chapter.getImg().isEmpty()) {
                    String message = mediaClient.deleteFile(chapter.getImg(), "image");
                    log.info("Old chapter image deleted successfully: {}", message);
                }
                String imgUrl = mediaClient.uploadFile(img, "chapter-img");
                chapter.setImg(imgUrl);
            }

            if(request.getPublishedAt() != null){
                ChapterVersion version = chapterVersionRepository.findById(chapter.getPublishedVersionId())
                        .orElse(null);
                if(version == null){
                    log.warn("Cannot set publishedAt for chapterId: {} because it has no published version", request.getChapterId());
                    throw new AppException(ErrorCode.VERSION_NOT_FOUND);
                }
                chapter.setPublishedAt(request.getPublishedAt());
            }

            chapterRepository.save(chapter);
            return ChapterResponse.builder()
                    .chapterId(chapter.getChapterId())
                    .storyId(chapter.getStoryId())
                    .title(chapter.getTitle())
                    .img(chapter.getImg())
                    .status(chapter.getStatus().name())
                    .createdAt(chapter.getCreatedAt())
                    .build();

        }
        catch (Exception e){
            log.error("Error updating chapter: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteChapter(String userId, String chapterId) {
        log.info("Deleting chapter with ID: {}", chapterId);
        Story story = storyRepository.findById(chapterRepository.findById(chapterId)
                        .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND))
                        .getStoryId())
                .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

        if (!story.getAuthorId().equals(userId)) {
            log.warn("User with ID: {} is not the author of the story and cannot delete chapters", userId);
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }


        /* Delete comment, rating, ... do later */
        chapterRepository.deleteById(chapterId);
        log.info("Chapter with ID: {} deleted successfully", chapterId);

        chapterDraftRepository.deleteByChapterId(chapterId);
        log.info("Chapter draft for chapterId: {} deleted successfully", chapterId);

        chapterVersionRepository.deleteByChapterId(chapterId);
        log.info("Chapter versions for chapterId: {} deleted successfully", chapterId);
    }

    // Create chapter drafts
    public DraftResponse createDraft(String chapterId){
        try{
            if(chapterDraftRepository.existsByChapterId(chapterId)){
                log.warn("Chapter draft already exists for chapterId: {}", chapterId);
                throw new AppException(ErrorCode.DRAFT_ALREADY_EXISTS);
            }

            log.info("Creating chapter draft for chapterId: {}", chapterId);
            ChapterDraft draft = ChapterDraft.builder()
                    .chapterId(chapterId)
                    .content("")
                    .lastSavedAt(LocalDateTime.now())
                    .build();
            chapterDraftRepository.save(draft);
            log.info("Chapter draft created successfully for chapterId: {}", chapterId);
            return DraftResponse.builder()
                    .draftId(draft.getChapterDraftId())
                    .chapterId(draft.getChapterId())
                    .content(draft.getContent())
                    .lastSavedAt(draft.getLastSavedAt())
                    .build();
        }
        catch (Exception e){
            log.error("Error creating chapter draft: {}", e.getMessage());
            throw e;
        }
    }

    // Save chapter drafts
    public void autoSaveDraft(String chapterId, String content) {
        try{
            Query query = new Query(Criteria.where("chapterId").is(chapterId));
            Update update = new Update()
                    .set("content", content)
                    .set("lastSavedAt", LocalDateTime.now());
            mongoTemplate.upsert(query, update, ChapterDraft.class);

            log.info("Chapter draft auto-saved successfully for chapterId: {}", chapterId);
        }
        catch (Exception e){
            log.error("Error auto-saving chapter draft: {}", e.getMessage());
            throw e;
        }
    }

    // Get chapter drafts
    public DraftResponse getChapterDraft(String chapterId) {
        try{
            log.info("Fetching chapter draft for chapterId: {}", chapterId);
            ChapterDraft draft = chapterDraftRepository.findByChapterId(chapterId);
            if(draft == null){
                log.warn("Chapter draft not found for chapterId: {}", chapterId);
                throw new AppException(ErrorCode.DRAFT_NOT_FOUND);
            }

            log.info("Chapter draft fetched successfully for chapterId: {}", chapterId);
            return DraftResponse.builder()
                    .draftId(draft.getChapterDraftId())
                    .chapterId(draft.getChapterId())
                    .content(draft.getContent())
                    .lastSavedAt(draft.getLastSavedAt())
                    .build();
        } catch (Exception e){
            log.error("Error fetching chapter draft: {}", e.getMessage());
            throw e;
        }
    }

    // Create Version Snapshot
    public VersionResponse createChapterVersionSnapshot(String chapterId, String versionName) {
        try{
            log.info("Creating chapter version snapshot for chapterId: {}, versionName: {}", chapterId, versionName);
            ChapterDraft draft = chapterDraftRepository.findByChapterId(chapterId);

            if (draft == null) {
                log.warn("Chapter draft not found for chapterId: {}", chapterId);
                throw new AppException(ErrorCode.DRAFT_NOT_FOUND);
            }

            ChapterVersion version = ChapterVersion.builder()
                    .chapterId(chapterId)
                    .versionName(versionName)
                    .content(draft.getContent())
                    .createdAt(LocalDateTime.now())
                    .build();
            chapterVersionRepository.save(version);
            log.info("Chapter version snapshot created successfully for chapterId: {}", chapterId);
            return VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .createdAt(version.getCreatedAt())
                    .build();
        } catch (Exception e){
            log.error("Error creating chapter version snapshot: {}", e.getMessage());
            throw e;
        }
    }


    public VersionResponse getChapterVersion(String versionId) {
        try{
            log.info("Fetching chapter version details for versionId: {}", versionId);
            ChapterVersion version = chapterVersionRepository.findById(versionId)
                    .orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND));

            log.info("Chapter version details fetched successfully for versionId: {}", versionId);
            return VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .content(version.getContent())
                    .createdAt(version.getCreatedAt())
                    .build();
        } catch (Exception e){
            log.error("Error fetching chapter version details: {}", e.getMessage());
            throw e;
        }
    }

    // Get chapter versions
    public Page<VersionResponse> getChapterVersionList(
            String chapterId, int page, int size) {
        try{
            log.info("Fetching chapter versions for chapterId: {}", chapterId);
            Pageable pageable = PageRequest.of(page, size);
            return chapterVersionRepository.findByChapterId(chapterId,pageable)
                    .map(version -> VersionResponse.builder()
                            .versionId(version.getChapterVersionId())
                            .chapterId(version.getChapterId())
                            .versionName(version.getVersionName())
                            .createdAt(version.getCreatedAt())
                            .build());
        } catch (Exception e){
            log.error("Error fetching chapter versions: {}", e.getMessage());
            throw e;
        }
    }

    public VersionResponse updateChapterVersion(
            String versionId, String versionName, String content) {
        try {
            log.info("Updating chapter version with ID: {}", versionId);
            ChapterVersion version = chapterVersionRepository.findById(versionId)
                    .orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND));

            if (versionName != null && !versionName.isEmpty()) {
                version.setVersionName(versionName);
            }
            if (content != null && !content.isEmpty()) {
                version.setContent(content);
            }
            chapterVersionRepository.save(version);
            log.info("Chapter version with ID: {} updated successfully", versionId);
            return VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .content(version.getContent())
                    .createdAt(version.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error updating chapter version: {}", e.getMessage());
            throw e;
        }
    }

    public void deleteChapterVersion(String versionId) {
        try{
            log.info("Deleting chapter version with ID: {}", versionId);
            if(!chapterVersionRepository.existsById(versionId)){
                log.warn("Chapter version not found with ID: {}", versionId);
                throw new AppException(ErrorCode.VERSION_NOT_FOUND);
            }
            chapterVersionRepository.deleteById(versionId);
            log.info("Chapter version with ID: {} deleted successfully", versionId);
        } catch (Exception e){
            log.error("Error deleting chapter version: {}", e.getMessage());
            throw e;
        }
    }

    public VersionResponse importFile(String userId, String versionName, String chapterId, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_FILE);
            }

            String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            if (!filename.endsWith(".docx")) {
                throw new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE);
            }

            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));

            Story story = storyRepository.findById(chapter.getStoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

            if (!story.getAuthorId().equals(userId)) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }

            String importedContent = extractDocxAsHtml(file);
            if (importedContent == null || importedContent.trim().isEmpty()) {
                throw new AppException(ErrorCode.EMPTY_IMPORT_CONTENT);
            }

            // sanitize the imported HTML content to prevent XSS attacks
            String safeContent = Jsoup.clean(importedContent, org.jsoup.safety.Safelist.basic());

            ChapterVersion version = ChapterVersion.builder()
                    .chapterId(chapterId)
                    .versionName(versionName)
                    .content(safeContent)
                    .createdAt(LocalDateTime.now())
                    .build();
            chapterVersionRepository.save(version);

            log.info("Word file imported successfully for chapterId: {}, versionId: {}", chapterId, version.getChapterVersionId());
            return VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .content(version.getContent())
                    .createdAt(version.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error importing Word file to draft: {}", e.getMessage());
            throw e;
        }
    }



    // Publish chapter
    @Transactional
    public ChapterResponse publishChapter(String userId, PublishRequest request) {
        try{
            log.info("Publishing chapter with ID: {}", request.getChapterId());
            Story story = storyRepository.findById(request.getStoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

            if(!story.getAuthorId().equals(userId)){
                log.warn("User with ID: {} is not the author of the story and cannot publish chapters", userId);
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }

            if(story.getStatus().equals(StoryStatus.DRAFT)){
                log.warn("Story with ID: {} is in DRAFT status and cannot publish chapters", request.getStoryId());
                throw new AppException(ErrorCode.STORY_IS_NOT_PUBLISHED);
            }

            if (story.getStatus().equals(StoryStatus.COMPLETED)) {
                log.warn("Story with ID: {} is already completed and cannot publish new chapters", request.getStoryId());
                throw new AppException(ErrorCode.STORY_IS_COMPLETED);
            }

            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));

            if(chapter.getStatus() == ChapterStatus.PUBLISHED){
                log.warn("Chapter with ID: {} is already published", request.getChapterId());
                throw new AppException(ErrorCode.CHAPTER_ALREADY_PUBLISHED);
            }

            ChapterVersion version = chapterVersionRepository.findById(request.getChapterVersionId())
                    .orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND));
            chapter.setPublishedVersionId(version.getChapterVersionId());


            LocalDateTime publishDate = request.getPublishDate();
            if(publishDate != null && publishDate.isAfter(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))){
                chapter.setStatus(ChapterStatus.SCHEDULED);
                chapter.setPublishedAt(publishDate);

                chapterRepository.save(chapter);

                log.info("Chapter {} scheduled to be published at {}", chapter.getChapterId(), publishDate);

                ChapterPublishedEvent event = ChapterPublishedEvent.builder()
                        .chapterId(chapter.getChapterId())
                        .storyId(chapter.getStoryId())
                        .title(chapter.getTitle())
                        .authorId(story.getAuthorId())
                        .eventType("CHAPTER_SCHEDULED")
                        .build();
                chapterEventProducer.publishChapterCreatedEvent(event);
                return ChapterResponse.builder()
                        .chapterId(chapter.getChapterId())
                        .status(chapter.getStatus().name())
                        .publishedAt(chapter.getPublishedAt())
                        .build();
            }


            return doPublishChapter(story, chapter, version);
        } catch (Exception e){
            log.error("Error publishing chapter: {}", e.getMessage());
            throw e;
        }

    }

    @Transactional
    public ChapterResponse doPublishChapter(Story story, Chapter chapter, ChapterVersion version) {
        chapter.setStatus(ChapterStatus.PUBLISHED);
        if(chapter.getAudioUrl() != null && !chapter.getAudioUrl().isEmpty()){
            mediaClient.deleteFile(chapter.getAudioUrl(), "video");
        }
        chapter.setAudioUrl(getAudioUrl(version.getContent(), "vn-VN"));
        chapter.setPublishedAt(LocalDateTime.now());
        chapterRepository.save(chapter);


        story.setNumberOfChapters(story.getNumberOfChapters() + 1);
        storyRepository.save(story);
        storyHelper.markStoryAsDirty(story.getStoryId());

        ChapterPublishedEvent event = ChapterPublishedEvent.builder()
                .chapterId(chapter.getChapterId())
                .storyId(chapter.getStoryId())
                .title(chapter.getTitle())
                .authorId(story.getAuthorId())
                .eventType("CHAPTER_PUBLISHED")
                .build();
        chapterEventProducer.publishChapterCreatedEvent(event);

        log.info("Chapter with ID: {} published automatically/immediately success", chapter.getChapterId());

        return ChapterResponse.builder()
                .chapterId(chapter.getChapterId())
                .storyId(chapter.getStoryId())
                .title(chapter.getTitle())
                .img(chapter.getImg())
                .status(chapter.getStatus().name())
                .createdAt(chapter.getCreatedAt())
                .publishedAt(chapter.getPublishedAt())
                .build();
    }

    private String getAudioUrl(String content, String language){
        try {

            String text = Jsoup.parse(content).text();


            byte[] audioBytes = azureTtsService.synthesizeText(text, language);
            MultipartFile fileToSend = new ByteArrayMultipartFile(
                    audioBytes,
                    "audioData",
                    "chapter_1.mp3",
                    "audio/mpeg"
            );
            String url = mediaClient.uploadFile(fileToSend, "truyen-audio");
            log.info("Text to speech transfer completed successfully.");
            return url;
        }
        catch (Exception e){
            log.error("Error transferring text to speech: {}", e.getMessage());
            throw e;
        }
    }

    private String extractDocxAsHtml(MultipartFile file) {
        try (var is = file.getInputStream();
             var doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(is)) {

            StringBuilder html = new StringBuilder();

            for (var p : doc.getParagraphs()) {
                String text = p.getText();
                if (text == null || text.trim().isEmpty()) continue;

                String style = p.getStyle();
                if (style != null && style.toLowerCase().contains("heading")) {
                    html.append("<h3>").append(org.jsoup.parser.Parser.unescapeEntities(text, false)).append("</h3>");
                } else {
                    html.append("<p>").append(org.jsoup.parser.Parser.unescapeEntities(text, false)).append("</p>");
                }
            }

            return html.toString();
        } catch (Exception ex) {
            throw new AppException(ErrorCode.FILE_IMPORT_FAILED);
        }
    }


}
