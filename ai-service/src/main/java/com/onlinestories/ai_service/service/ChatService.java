package com.onlinestories.ai_service.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
//@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {
    ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        String systemPrompt = """
        Bạn là nhân viên tư vấn của hệ thống truyện NovelToolKit, NHIỆM VỤ của bạn là giúp GỢI Ý VÀ TÌM TRUYỆN cho người dùng.
        Bạn chỉ có thể trả lời dựa trên cơ sở dữ liệu của chúng tôi
        Một số nguyên tắc BẮT BUỘC BẠN PHẢI NHỚ:
        1. BẠN PHẢI SỬ DỤNG CÁC TOOL được cung cấp ĐỂ TÌM TRUYỆN trước khi trả lời.
        2. CHỈ ĐƯỢC PHÉP gợi ý các truyện trả về từ công cụ.
        3. TUYỆT ĐỐI KHÔNG ĐƯỢC TỰ BỊA RA TÊN TRUYỆN MÀ KHÔNG CÓ TRONG HỆ THỐNG.
        4. Nếu công cụ trả về 'Không tìm thấy dữ liệu', hãy báo thẳng: 'Hệ thống hiện chưa có tựa truyện nào phù hợp.'
        5. Trả lời ngắn gọn, lịch sự.
        """;

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .build();
    }

    public String chatWithUser(String userMessage, String sessionId) {
        try {
            log.info("Received user message: {}", userMessage);
            return this.chatClient.prompt()
                    .user(userMessage)
                    .toolNames("searchStoryByContent", "getAvailableGenres")
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI Model bị lỗi hoặc Timeout: {}", e.getMessage());
            return "Xin lỗi, hệ thống AI đang quá tải lúc này. Vui lòng hỏi lại sau ít phút!";
        }
    }
}
