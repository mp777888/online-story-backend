package com.onlinestories.story_service.repository;


import com.onlinestories.story_service.entity.ChapterDraft;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterDraftRepository extends MongoRepository<ChapterDraft, String> {
    ChapterDraft findByChapterId(String chapterId);
    boolean existsByChapterId(String chapterId);
    void deleteByChapterId(String chapterId);
}
