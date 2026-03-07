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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.stream.Collectors;

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
                    .img(img == null || img.isEmpty()
                            ? null : mediaClient.uploadFile(img,"cover-img"))
                    .status(Status.ONGOING)
                    .numberOfChapters(0)
                    .isPublished(false)
                    .genres(request.getGenreIds().stream()
                            .map(genreId -> genreRepository.findById(genreId)
                                    .orElseThrow(() -> new RuntimeException("Genre not found: " + genreId)))
                            .map(genre -> new Story.GenreSummary(genre.getGenreId(), genre.getName()))
                            .collect(Collectors.toSet()))
                    .build();
            storyRepository.save(story);
            log.info("Story {} created successfully", request.getTitle());

            StoryResponse response = StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .authorId(story.getAuthorId())
                    .title(story.getTitle())
                    .description(story.getDescription())
                    .img(story.getImg())
                    .isPublished(story.getIsPublished())
                    .status(story.getStatus().name())
                    .numberOfChapters(story.getNumberOfChapters())
                    .genres(story.getGenres().stream()
                            .map(Story.GenreSummary::getName)
                            .collect(Collectors.toSet()))
                    .build();

            return ResponseEntity.ok().body(response);
        }
        catch (Exception ex) {
            logger.error("Error creating new story: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public ResponseEntity<Page<StoryResponse>> getUserStories(
            String userId, int page, int size) {
        try{
            log.info("Fetching stories for user: {}, page: {}, size: {}", userId, page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<StoryResponse> storyPage = storyRepository.findByAuthorIdAndIsPublishedTrue(userId, pageable)
                    .map(story -> StoryResponse.builder()
                            .storyId(story.getStoryId())
                            .authorId(story.getAuthorId())
                            .title(story.getTitle())
                            .description(story.getDescription())
                            .img(story.getImg())
                            .status(story.getStatus().name())
                            .numberOfChapters(story.getNumberOfChapters())
                            .isPublished(story.getIsPublished())
                            .genres(story.getGenres()
                                    .stream()
                                    .map(Story.GenreSummary::getName)
                                    .collect(Collectors.toSet()))
                            .build());

            log.info("Fetched {} stories for user {}", storyPage.getTotalElements(), userId);
            return ResponseEntity.ok().body(storyPage);

        }
        catch(Exception ex){
            logger.error("Error fetching stories for user {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    // Get story details
    public ResponseEntity<?> getStoryDetails(String storyId) {
        return null;
    }


    public ResponseEntity<Page<StoryResponse>> getMyStories(
            String userId, int page, int size) {
        try{
            log.info("Fetching my stories for user: {}, page: {}, size: {}", userId, page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<StoryResponse> storyPage = storyRepository.findByAuthorId(userId, pageable)
                    .map(story -> StoryResponse.builder()
                            .storyId(story.getStoryId())
                            .authorId(story.getAuthorId())
                            .title(story.getTitle())
                            .description(story.getDescription())
                            .img(story.getImg())
                            .status(story.getStatus().name())
                            .numberOfChapters(story.getNumberOfChapters())
                            .isPublished(story.getIsPublished())
                            .genres(story.getGenres()
                                    .stream()
                                    .map(Story.GenreSummary::getName)
                                    .collect(Collectors.toSet()))
                            .build());

            log.info("Fetched {} stories for user {}", storyPage.getTotalElements(), userId);
            return ResponseEntity.ok().body(storyPage);

        }
        catch(Exception ex){
            logger.error("Error fetching my stories for user {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    // Get stories by filters: genre, author, status
    public ResponseEntity<?> getStoriesBy(){
        return null;
    }

}
