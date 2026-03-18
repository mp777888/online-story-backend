package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.DTO.Request.CommentRequest;
import com.onlinestories.story_service.DTO.Response.CommentResponse;
import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Entity.Comment;
import com.onlinestories.story_service.Enum.ChapterStatus;
import com.onlinestories.story_service.Exception.AppException;
import com.onlinestories.story_service.Exception.ErrorCode;
import com.onlinestories.story_service.Repository.ChapterRepository;
import com.onlinestories.story_service.Repository.CommentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InteractService {
    ChapterRepository chapterRepository;
    CommentRepository commentRepository;

    public CommentResponse addComment(String userId,CommentRequest request){
        log.info("Adding comment for chapterId: {}, userId: {}"
                , request.getChapterId(), userId);

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));

        if (chapter.getStatus() != ChapterStatus.PUBLISHED) {
            throw new AppException(ErrorCode.CHAPTER_IS_NOT_PUBLISHED);
        }

        if(request.getParentCommentId() != null) {
            if (!commentRepository.existsById(request.getParentCommentId())) {
                throw new AppException(ErrorCode.PARENT_COMMENT_NOT_FOUND);
            }
        }

        Comment comment = Comment.builder()
                .chapterId(request.getChapterId())
                .userId(userId)
                .content(request.getContent())
                .parentCommentId(request.getParentCommentId())
                .createdAt(LocalDateTime.now())
                .build();

        comment = commentRepository.save(comment);

        return mapToResponse(comment);
    }

    public CommentResponse getCommentById(String commentId) {
        log.info("Getting comment by id: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        return mapToResponse(comment);
    }

    public List<CommentResponse> getChildCommentsById(String commentId) {
        log.info("Getting child comment by id: {}", commentId);

        if(!commentRepository.existsById(commentId)) {
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findByParentCommentId(commentId);

        return comments.stream().map(this::mapToResponse).toList();
    }

    public Page<CommentResponse> getCommentsByChapterId(
            String chapterId,int page, int size) {
        log.info("Getting comments by chapter id: {}", chapterId);
        if (!chapterRepository.existsById(chapterId)) {
            throw new AppException(ErrorCode.CHAPTER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> rootCommentsPage = commentRepository.findByChapterIdAndParentCommentIdIsNull(chapterId, pageable);

        if (rootCommentsPage.isEmpty()) {
            return Page.empty();
        }

        List<String> rootCommentIds = rootCommentsPage.getContent().stream()
                .map(Comment::getCommentId)
                .toList();

        List<Comment> allReplies = commentRepository.findByParentCommentIdIn(rootCommentIds);

        Map<String, List<Comment>> repliesMap = allReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentCommentId));



        return rootCommentsPage.map(rootComment -> {
            CommentResponse response = mapToResponse(rootComment);

            // Lấy danh sách con từ Map, nếu không có thì trả về list rỗng
            List<Comment> children = repliesMap.getOrDefault(rootComment.getCommentId(), List.of());

            // Map các Entity con sang Response con
            List<CommentResponse> childResponses = children.stream()
                    .map(this::mapToResponse)
                    .toList();

            response.setReplies(childResponses);
            return response;
        });
    }


    public void deleteComment(String commentId) {
        log.info("Deleting comment by id: {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
        }
        try {
            commentRepository.deleteByParentCommentId(commentId);
            commentRepository.deleteById(commentId);
        } catch (Exception e) {
            log.error("Error while deleting child comments of commentId: {}, error: {}", commentId, e.getMessage());
        }

    }



    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .chapterId(comment.getChapterId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
