package com.onlinestories.story_service.service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.common.story.event.StoryUpdatedEvent;
import com.onlinestories.common.user.dto.UserResponse;

import com.onlinestories.story_service.client.MediaClient;
import com.onlinestories.story_service.client.UserClient;
import com.onlinestories.story_service.dto.request.CreateStoryRequest;
import com.onlinestories.story_service.dto.request.UpdateStoryRequest;
import com.onlinestories.story_service.dto.response.StoryResponse;
import com.onlinestories.story_service.entity.Chapter;
import com.onlinestories.story_service.entity.Story;
import com.onlinestories.story_service.enums.StoryStatus;

import com.onlinestories.story_service.kafka.Producer.StoryEventProducer;
import com.onlinestories.story_service.repository.ChapterRepository;
import com.onlinestories.story_service.repository.GenreRepository;
import com.onlinestories.story_service.repository.StoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoryService {
    TagService tagService;
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    WritingService writingService;
    GenreRepository genreRepository;
    UserClient userClient;
    MediaClient mediaClient;
    StoryEventProducer storyEventProducer;
    Logger logger = LoggerFactory.getLogger(StoryService.class);

    public StoryResponse createNewStory(CreateStoryRequest request, String authorId, MultipartFile img) {
        try {
            log.info("Creating new story: {}", request.getTitle());

            // Validate author existence via UserClient
            UserResponse userResponse = userClient.getUserById(authorId).getResult();
            if (userResponse == null) {
                log.error("Author with ID {} not found", authorId);
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            Set<String> processedTags = tagService.processTagsForStory(request.getTags());

            Story story = Story.builder()
                    .authorId(authorId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .img(img == null || img.isEmpty()
                            ? null : mediaClient.uploadFile(img,"cover-img"))
                    .status(StoryStatus.DRAFT)
                    .language(request.getLanguage())
                    .numberOfChapters(0)
                    .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .genres(request.getGenreIds().stream()
                            .map(genreId -> genreRepository.findById(genreId)
                                    .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND)))
                            .map(genre -> new Story.GenreSummary(genre.getGenreId(), genre.getName()))
                            .collect(Collectors.toSet()))
                    .tags(processedTags)
                    .build();
            storyRepository.save(story);
            log.info("Story {} created successfully", request.getTitle());

            return StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .authorId(story.getAuthorId())
                    .title(story.getTitle())
                    .description(story.getDescription())
                    .img(story.getImg())
                    .language(story.getLanguage().name())
                    .status(story.getStatus().name())
                    .numberOfChapters(story.getNumberOfChapters())
                    .genres(story.getGenres().stream()
                            .map(Story.GenreSummary::getName)
                            .collect(Collectors.toSet()))
                    .tags(story.getTags())
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
                    .language(story.getLanguage().name())
                    .averageRatingScore(story.getAverageRatingScore())
                    .totalRatingCount(story.getTotalRatingCount())
                    .numberOfChapters(story.getNumberOfChapters())
                    .numberOfViews(story.getNumberOfViews())
                    .premium(story.isPremium())
                    .unlockPrice(story.getUnlockPrice())
                    .genres(story.getGenres().stream()
                            .map(Story.GenreSummary::getName)
                            .collect(Collectors.toSet()))
                    .tags(story.getTags())
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
                            .language(story.getLanguage().name())
                            .numberOfChapters(story.getNumberOfChapters())
                            .numberOfViews(story.getNumberOfViews())
                            .averageRatingScore(story.getAverageRatingScore())
                            .totalRatingCount(story.getTotalRatingCount())
                            .premium(story.isPremium())
                            .unlockPrice(story.getUnlockPrice())
                            .genres(story.getGenres()
                                    .stream()
                                    .map(Story.GenreSummary::getName)
                                    .collect(Collectors.toSet()))
                            .tags(story.getTags())
                            .build());

            log.info("Fetched {} stories for user {}", storyPage.getTotalElements(), userId);
            return storyPage;

        }
        catch(Exception ex){
            logger.error("Error fetching my stories for user {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public StoryResponse updateStory(String userId, boolean isAdmin, UpdateStoryRequest request, MultipartFile img) {
        try {
            log.info("Updating story: {}", request.getStoryId());
            Story story = storyRepository.findById(request.getStoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

            if(!story.getAuthorId().equals(userId) && !isAdmin){
                log.error("Unauthorized access: User {} is not the author of story {}", userId, request.getStoryId());
                throw new AppException(ErrorCode.NOT_AUTHOR_OF_STORY);
            }

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

            if (request.getTags() != null) {
                Set<String> processedTags = tagService.processTagsForStory(request.getTags());
                story.setTags(processedTags);
            }

            if (img != null && !img.isEmpty()) {
//                if (story.getImg() != null) {
//                    String message = mediaClient.deleteFile(story.getImg(), "image");
//                    log.info("Deleted old image for story {}: {}", story.getStoryId(), message);
//                }
                String imgUrl = mediaClient.uploadFile(img, "cover-img");
                story.setImg(imgUrl);
            }

            if (request.isPremium() && request.getUnlockPrice() > 0) {
                story.setPremium(true);
                story.setUnlockPrice(request.getUnlockPrice());
            }

            storyRepository.save(story);
            log.info("Story {} updated successfully", request.getStoryId());


            try {
                StoryUpdatedEvent event = StoryUpdatedEvent.builder()
                        .storyId(story.getStoryId())
                        .authorId(story.getAuthorId())
                        .authorName(userClient.getUserById(story.getAuthorId()).getResult().getNickname())
                        .title(story.getTitle())
                        .description(story.getDescription())
                        .coverImg(story.getImg())
                        .status(story.getStatus().name())
                        .numberOfChapters(story.getNumberOfChapters())
                        .averageRatingScore(story.getAverageRatingScore())
                        .totalRatingCount(story.getTotalRatingCount())
                        .premium(story.isPremium())
                        .unlockPrice(story.getUnlockPrice())
                        .genres(story.getGenres().stream()
                                .map(Story.GenreSummary::getName)
                                .collect(Collectors.toList()))
                        .tags(story.getTags().stream().toList())
                        .build();
                storyEventProducer.storyUpdatedEvent(event);
            }
            catch (Exception ex) {
                log.error("Failed to publish StoryUpdatedEvent for story {}: {}", story.getStoryId(), ex.getMessage(), ex);
            }

            return StoryResponse.builder()
                    .storyId(story.getStoryId())
                    .authorId(story.getAuthorId())
                    .title(story.getTitle())
                    .description(story.getDescription())
                    .img(story.getImg())
                    .status(story.getStatus().name())
                    .language(story.getLanguage().name())
                    .numberOfChapters(story.getNumberOfChapters())
                    .averageRatingScore(story.getAverageRatingScore())
                    .totalRatingCount(story.getTotalRatingCount())
                    .premium(story.isPremium())
                    .unlockPrice(story.getUnlockPrice())
                    .genres(story.getGenres().stream()
                            .map(Story.GenreSummary::getName)
                            .collect(Collectors.toSet()))
                    .tags(story.getTags())
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
