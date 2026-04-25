package com.onlinestories.ai_service.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ReportingAssistantService {
    ChatClient.Builder chatClientBuilder;

    public boolean isContentViolating(String content) {
        String systemPrompt = """
            Bạn là hệ thống kiểm duyệt nội dung tự động. 
            Nhiệm vụ của bạn là kiểm tra xem nội dung văn bản có chứa:
            - Ngôn từ tục tĩu, chửi thề (Toxicity)
            - Phân biệt chủng tộc, bôi nhọ tôn giáo, chính trị
            - Nội dung phản cảm, 18+ (NSFW)
            Nếu CÓ vi phạm, chỉ trả về đúng 1 chữ: "TRUE".
            Nếu KHÔNG vi phạm (nội dung an toàn), chỉ trả về đúng 1 chữ: "FALSE".
            Không giải thích gì thêm.
            """;

        String response = chatClientBuilder.defaultSystem(systemPrompt).build()
                .prompt().user(content).call().content();

        return "TRUE".equalsIgnoreCase(response != null ? response.trim() : "FALSE");
    }
}
