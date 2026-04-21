package com.onlinestories.ai_service.kafka.consumer;

import com.onlinestories.common.chapter.event.ChapterSyncEvent;
import com.onlinestories.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSyncKafkaListener {
    private final VectorStore vectorStore;

    /**
     * LẮNG NGHE SỰ KIỆN CẬP NHẬT TRUYỆN MỚI
     */
    @KafkaListener(
            topics = {KafkaTopics.CHAPTER_SYNC},
            groupId = "ai-service-group"
    )
    public void consumeChapterEvent(ChapterSyncEvent event) {
        try {
            log.info("Received ChapterSyncEvent for chapterId: {}", event.getChapterId());

            String modifiedContent = String.format("""
            Tên truyện: %s
            Tên chương: %s
            Nội dung:
            %s
            """, event.getStoryTitle(), event.getChapterTitle(), event.getContent());


            Document document = new Document(modifiedContent, Map.of(
                    "storyId", event.getStoryId(),
                    "chapterId", event.getChapterId(),
                    "status", event.getStatus()
            ));

            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = splitter.split(document);

            vectorStore.add(chunks);
        }
        catch (Exception e) {
            log.error("Error processing ChapterSyncEvent for chapterId: {}, error: {}", event.getChapterId(), e.getMessage());
        }

    }



}
