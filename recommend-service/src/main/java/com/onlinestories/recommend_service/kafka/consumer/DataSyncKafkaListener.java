package com.onlinestories.recommend_service.kafka.consumer;

import com.onlinestories.common.kafka.KafkaTopics;
import com.onlinestories.common.story.event.StoryMetricsSyncEvent;
import com.onlinestories.common.story.event.StoryUpdatedEvent;
import com.onlinestories.common.user.event.UserEvent;
import com.onlinestories.recommend_service.entity.StorySearchItem;
import com.onlinestories.recommend_service.entity.UserSearchItem;
import com.onlinestories.recommend_service.service.AiEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSyncKafkaListener {

    private final OpenSearchClient openSearchClient;
    private final AiEmbeddingService aiEmbeddingService;

    /**
     * LẮNG NGHE SỰ KIỆN TẠO/CẬP NHẬT TRUYỆN MỚI
     */
    @KafkaListener(
            topics = {KafkaTopics.STORY_UPDATED},
            groupId = "recommend-service-group"
    )
    public void consumeStoryEvent(StoryUpdatedEvent event) {
        try {
            log.info("Received story event from Kafka - Topic: {}, Story ID: {}",
                    "STORY_UPDATED", event.getStoryId());

            // 1. Chuyển đổi từ Event của Kafka sang Entity của Document OpenSearch
            StorySearchItem item = new StorySearchItem();
            item.setStoryId(event.getStoryId());
            item.setTitle(event.getTitle());
            item.setAuthorId(event.getAuthorId());
            item.setAuthorName(event.getAuthorName());
            item.setDescription(event.getDescription());
            item.setCoverImg(event.getCoverImg());
            item.setStatus(event.getStatus());
            item.setNumberOfChapters(event.getNumberOfChapters());
            item.setNumberOfViews(event.getNumberOfViews());
            item.setAverageRatingScore(event.getAverageRatingScore());
            item.setTotalRatingCount(event.getTotalRatingCount());
            item.setPremium(event.isPremium());
            item.setUnlockPrice(event.getUnlockPrice());
            item.setGenres(event.getGenres());
            item.setTags(event.getTags());

            float[] vector = null;

            GetResponse<StorySearchItem> documentResponse = openSearchClient.get(g -> g
                            .index("stories")
                            .id(event.getStoryId()),
                    StorySearchItem.class
            );
            if(documentResponse.found()){
                StorySearchItem existingItem = documentResponse.source();
                if(existingItem != null && Objects.equals(existingItem.getTitle(), event.getTitle()) &&
                        Objects.equals(existingItem.getAuthorName(), event.getAuthorName()) &&
                        Objects.equals(existingItem.getDescription(), event.getDescription()) &&
                        Objects.equals(existingItem.getGenres(), event.getGenres()) &&
                        Objects.equals(existingItem.getTags(), event.getTags())
                ){
                    // Nếu title, authorName, description, genres, tags không thay đổi thì giữ nguyên vector cũ
                    vector = existingItem.getEmbedding();
                }
            }

            if(vector == null) {
                // Gọi thư viện AI/API để tạo vector embedding từ title của truyện
                String textForAi = "Truyện: " + event.getTitle() +
                        ", Thể loại: " + (event.getGenres() != null ? String.join(", ", event.getGenres()) : "N/A") +
                        ", Tác giả: " + event.getAuthorName() +
                        ", Tags: " + (event.getTags() != null ? String.join(", ", event.getTags()) : "N/A") +
                        ", Mô tả: " + event.getDescription();

                // Lấy vector
                vector = aiEmbeddingService.generateEmbedding(textForAi);
            }
            item.setEmbedding(vector);

            // 2. Index vào OpenSearch bản thu gọn
            IndexRequest<StorySearchItem> request = IndexRequest.of(i -> i
                    .index("stories") // Map đến index "stories" trên Bonsai
                    .id(item.getStoryId())
                    .document(item)
            );

            openSearchClient.index(request);
            log.info("Successfully synced story to Bonsai OpenSearch - ID: {}", event.getStoryId());

        } catch (Exception e) {
            log.error("Error syncing story to OpenSearch", e);
        }
    }

    @KafkaListener(
            topics = {KafkaTopics.STORY_METRICS_UPDATED},
            groupId = "recommend-service-group"
    )
    public void updateStoryMetricsInOpenSearch(StoryMetricsSyncEvent event) {
        try {
            // Sử dụng Map để thực hiện partial update (chỉ cập nhật các trường được chỉ định)
            Map<String, Object> partialDoc = new HashMap<>();
            partialDoc.put("numberOfViews", event.getNumberOfViews());
            partialDoc.put("numberOfChapters", event.getNumberOfChapters());
            partialDoc.put("averageRatingScore", event.getAverageRatingScore());
            partialDoc.put("totalRatingCount", event.getTotalRatingCount());
            partialDoc.put("premium", event.isPremium());
            partialDoc.put("unlockPrice", event.getUnlockPrice());

            openSearchClient.update(u -> u
                            .index("stories")
                            .id(event.getStoryId())
                            .doc(partialDoc),
                    Map.class
            );

            log.info("Successfully partial updated metrics for story: {}", event.getStoryId());
        } catch (IOException e) {
            log.error("Error doing partial update for story metrics: {}", event.getStoryId(), e);
        }
    }

    /**
     * LẮNG NGHE SỰ KIỆN KHI TẠO USER (TÁC GIẢ)
     */
    @KafkaListener(
            topics = {KafkaTopics.USER_CREATED, KafkaTopics.USER_UPDATED},
            groupId = "recommend-service-group"
    )
    public void consumeUserEvent(UserEvent event) {
        try {
            log.info("Received user event from Kafka - User ID: {}", event.getUserId());

            UserSearchItem item = new UserSearchItem();
            item.setUserId(event.getUserId());
            item.setNickname(event.getNickname());
            item.setDescription(event.getDescription());
            item.setImg(event.getImg());

            float[] vector = null;

            GetResponse<UserSearchItem> documentResponse = openSearchClient.get(g -> g
                            .index("users")
                            .id(event.getUserId()),
                    UserSearchItem.class
            );

            if(documentResponse.found()){
                UserSearchItem existingItem = documentResponse.source();
                if(existingItem != null && Objects.equals(existingItem.getNickname(), event.getNickname()) &&
                        Objects.equals(existingItem.getDescription(), event.getDescription())
                ){
                    // Nếu nickname và description không thay đổi thì giữ nguyên vector cũ
                    vector = existingItem.getEmbedding();
                }
            }

            if(vector == null) {
                // TẠO VECTOR AI CHO TÁC GIẢ
                String textForAi = "Tác giả: " + event.getNickname() +
                        ", Mô tả / Tiểu sử: " + (event.getDescription() != null ? event.getDescription() : "Không có");

                vector = aiEmbeddingService.generateEmbedding(textForAi);
            }
            item.setEmbedding(vector);

            IndexRequest<UserSearchItem> request = IndexRequest.of(i -> i
                    .index("users")
                    .id(item.getUserId())
                    .document(item)
            );

            openSearchClient.index(request);
            log.info("Successfully synced user to Bonsai OpenSearch - ID: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Error syncing user to OpenSearch", e);
        }
    }

}
