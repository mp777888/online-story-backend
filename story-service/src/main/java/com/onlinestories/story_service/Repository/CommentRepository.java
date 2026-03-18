package com.onlinestories.story_service.Repository;

import com.onlinestories.story_service.Entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    Page<Comment> findByChapterIdAndParentCommentIdIsNull(String chapterId, Pageable pageable);
    List<Comment> findByParentCommentIdIn(List<String> parentCommentIds);
    List<Comment> findByParentCommentId(String commentId);
    List<Comment> findByChapterId(String chapterId);
    void deleteByParentCommentId(String commentId);
}
