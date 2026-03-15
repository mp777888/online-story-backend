package com.onlinestories.story_service.Repository;


import com.onlinestories.story_service.Entity.ChapterVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterVersionRepository extends MongoRepository<ChapterVersion, String> {
    ChapterVersion findByChapterIdAndStatus(String chapterId, String status);
    Page<ChapterVersion> findByChapterId(String chapterId, Pageable pageable);
    void deleteByChapterId(String chapterId);
}
