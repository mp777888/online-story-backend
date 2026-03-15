package com.onlinestories.story_service.Repository;


import com.onlinestories.story_service.Entity.ChapterDraft;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterDraftRepository extends MongoRepository<ChapterDraft, String> {
    ChapterDraft findByChapterId(String chapterId);
    boolean existsByChapterId(String chapterId);
    void deleteByChapterId(String chapterId);
}
