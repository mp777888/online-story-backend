package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.Client.MediaClient;
import com.onlinestories.story_service.Client.UserClient;
import com.onlinestories.story_service.DTO.Request.CreateStoryRequest;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.DTO.Response.UserResponse;
import com.onlinestories.story_service.Entity.Genre;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Enum.Status;
import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Repository.GenreRepository;
import com.onlinestories.story_service.Repository.StoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoryService {
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    GenreRepository genreRepository;
    UserClient userClient;
    MediaClient mediaClient;
    Logger logger = LoggerFactory.getLogger(StoryService.class);

    public ResponseEntity<StoryResponse> createNewStory(CreateStoryRequest request, String authorId, MultipartFile img) {
        try {
            log.info("Creating new story: {}", request.getTitle());

            // Validate author existence via UserClient
            UserResponse userResponse = userClient.getUserById(authorId);
            if (userResponse == null) {
                log.error("Author with ID {} not found", authorId);
                return ResponseEntity.badRequest().build();
            }

            Story story = Story.builder()
                    .authorId(authorId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .img(img != null ? mediaClient.uploadFile(img,"cover-img") : null)
                    .status(Status.ONGOING)
                    .genres(request.getGenreIds().stream()
                            .map(genreId -> genreRepository.findById(genreId).orElseThrow(() -> new RuntimeException("Genre not found: " + genreId)))
                            .filter(Objects::nonNull)
                            .collect(java.util.stream.Collectors.toSet()))
                    .build();
            storyRepository.save(story);
            log.info("Story {} created successfully", request.getTitle());

            StoryResponse response = StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .authorId(story.getAuthorId())
                    .title(story.getTitle())
                    .description(story.getDescription())
                    .img(story.getImg())
                    .status(story.getStatus().name())
                    .genres(story.getGenres().stream().map(Genre::getName).collect(java.util.stream.Collectors.toSet()))
                    .build();

            return ResponseEntity.ok().body(response);
        }
        catch (Exception ex) {
            logger.error("Error creating new story: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    // Get story details
    public ResponseEntity<?> getStoryDetails(String storyId) {
        return null;
    }


    // Get stories by filters: genre, author, status
    public ResponseEntity<?> getStoriesBy(){
        return null;
    }

}
