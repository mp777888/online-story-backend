package com.example.onlinestories.transaction_service.Kafka.Consumer;

import com.example.onlinestories.transaction_service.Entity.PayoutHistory;
import com.example.onlinestories.transaction_service.Enums.PayoutStatus;
import com.example.onlinestories.transaction_service.Service.WalletService;
import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.common.story.event.PayoutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionConsumer {
    private final WalletService walletService;
    private final MongoTemplate mongoTemplate;

    @KafkaListener(
            topics = {KafkaTopics.PAYOUT_PROCESSED},
            groupId = "${spring.application.name}"
    )
    public void consumeTransactionMessage(PayoutEvent event) {
        log.info("Received PayoutEvent for authorId: {}", event.getAuthorId());
        Query query = new Query(
                Criteria.where("authorId").is(event.getAuthorId())
                        .and("payoutMonth").is(event.getPayoutMonth())
                        .and("status").is(PayoutStatus.COMPLETED)
        );

        boolean isAlreadyPaid = mongoTemplate.exists(query, PayoutHistory.class);

        if (isAlreadyPaid) {
            log.info("Payout already processed for authorId: {} for month: {}. Skipping payout.",
                    event.getAuthorId(), event.getPayoutMonth());
            return;
        }

        try {
            walletService.payoutProcess(event.getAuthorId(), event.getTotalViews(), event.getPayoutMonth());
            log.info("Successfully processed payout for authorId: {} for month: {}", event.getAuthorId(), event.getPayoutMonth());
        } catch (Exception e) {
            log.error("Error processing payout for authorId: {} for month: {}. Error: {}",
                    event.getAuthorId(), event.getPayoutMonth(), e.getMessage());
        }
    }
}
