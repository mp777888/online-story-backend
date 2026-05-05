package com.onlinestories.story_service.Utils;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import com.onlinestories.story_service.Entity.Chapter;
import com.onlinestories.story_service.Entity.ChapterVersion;
import com.onlinestories.story_service.Enum.Period;
import com.onlinestories.story_service.Repository.ChapterVersionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoryHelper {
    static String DIRTY_STORIES_KEY = "dirty_stories_metrics";
    MongoTemplate mongoTemplate;
    StringRedisTemplate stringRedisTemplate;
    ChapterVersionRepository chapterVersionRepository;

    public void markStoryAsDirty(String storyId) {
        stringRedisTemplate.opsForSet().add(DIRTY_STORIES_KEY, storyId);
    }

    public String extractDocxAsHtml(MultipartFile file) {
        try (var is = file.getInputStream();
             var doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(is)) {

            StringBuilder html = new StringBuilder();

            for (var p : doc.getParagraphs()) {
                String text = p.getText();
                if (text == null || text.trim().isEmpty()) continue;

                String style = p.getStyle();
                if (style != null && style.toLowerCase().contains("heading")) {
                    html.append("<h3>").append(org.jsoup.parser.Parser.unescapeEntities(text, false)).append("</h3>");
                } else {
                    html.append("<p>").append(org.jsoup.parser.Parser.unescapeEntities(text, false)).append("</p>");
                }
            }

            return html.toString();
        } catch (Exception ex) {
            throw new AppException(ErrorCode.FILE_IMPORT_FAILED);
        }
    }

    public LocalDateTime resolveStartTime(Period period, ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        return switch (period) {
            case TODAY -> today.atStartOfDay();
            case WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
            case MONTH -> today.withDayOfMonth(1).atStartOfDay();
            case ALL_TIME -> LocalDateTime.MIN;
        };
    }

    public String getContentForReading(String chapterVersionId) {
        log.info("Fetching chapter version details for chapterId: {}", chapterVersionId);
        ChapterVersion version = chapterVersionRepository.findById(chapterVersionId)
                .orElse(null);

        if(version == null){
            log.warn("Published chapter version not found for chapterId: {}", chapterVersionId);
            throw new AppException(ErrorCode.VERSION_NOT_FOUND);
        }

        log.info("Chapter version details fetched successfully for chapterId: {}", chapterVersionId);
        return version.getContent();
    }

    public void updateChapterLastEditedTime(String chapterId) {
        try {
            Query query = new Query( Criteria.where("_id").is(chapterId));
            Update update = new Update().set("lastEditedAt", LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

            mongoTemplate.updateFirst(query, update, Chapter.class);
        } catch (Exception e) {
            log.error("Error updating lastEditedAt for chapterId {}: {}", chapterId, e.getMessage());
        }
    }
}
