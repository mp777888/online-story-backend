package com.onlinestories.story_service.service;

import com.onlinestories.common.chapter.dto.ChapterDTOResponse;
import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.common.story.dto.StoryDTOResponse;
import com.onlinestories.story_service.entity.Story;
import com.onlinestories.story_service.enums.ChapterStatus;
import com.onlinestories.story_service.repository.ChapterRepository;
import com.onlinestories.story_service.repository.CommentRepository;
import com.onlinestories.story_service.repository.StoryRepository;
import com.onlinestories.story_service.utils.StoryHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalService {
    CommentRepository commentRepository;
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    StoryHelper storyHelper;


    public StoryDTOResponse checkStoryExistence(String storyId){
        log.info("Checking existence of story: {}", storyId);
        Story story = storyRepository.findById(storyId).orElse(null);
        if (story == null) {
            log.warn("Story not found for storyId: {}", storyId);
            return null;
        }
        log.info("Story found for storyId: {}", storyId);
        return StoryDTOResponse.builder()
                .storyId(story.getStoryId())
                .authorId(story.getAuthorId())
                .status(story.getStatus().name())
                .title(story.getTitle())
                .premium(story.isPremium())
                .unlockPrice(story.getUnlockPrice())
                .build();
    }

    public ChapterDTOResponse checkChapterExistence(String chapterId){
        log.info("Checking existence of chapter: {}", chapterId);
        var chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter == null) {
            log.warn("Chapter not found for chapterId: {}", chapterId);
            return null;
        }
        log.info("Chapter found for chapterId: {}", chapterId);
        return ChapterDTOResponse.builder()
                .chapterId(chapter.getChapterId())
                .storyId(chapter.getStoryId())
                .title(chapter.getTitle())
                .build();
    }

    public Boolean checkCommentExistence(String commentId) {
        log.info("Checking if comment exists by commentId: {}", commentId);
        return commentRepository.existsById(commentId);
    }

    public ChapterDTOResponse getChapterData(String chapterId) {
        log.info("Retrieving content for chapterId: {}", chapterId);

        var chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter == null) {
            log.warn("Chapter not found for chapterId: {}", chapterId);
            throw new AppException(ErrorCode.CHAPTER_NOT_FOUND);
        }

        var story = storyRepository.findById(chapter.getStoryId()).orElse(null);
        if (story == null) {
            log.warn("Story not found for storyId: {}", chapter.getStoryId());
            throw new AppException(ErrorCode.STORY_NOT_FOUND);
        }

        if (chapter.getStatus() != ChapterStatus.PUBLISHED) {
            log.warn("Chapter is not published for chapterId: {}", chapterId);
            throw new AppException(ErrorCode.CHAPTER_IS_NOT_PUBLISHED);
        }

        if (chapter.getPublishedVersionId() == null) {
            log.warn("Published version not found for chapterId: {}", chapterId);
            throw new AppException(ErrorCode.VERSION_NOT_FOUND);
        }

        return ChapterDTOResponse.builder()
                .chapterId(chapter.getChapterId())
                .authorId(story.getAuthorId())
                .storyId(chapter.getStoryId())
                .title(chapter.getTitle())
                .content(storyHelper.getContentForReading(chapter.getPublishedVersionId()))
                .publishedDate(chapter.getPublishedAt())
                .build();
    }
}
