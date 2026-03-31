package com.onlinestories.story_service.Service;

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


    public Boolean checkStoryExistence(String storyId){
        log.info("Checking existence of story: {}", storyId);
        return storyRepository.existsById(storyId);
    }

    public Boolean checkChapterExistence(String chapterId){
        log.info("Checking existence of chapter: {}", chapterId);
        return chapterRepository.existsById(chapterId);
    }

    public Boolean checkCommentExistence(String commentId) {
        log.info("Checking if comment exists by commentId: {}", commentId);
        return commentRepository.existsById(commentId);
    }
}
