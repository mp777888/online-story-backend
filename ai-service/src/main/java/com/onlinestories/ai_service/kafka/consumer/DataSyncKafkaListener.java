package com.onlinestories.ai_service.kafka.consumer;

import com.onlinestories.common.chapter.event.ChapterSyncEvent;
import com.onlinestories.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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



            Document document = new Document(event.getContent(), Map.of(
                    "storyId", event.getStoryId(),
                    "chapterId", event.getChapterId(),
                    "authorId", event.getAuthorId(),
                    "status", event.getStatus(),
                    "storyTitle", event.getStoryTitle(),
                    "chapterTitle", event.getChapterTitle()));

            // Định nghĩa các dấu mốc ưu tiên để ngắt chunk
            List<Character> punctuationMarks = List.of('.', '?', '!', ';', ',', '\n');
            TokenTextSplitter splitter = new TokenTextSplitter(
                    800,         // chunkSize: Giới hạn tối đa 800 token mỗi chunk (~600 từ)
                    200,                  // minChunkSizeChars: Độ dài tối thiểu của 1 chunk (khoảng 200 ký tự)
                    5,                    // minChunkLengthToEmbed: Độ dài tối thiểu để sinh vector (quá ngắn bỏ qua)
                    10000,                // maxNumChunks: Giới hạn số chunk tối đa cho 1 chương (chống spam)
                    true,                 // keepSeparator: Có giữ lại dấu ngăn cách khi cắt không
                    punctuationMarks      // punctuationMarks: Ngắt tại các vị trí ngữ pháp hợp lý
            );
            List<Document> chunks = splitter.split(document);

            List<String> oldChunkIds = new ArrayList<>();
            // Vòng lặp 50: Xóa tối đa 50 chunk của chương
            for (int i = 0; i < 50; i++) {
                oldChunkIds.add(event.getChapterId() + "_chunk_" + i);
            }

            try {
                vectorStore.delete(oldChunkIds);
                log.info("Deleted old chunks (if exist) for chapterId: {}", event.getChapterId());
            } catch (Exception ex) {
                log.warn("Could not delete old chunks or first time sync: {}", ex.getMessage());
            }

            List<Document> documentsToSave = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);

                String deterministicId = event.getChapterId() + "_chunk_" + i;

                Document docWithId = new Document(
                        deterministicId,
                        Objects.requireNonNull(chunk.getText()),
                        chunk.getMetadata()
                );
                documentsToSave.add(docWithId);
            }

            vectorStore.add(documentsToSave);
        }
        catch (Exception e) {
            log.error("Error processing ChapterSyncEvent for chapterId: {}, error: {}", event.getChapterId(), e.getMessage());
        }

    }



}
