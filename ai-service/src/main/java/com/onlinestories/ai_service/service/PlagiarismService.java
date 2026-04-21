package com.onlinestories.ai_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlagiarismService {

    VectorStore vectorStore;

    public Boolean checkPlagiarism(String authorId, String content) {
        log.info("Starting plagiarism check for authorId: {}", authorId);

        Document tempDoc = new Document(content);
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
        List<Document> chunks = splitter.split(tempDoc);

        for (Document chunk : chunks) {

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(Objects.requireNonNull(chunk.getText()))
                    .topK(5)
                    .build();

            List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

            for (Document matchedDoc : similarDocs) {

                Map<String, Object> metadata = matchedDoc.getMetadata();

                String matchedStatus = metadata.getOrDefault("status", "").toString();
                String matchedAuthorId = metadata.getOrDefault("authorId", "").toString();
                String matchedStoryId = metadata.getOrDefault("storyId", "Unknown").toString();
                String matchedChapterId = metadata.getOrDefault("chapterId", "Unknown").toString();

                if (authorId.equals(matchedAuthorId)) {
                    continue;
                }

                Object distanceRaw = metadata.get("distance");
                double score = distanceRaw != null ? Double.parseDouble(distanceRaw.toString()) : 1.0;
                log.info("Matched document from storyId [{}], chapterId [{}] with authorId [{}], status [{}], score: {}"
                        , matchedStoryId, matchedChapterId, matchedAuthorId, matchedStatus, score);

                if (!"PUBLISHED".equals(matchedStatus)) {
                    continue;
                }


                if (score <= 0.545) {
                    log.warn("Plagiarism detected! Author [{}] has content similar to storyId [{}], chapterId [{}] with score: {}", authorId,
                            matchedStoryId, matchedChapterId, score);
                    return true;
                }
            }
        }

        log.info("No plagiarism detected for authorId: {}", authorId);
        return false;
    }
}
