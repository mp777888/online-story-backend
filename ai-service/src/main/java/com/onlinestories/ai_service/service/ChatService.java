package com.onlinestories.ai_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
//@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {
    ChatClient chatClient;
    VectorStore vectorStore;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        String systemPrompt = """
            Bạn là trợ lý AI ảo của hệ thống truyện online NovelToolKit.
            Nhiệm vụ của bạn là giải đáp các câu hỏi dựa trên các tài liệu đã được cung cấp (từ CSDL của chúng tôi).
            - Nếu không có thông tin từ tài liệu, hãy nói rằng bạn không biết, đừng tự bịa ra thông tin.
            - Hãy trả lời lịch sự, thân thiện và ngắn gọn.
            """;
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .build();
    }

    public String chatWithUser(String userMessage) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userMessage)
                .filterExpression("status == 'PUBLISHED'") // Chỉ tìm kiếm trong các truyện đã xuất bản
                .topK(3)
                .build();

        List<Document> similarDocuments = this.vectorStore.similaritySearch(searchRequest);
        String documentContext = similarDocuments.stream()
                .map(doc -> {
                    Map<String, Object> meta = doc.getMetadata();
                    String storyTitle = meta.getOrDefault("storyTitle", "Chưa rõ").toString();
                    String chapterTitle = meta.getOrDefault("chapterTitle", "Chưa rõ").toString();

                    return String.format("Tên truyện: %s\nTên chương: %s\nNội dung: %s",
                            storyTitle, chapterTitle, doc.getText());
                })
                .collect(Collectors.joining("\n---\n"));

        String finalPrompt = String.format("""
                Dựa vào các thông tin sau từ cơ sở dữ liệu:
                %s
                
                Hãy trả lời câu hỏi của người dùng: "%s"
                """, documentContext, userMessage);

        try {
            log.info("AI response successfully generated");
            return this.chatClient.prompt()
                    .user(finalPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI Model bị lỗi hoặc Timeout: {}", e.getMessage());
            return "Xin lỗi, hệ thống AI đang quá tải lúc này. Vui lòng hỏi lại sau ít phút!";
        }
    }
}
