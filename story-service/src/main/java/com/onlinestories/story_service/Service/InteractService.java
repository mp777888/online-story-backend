package com.onlinestories.story_service.Service;

import com.onlinestories.story_service.DTO.Request.CommentRequest;
import com.onlinestories.story_service.DTO.Request.RatingRequest;
import com.onlinestories.story_service.DTO.Response.CommentResponse;
import com.onlinestories.story_service.DTO.Response.RatingResponse;
import com.onlinestories.story_service.Entity.*;
import com.onlinestories.story_service.Enum.ChapterStatus;
import com.onlinestories.story_service.Exception.AppException;
import com.onlinestories.story_service.Exception.ErrorCode;
import com.onlinestories.story_service.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
    StoryRepository storyRepository;
    ChapterRepository chapterRepository;
    CommentRepository commentRepository;
    RatingRepository ratingRepository;
    FavoriteRepository favoriteRepository;

    MongoTemplate mongoTemplate;

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

    public RatingResponse ratingStory(String userId, RatingRequest request){
        log.info("Rating story by userId: {}, storyId: {}, ratingScore: {}"
                , userId, request.getStoryId(), request.getRatingScore());

        if (!storyRepository.existsById(request.getStoryId())) {
            throw new AppException(ErrorCode.STORY_NOT_FOUND);
        }

        if(ratingRepository.existsByUserIdAndStoryId(userId, request.getStoryId())) {
            throw new AppException(ErrorCode.RATING_ALREADY_EXISTS);
        }

        Rating rating = Rating.builder()
                .userId(userId)
                .storyId(request.getStoryId())
                .ratingScore(request.getRatingScore())
                .comment(request.getComment())
                .ratedAt(LocalDateTime.now())
                .build();

        rating = ratingRepository.save(rating);

        Query query = new Query(Criteria.where("_id").is(request.getStoryId()));

        Update updateTotals = new Update()
                .inc("totalRatingScore", request.getRatingScore())
                .inc("totalRatingCount", 1);

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        Story updatedStory = mongoTemplate.findAndModify(query, updateTotals, options, Story.class);

        if (updatedStory != null && updatedStory.getTotalRatingCount() > 0) {
            double newAverage = updatedStory.getTotalRatingScore() / (double) updatedStory.getTotalRatingCount();

            // Làm tròn 1 chữ số thập phân
            newAverage = Math.round(newAverage * 10.0) / 10.0;

            Update updateAvg = new Update().set("averageRatingScore", newAverage);
            mongoTemplate.updateFirst(query, updateAvg, Story.class);
        }

        return RatingResponse.builder()
                .voteId(rating.getVoteId())
                .userId(userId)
                .storyId(request.getStoryId())
                .ratingScore(request.getRatingScore())
                .comment(request.getComment())
                .ratedAt(rating.getRatedAt())
                .build();
    }

    public RatingResponse getStoryRating(String storyId) {
        log.info("Getting story rating by storyId: {}", storyId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));

        return RatingResponse.builder()
                .storyId(storyId)
                .ratingScore(story.getAverageRatingScore())
                .build();
    }

    public RatingResponse getMyRating(String userId, String storyId) {
        log.info("Getting my rating for storyId: {}, userId: {}", storyId, userId);

        if (!storyRepository.existsById(storyId)) {
            throw new AppException(ErrorCode.STORY_NOT_FOUND);
        }

        Rating rating = ratingRepository.findByUserIdAndStoryId(userId, storyId);

        if (rating == null) {
            throw new AppException(ErrorCode.RATING_NOT_FOUND);
        }

        return RatingResponse.builder()
                .voteId(rating.getVoteId())
                .userId(userId)
                .storyId(storyId)
                .ratingScore(rating.getRatingScore())
                .comment(rating.getComment())
                .ratedAt(rating.getRatedAt())
                .build();
    }

    public void addingStoryToFavoriteList(String userId, String storyId) {
        log.info("Adding story to favorite list by userId: {}, storyId: {}", userId, storyId);

        if (!storyRepository.existsById(storyId)) {
            throw new AppException(ErrorCode.STORY_NOT_FOUND);
        }

        if (favoriteRepository.existsByUserIdAndStoryId(userId, storyId)) {
            throw new AppException(ErrorCode.FAVORITE_ALREADY_EXISTS);
        }
        try {
            favoriteRepository.save(Favorite.builder()
                    .userId(userId)
                    .storyId(storyId)
                    .build());
        }
        catch (Exception e) {
            log.error("Error while adding like for userId: {}, storyId: {}, error: {}", userId, storyId, e.getMessage());
        }
    }

    public void removeStoryFromFavoriteList(String userId, String storyId) {
        log.info("Removing story from favorite list by userId: {}, storyId: {}", userId, storyId);

        if (!storyRepository.existsById(storyId)) {
            throw new AppException(ErrorCode.STORY_NOT_FOUND);
        }

        if (!favoriteRepository.existsByUserIdAndStoryId(userId, storyId)) {
            throw new AppException(ErrorCode.FAVORITE_NOT_FOUND);
        }
        try {
            favoriteRepository.deleteByUserIdAndStoryId(userId, storyId);
        }
        catch (Exception e) {
            log.error("Error while removing like for userId: {}, storyId: {}, error: {}", userId, storyId, e.getMessage());
        }
    }

    public Boolean isStoryInFavoriteList(String userId, String storyId) {
        log.info("Checking if story is in favorite list by userId: {}, storyId: {}", userId, storyId);

        if (!storyRepository.existsById(storyId)) {
            throw new AppException(ErrorCode.STORY_NOT_FOUND);
        }

        return favoriteRepository.existsByUserIdAndStoryId(userId, storyId);
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
