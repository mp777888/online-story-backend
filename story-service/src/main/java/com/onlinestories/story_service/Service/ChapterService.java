package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.Client.MediaClient;
import com.onlinestories.story_service.Client.UserClient;
import com.onlinestories.story_service.DTO.Request.CreateChapterRequest;
import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Utils.ByteArrayMultipartFile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChapterService {
    ChapterRepository chapterRepository;
    AzureTtsService azureTtsService;
    MediaClient mediaClient;

    // Create new chapters
    public ResponseEntity<?> creteNewChapter(CreateChapterRequest request){
        try{
            log.info("Creating new chapter: {}", request.getTitle());
            // Implementation for creating a new chapter goes here

            Chapter chapter = Chapter.builder()
                    .storyId(request.getStoryId())
                    .title(request.getTitle())
                    .createdAt(java.time.LocalDateTime.now())
                    .chapterNumber(1) //exist + 1
                    .build();

            chapterRepository.save(chapter);


            log.info("Chapter {} created successfully", request.getTitle());
            return ResponseEntity.ok().build();
        }
        catch(Exception e){
            log.error("Error creating chapter: {}", e.getMessage());
            throw e;
        }
    }

    public String getAudioUrl(String text, String language){
        try {
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

    // Save new versions of chapters
    public ResponseEntity<?> changeChapterName(String name) {
        return null;
    }


    // Save new versions of chapters
    public ResponseEntity<?> saveChapterVersion() {
        return null;
    }

    // Get chapter details
    public ResponseEntity<?> getChapterDetails(String chapterId) {
        return null;
    }

    // Publish chapter
    public ResponseEntity<?> publishChapter(String chapterId) {
        return null;
    }


}
