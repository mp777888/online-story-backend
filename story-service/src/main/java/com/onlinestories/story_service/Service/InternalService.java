package com.onlinestories.story_service.Service;

import com.onlinestories.common.chapter.dto.ChapterDTOResponse;
import com.onlinestories.common.story.dto.StoryDTOResponse;
import com.onlinestories.story_service.Entity.Story;
import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Repository.CommentRepository;
import com.onlinestories.story_service.Repository.StoryRepository;
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
}
