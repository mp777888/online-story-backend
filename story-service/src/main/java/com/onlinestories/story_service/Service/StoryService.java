package com.onlinestories.story_service.Service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.story_service.Client.MediaClient;
import com.onlinestories.story_service.Client.UserClient;
import com.onlinestories.story_service.DTO.Request.CreateStoryRequest;
import com.onlinestories.story_service.DTO.Request.UpdateStoryRequest;
import com.onlinestories.story_service.DTO.Response.StoryResponse;
import com.onlinestories.story_service.DTO.Response.UserResponse;
import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Enum.StoryStatus;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoryService {
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    WritingService writingService;
    GenreRepository genreRepository;
    UserClient userClient;
    MediaClient mediaClient;
    Logger logger = LoggerFactory.getLogger(StoryService.class);

    public StoryResponse createNewStory(CreateStoryRequest request, String authorId, MultipartFile img) {
        try {
            log.info("Creating new story: {}", request.getTitle());

            // Validate author existence via UserClient
            UserResponse userResponse = userClient.getUserById(authorId);
            if (userResponse == null) {
                log.error("Author with ID {} not found", authorId);
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            Story story = Story.builder()
                    .authorId(authorId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .img(img == null || img.isEmpty()
                            ? null : mediaClient.uploadFile(img,"cover-img"))
                    .status(StoryStatus.DRAFT)
                    .numberOfChapters(0)
                    .genres(request.getGenreIds().stream()
                            .map(genreId -> genreRepository.findById(genreId)
                                    .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND)))
                            .map(genre -> new Story.GenreSummary(genre.getGenreId(), genre.getName()))
                            .collect(Collectors.toSet()))
                    .build();
            storyRepository.save(story);
            log.info("Story {} created successfully", request.getTitle());

            return StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .authorId(story.getAuthorId())
                    .title(story.getTitle())
                    .description(story.getDescription())
                    .img(story.getImg())
                    .status(story.getStatus().name())
                    .numberOfChapters(story.getNumberOfChapters())
                    .genres(story.getGenres().stream()
                            .map(Story.GenreSummary::getName)
                            .collect(Collectors.toSet()))
                    .build();
        }
        catch (Exception ex) {
            logger.error("Error creating new story: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    // Get story details
    public StoryResponse getStoryDetails(String userId, String storyId) {
        try{
            log.info("Fetching details for story: {}", storyId);
            Story story = storyRepository.findById(storyId)
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

            if(story.getStatus() == StoryStatus.DRAFT && !story.getAuthorId().equals(userId)){
                log.error("Unauthorized access: User {} is not the author of story {} and story is in DRAFT status", userId, storyId);
                throw new AppException(ErrorCode.NOT_AUTHOR_OF_STORY);
            }

            return StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .authorId(story.getAuthorId())
                    .title(story.getTitle())
                    .description(story.getDescription())
                    .img(story.getImg())
                    .status(story.getStatus().name())
                    .averageRatingScore(story.getAverageRatingScore())
                    .totalRatingCount(story.getTotalRatingCount())
                    .numberOfChapters(story.getNumberOfChapters())
                    .numberOfViews(story.getNumberOfViews())
                    .genres(story.getGenres().stream()
                            .map(Story.GenreSummary::getName)
                            .collect(Collectors.toSet()))
                    .build();
        }catch (Exception ex){
            logger.error("Error fetching story details for story {}: {}", storyId, ex.getMessage(), ex);
            throw ex;
        }
    }


    public Page<StoryResponse> getMyStories(
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
                            .numberOfViews(story.getNumberOfViews())
                            .averageRatingScore(story.getAverageRatingScore())
                            .totalRatingCount(story.getTotalRatingCount())
                            .genres(story.getGenres()
                                    .stream()
                                    .map(Story.GenreSummary::getName)
                                    .collect(Collectors.toSet()))
                            .build());

            log.info("Fetched {} stories for user {}", storyPage.getTotalElements(), userId);
            return storyPage;

        }
        catch(Exception ex){
            logger.error("Error fetching my stories for user {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public StoryResponse updateStory(UpdateStoryRequest request, MultipartFile img) {
        try {
            log.info("Updating story: {}", request.getStoryId());
            Story story = storyRepository.findById(request.getStoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

            if (request.getTitle() != null) {
                story.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                story.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                story.setStatus(StoryStatus.valueOf(request.getStatus()));
            }
            if (request.getGenreIds() != null) {
                Set<Story.GenreSummary> genres = story.getGenres();

                Set<Story.GenreSummary> newGenres = request.getGenreIds().stream()
                        .map(genreId -> genreRepository.findById(genreId)
                                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND)))
                        .map(genre -> new Story.GenreSummary(genre.getGenreId(), genre.getName()))
                        .collect(Collectors.toSet());

                genres.addAll(newGenres);
            }
            if (img != null && !img.isEmpty()) {
                String message = mediaClient.deleteFile(story.getImg(), "image");
                log.info("Deleted old image for story {}: {}", story.getStoryId(), message);
                String imgUrl = mediaClient.uploadFile(img, "cover-img");
                story.setImg(imgUrl);
            }

            storyRepository.save(story);
            log.info("Story {} updated successfully", request.getStoryId());

            return StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .authorId(story.getAuthorId())
                    .title(story.getTitle())
                    .description(story.getDescription())
                    .img(story.getImg())
                    .status(story.getStatus().name())
                    .numberOfChapters(story.getNumberOfChapters())
                    .averageRatingScore(story.getAverageRatingScore())
                    .totalRatingCount(story.getTotalRatingCount())
                    .genres(story.getGenres().stream()
                            .map(Story.GenreSummary::getName)
                            .collect(Collectors.toSet()))
                    .build();
        }
        catch (Exception ex) {
            logger.error("Error updating story {}: {}", request.getStoryId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional
    public void deleteStory(String userId, String storyId){
        log.info("Deleting story: {}", storyId);
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

        try{
            List<Chapter> chapters = chapterRepository.findByStoryId(storyId);
            for(Chapter chapter : chapters){
                writingService.deleteChapter(userId, chapter.getChapterId());
            }

            String message = mediaClient.deleteFile(story.getImg(), "image");
            log.info("Deleted image for story {}: {}", storyId, message);
            storyRepository.delete(story);
            log.info("Story {} deleted successfully", storyId);
        }
        catch (Exception ex){
            logger.error("Error deleting story {}: {}", storyId, ex.getMessage(), ex);
            throw ex;
        }
    }
}
