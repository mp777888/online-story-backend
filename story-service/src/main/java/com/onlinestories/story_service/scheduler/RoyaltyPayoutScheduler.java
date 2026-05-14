package com.onlinestories.story_service.scheduler;

import com.onlinestories.common.story.event.PayoutEvent;
import com.onlinestories.story_service.dto.response.PayoutDTO;
import com.onlinestories.story_service.kafka.Producer.StoryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoyaltyPayoutScheduler {
    private final MongoTemplate mongoTemplate;
    private final StoryEventProducer storyEventProducer;

    // Chạy vào lúc 00:00:00 ngày đầu tiên của mỗi tháng
    @Scheduled(cron = "0 0 0 1 * ?")
//    @Scheduled(cron = "0 0/10 * * * ?") // Test: chạy mỗi 10 phút
    public void calculateAndPayoutMonthlyViews() {
        log.info("Calculating monthly views and processing payouts...");

        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate now = LocalDate.now(zone);
        LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = now.withDayOfMonth(1).minusDays(1);

        String payoutMonth = firstDayOfLastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 1. Lọc điều kiện date trong tháng trước
        var matchDate = Aggregation.match(
                Criteria.where("date").gte(firstDayOfLastMonth).lte(lastDayOfLastMonth)
        );

        // 2. Lookup sang bảng story
        AggregationOperation lookupStory = context -> new org.bson.Document("$lookup",
                new org.bson.Document("from", "story")
                        .append("let", new org.bson.Document("sid", "$storyId"))
                        .append("pipeline", List.of(
                                new org.bson.Document("$match", new org.bson.Document("$expr",
                                        new org.bson.Document("$eq", List.of(
                                                new org.bson.Document("$toString", "$_id"),
                                                "$$sid"
                                        ))
                                ))
                        ))
                        .append("as", "storyDetails")
        );

        // 3. Unwind kết quả lookup (vì lookup trả về mảng)
        var unwindStory = Aggregation.unwind("storyDetails");

        // 4. Group theo authorId và tính tổng view
        var groupAuthors = Aggregation.group("storyDetails.authorId")
                .sum("viewCount").as("totalViews");

        var calculatePayoutAgg = Aggregation.newAggregation(
                matchDate,
                lookupStory,
                unwindStory,
                groupAuthors
        );

        var results = mongoTemplate.aggregate(calculatePayoutAgg, "storyDailyView", PayoutDTO.class);
        List<PayoutDTO> payouts = results.getMappedResults();

        // 5. Tính token và gửi sang transaction-service
        for (PayoutDTO payout : payouts) {
            String authorId = payout.getId(); // Group by _id nên field id sẽ lưu authorId
            int totalMonthViews = payout.getTotalViews();

            if (totalMonthViews > 0) {
                log.info("Processing payout for authorId: {}, totalViews: {}", authorId, totalMonthViews);
                PayoutEvent payoutEvent = PayoutEvent.builder()
                        .authorId(authorId)
                        .payoutMonth(payoutMonth)
                        .totalViews(totalMonthViews)
                        .build();
                try{
                    storyEventProducer.payoutStoryViewsEvent(payoutEvent);
                    log.info("Payout event sent for authorId: {}", authorId);
                } catch (Exception e) {
                    log.error("Failed to send payout event for authorId: {}. Error: {}", authorId, e.getMessage());
                }
            }
        }
    }
}
