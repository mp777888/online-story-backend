package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.Client.MediaClient;
import com.onlinestories.story_service.Client.UserClient;
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
import com.onlinestories.story_service.Repository.ChapterDraftRepository;
import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Repository.ChapterVersionRepository;
import com.onlinestories.story_service.Repository.StoryRepository;
import com.onlinestories.story_service.Utils.ByteArrayMultipartFile;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChapterService {
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    ChapterVersionRepository chapterVersionRepository;
    ChapterDraftRepository chapterDraftRepository;
    AzureTtsService azureTtsService;
    MediaClient mediaClient;
    MongoTemplate mongoTemplate;

    // Create new chapters
    public ResponseEntity<ChapterResponse> creteNewChapter(CreateChapterRequest request, MultipartFile img) {
        try{
            log.info("Creating new chapter: {}", request.getTitle());
            if(!storyRepository.existsById(request.getStoryId())){
                log.warn("Story not found with ID: {}", request.getStoryId());
                throw new RuntimeException("Story not found with ID: " + request.getStoryId());
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


            ChapterResponse response = ChapterResponse.builder()
                    .chapterId(chapter.getChapterId())
                    .storyId(chapter.getStoryId())
                    .title(chapter.getTitle())
                    .img(chapter.getImg())
                    .createdAt(chapter.getCreatedAt())
                    .build();

            log.info("Chapter {} created successfully", request.getTitle());
            return ResponseEntity.ok().body(response);
        }
        catch(Exception e){
            log.error("Error creating chapter: {}", e.getMessage());
            throw e;
        }
    }

    // Get chapter details
    public ResponseEntity<ChapterResponse> getChapterDetails(String chapterId) {
        try {
            log.info("Fetching chapter details for chapterId: {}", chapterId);
            Chapter chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new RuntimeException("Chapter not found with ID: " + chapterId));
            ChapterResponse response = ChapterResponse.builder()
                    .chapterId(chapter.getChapterId())
                    .storyId(chapter.getStoryId())
                    .title(chapter.getTitle())
                    .status(chapter.getStatus().name())
                    .img(chapter.getImg())
                    .createdAt(chapter.getCreatedAt())
                    .build();
            log.info("Chapter details fetched successfully for chapterId: {}", chapterId);
            return ResponseEntity.ok().body(response);
        }catch (Exception e){
            log.error("Error fetching chapter details: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<ChapterResponse> updateChapter(UpdateChapterRequest request, MultipartFile img) {
        try{
            log.info("Updating chapter: {}", request.getChapterId());
            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new RuntimeException("Chapter not found with ID: " + request.getChapterId()));

            if(request.getTitle() != null && !request.getTitle().isEmpty()) {
                chapter.setTitle(request.getTitle());
            }

            if(request.getStatus() != null && !request.getStatus().isEmpty()) {
                String status = request.getStatus().toUpperCase();
                if(status.equals("TAKEN_DOWN")){
                    Story story = storyRepository.findById(chapter.getStoryId())
                            .orElseThrow(() -> new RuntimeException("Story not found with ID: " + chapter.getStoryId()));

                    story.setNumberOfChapters(story.getNumberOfChapters() - 1);
                    storyRepository.save(story);
                }
                chapter.setStatus(ChapterStatus.valueOf(status));
            }

            if(img != null && !img.isEmpty()) {
                String message = mediaClient.deleteFile(chapter.getImg(), "image");
                log.info("Old chapter image deleted successfully: {}", message);
                String imgUrl = mediaClient.uploadFile(img, "chapter-img");
                chapter.setImg(imgUrl);
            }
            chapterRepository.save(chapter);
            ChapterResponse response = ChapterResponse.builder()
                    .chapterId(chapter.getChapterId())
                    .storyId(chapter.getStoryId())
                    .title(chapter.getTitle())
                    .img(chapter.getImg())
                    .status(chapter.getStatus().name())
                    .createdAt(chapter.getCreatedAt())
                    .build();
            log.info("Chapter {} updated successfully", request.getChapterId());
            return ResponseEntity.ok().body(response);
        }
        catch (Exception e){
            log.error("Error updating chapter: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public ResponseEntity<String> deleteChapter(String chapterId) {
        try{
            log.info("Deleting chapter with ID: {}", chapterId);
            if(!chapterRepository.existsByChapterId(chapterId)){
                log.warn("Chapter not found with ID: {}", chapterId);
                throw new RuntimeException("Chapter not found with ID: " + chapterId);
            }
            chapterRepository.deleteById(chapterId);
            log.info("Chapter with ID: {} deleted successfully", chapterId);

            chapterDraftRepository.deleteByChapterId(chapterId);
            log.info("Chapter draft for chapterId: {} deleted successfully", chapterId);

            chapterVersionRepository.deleteByChapterId(chapterId);
            log.info("Chapter versions for chapterId: {} deleted successfully", chapterId);
            return ResponseEntity.ok().body("Chapter deleted successfully");
        }
        catch (Exception e){
            log.error("Error deleting chapter: {}", e.getMessage());
            throw e;
        }
    }

    // Create chapter drafts
    public ResponseEntity<DraftResponse> createDraft(String chapterId){
        try{
            if(chapterDraftRepository.existsByChapterId(chapterId)){
                log.warn("Chapter draft already exists for chapterId: {}", chapterId);
                throw new RuntimeException("Chapter draft already exists for chapterId: " + chapterId);
            }

            log.info("Creating chapter draft for chapterId: {}", chapterId);
            ChapterDraft draft = ChapterDraft.builder()
                    .chapterId(chapterId)
                    .content("")
                    .lastSavedAt(LocalDateTime.now())
                    .build();
            chapterDraftRepository.save(draft);
            log.info("Chapter draft created successfully for chapterId: {}", chapterId);
            return ResponseEntity.ok().body(DraftResponse.builder()
                    .draftId(draft.getChapterDraftId())
                    .chapterId(draft.getChapterId())
                    .content(draft.getContent())
                    .lastSavedAt(draft.getLastSavedAt())
                    .build());
        }
        catch (Exception e){
            log.error("Error creating chapter draft: {}", e.getMessage());
            throw e;
        }
    }

    // Save chapter drafts
    public ResponseEntity<String> autoSaveDraft(String chapterId, String content) {
        try{
            Query query = new Query(Criteria.where("chapterId").is(chapterId));
            Update update = new Update()
                    .set("content", content)
                    .set("lastSavedAt", LocalDateTime.now());
            mongoTemplate.upsert(query, update, ChapterDraft.class);

            log.info("Chapter draft auto-saved successfully for chapterId: {}", chapterId);
            return ResponseEntity.ok().body("Chapter draft auto-saved successfully");
        }
        catch (Exception e){
            log.error("Error auto-saving chapter draft: {}", e.getMessage());
            throw e;
        }
    }

    // Get chapter drafts
    public ResponseEntity<DraftResponse> getChapterDraft(String chapterId) {
        try{
            log.info("Fetching chapter draft for chapterId: {}", chapterId);
            ChapterDraft draft = chapterDraftRepository.findByChapterId(chapterId);
            if(draft == null){
                log.warn("Chapter draft not found for chapterId: {}", chapterId);
                return ResponseEntity.status(404).build();
            }


            log.info("Chapter draft fetched successfully for chapterId: {}", chapterId);
            return ResponseEntity.ok().body(DraftResponse.builder()
                    .draftId(draft.getChapterDraftId())
                    .chapterId(draft.getChapterId())
                    .content(draft.getContent())
                    .lastSavedAt(draft.getLastSavedAt())
                    .build());
        } catch (Exception e){
            log.error("Error fetching chapter draft: {}", e.getMessage());
            throw e;
        }
    }

    // Create Version Snapshot
    public ResponseEntity<VersionResponse> createChapterVersionSnapshot(String chapterId, String versionName) {
        try{
            log.info("Creating chapter version snapshot for chapterId: {}, versionName: {}", chapterId, versionName);
            ChapterDraft draft = chapterDraftRepository.findByChapterId(chapterId);

            if (draft == null) {
                log.warn("Chapter draft not found for chapterId: {}", chapterId);
                return ResponseEntity.status(404).build();
            }

            ChapterVersion version = ChapterVersion.builder()
                    .chapterId(chapterId)
                    .versionName(versionName)
                    .content(draft.getContent())
                    .createdAt(LocalDateTime.now())
                    .build();
            chapterVersionRepository.save(version);
            log.info("Chapter version snapshot created successfully for chapterId: {}", chapterId);
            return ResponseEntity.ok().body(VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .createdAt(version.getCreatedAt())
                    .build());
        } catch (Exception e){
            log.error("Error creating chapter version snapshot: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<VersionResponse> getChapterVersionDetails(String chapterId) {
        try{
            log.info("Fetching chapter version details for chapterId: {}", chapterId);
            ChapterVersion version = chapterVersionRepository.findByChapterIdAndIsPublished(chapterId);

            if(version == null){
                log.warn("Published chapter version not found for chapterId: {}", chapterId);
                return ResponseEntity.status(404).build();
            }

            log.info("Chapter version details fetched successfully for chapterId: {}", chapterId);
            return ResponseEntity.ok().body(VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .content(version.getContent())
                    .createdAt(version.getCreatedAt())
                    .build());
        } catch (Exception e){
            log.error("Error fetching chapter version details: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<VersionResponse> getChapterVersion(String versionId) {
        try{
            log.info("Fetching chapter version details for versionId: {}", versionId);
            ChapterVersion version = chapterVersionRepository.findById(versionId)
                    .orElseThrow(() -> new RuntimeException("Chapter version not found for versionId: " + versionId));

            log.info("Chapter version details fetched successfully for versionId: {}", versionId);
            return ResponseEntity.ok().body(VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .content(version.getContent())
                    .createdAt(version.getCreatedAt())
                    .build());
        } catch (Exception e){
            log.error("Error fetching chapter version details: {}", e.getMessage());
            throw e;
        }
    }

    // Get chapter versions
    public ResponseEntity<Page<VersionResponse>> getChapterVersionList(
            String chapterId, int page, int size) {
        try{
            log.info("Fetching chapter versions for chapterId: {}", chapterId);
            Pageable pageable = PageRequest.of(page, size);
            Page<VersionResponse> versionPage = chapterVersionRepository.findByChapterId(chapterId,pageable)
                    .map(version -> VersionResponse.builder()
                            .versionId(version.getChapterVersionId())
                            .chapterId(version.getChapterId())
                            .versionName(version.getVersionName())
                            .createdAt(version.getCreatedAt())
                            .build());
            log.info("Fetched {} chapter versions for chapterId: {}", versionPage.getTotalElements(), chapterId);
            return ResponseEntity.ok().body(versionPage);
        } catch (Exception e){
            log.error("Error fetching chapter versions: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<VersionResponse> updateChapterVersion(
            String versionId, String versionName, String content) {
        try {
            log.info("Updating chapter version with ID: {}", versionId);
            ChapterVersion version = chapterVersionRepository.findById(versionId)
                    .orElseThrow(() -> new RuntimeException("Chapter version not found with ID: " + versionId));

            if (versionName != null && !versionName.isEmpty()) {
                version.setVersionName(versionName);
            }
            if (content != null && !content.isEmpty()) {
                version.setContent(content);
            }
            chapterVersionRepository.save(version);
            log.info("Chapter version with ID: {} updated successfully", versionId);
            return ResponseEntity.ok().body(VersionResponse.builder()
                    .versionId(version.getChapterVersionId())
                    .chapterId(version.getChapterId())
                    .versionName(version.getVersionName())
                    .content(version.getContent())
                    .createdAt(version.getCreatedAt())
                    .build());
        } catch (Exception e) {
            log.error("Error updating chapter version: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<String> deleteChapterVersion(String versionId) {
        try{
            log.info("Deleting chapter version with ID: {}", versionId);
            if(!chapterVersionRepository.existsById(versionId)){
                log.warn("Chapter version not found with ID: {}", versionId);
                throw new RuntimeException("Chapter version not found with ID: " + versionId);
            }
            chapterVersionRepository.deleteById(versionId);
            log.info("Chapter version with ID: {} deleted successfully", versionId);
            return ResponseEntity.ok().body("Chapter version deleted successfully");
        } catch (Exception e){
            log.error("Error deleting chapter version: {}", e.getMessage());
            throw e;
        }
    }

    // Publish chapter
    @Transactional
    public ResponseEntity<ChapterResponse> publishChapter(PublishRequest request) {
        try{
            log.info("Publishing chapter with ID: {}", request.getChapterId());
            Story story = storyRepository.findById(request.getStoryId())
                    .orElseThrow(() -> new RuntimeException("Story not found with ID: " + request.getStoryId()));

            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new RuntimeException("Chapter not found with ID: " + request.getChapterId()));

            ChapterVersion version = chapterVersionRepository.findById(request.getChapterVersionId())
                    .orElseThrow(() -> new RuntimeException("Chapter version not found with ID: " + request.getChapterVersionId()));

            if(chapter.getStatus() == ChapterStatus.PUBLISHED){
                log.warn("Chapter with ID: {} is already published", request.getChapterId());
                throw new RuntimeException("Chapter is already published");
            }

            chapter.setStatus(ChapterStatus.PUBLISHED);

            if(chapter.getAudioUrl() != null && !chapter.getAudioUrl().isEmpty()){
                String message = mediaClient.deleteFile(chapter.getAudioUrl(), "audio");
                log.info("Old chapter audio deleted successfully: {}", message);
            }
            chapter.setAudioUrl(getAudioUrl(version.getContent(), "vn-VN"));
            chapterRepository.save(chapter);

            version.setIsPublished(true);
            chapterVersionRepository.save(version);

            story.setNumberOfChapters(story.getNumberOfChapters() + 1);
            storyRepository.save(story);
            log.info("Chapter with ID: {} published successfully", request.getChapterId());

            ChapterResponse response = ChapterResponse.builder()
                    .chapterId(chapter.getChapterId())
                    .storyId(chapter.getStoryId())
                    .title(chapter.getTitle())
                    .img(chapter.getImg())
                    .status(chapter.getStatus().name())
                    .createdAt(chapter.getCreatedAt())
                    .build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e){
            log.error("Error publishing chapter: {}", e.getMessage());
            throw e;
        }

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

}
