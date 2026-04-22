package com.onlinestories.ai_service.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class WritingAssistantService {
    ChatClient.Builder chatClientBuilder;

    public String correctSpelling(String content) {
        String systemPrompt = """
                Bạn là một biên tập viên văn học xuất sắc. Nhiệm vụ của bạn là sửa lỗi chính tả,
                lỗi đánh máy và cấu trúc ngữ pháp lủng củng của đoạn văn bản được cung cấp.
                YÊU CẦU QUAN TRỌNG:
                - Giữ nguyên văn phong và ý nghĩa của tác giả, chỉ sửa lỗi.
                - CHỈ TRẢ VỀ đoạn văn bản đã được sửa.
                - TUYỆT ĐỐI KHÔNG thêm bất kỳ lời bình luận, giải thích hay câu chào hỏi nào.
                """;

        ChatClient client = chatClientBuilder.defaultSystem(systemPrompt).build();

        return client.prompt()
                .user(content)
                .call()
                .content();
    }

    public String suggestContent(String currentContent, String direction) {
        String systemPrompt = """
                Bạn là một tiểu thuyết gia và trợ lý viết truyện sáng tạo.
                Tác giả đang viết một câu chuyện và cần bạn viết tiếp hoặc đưa ra gợi ý tiếp diễn tóm tắt.
                Hãy viết tiếp tục đoạn văn bản một cách logic, văn phong cuốn hút, liền mạch, viết theo ngôn ngữ mà tác giả đang sử dụng.
                Nếu tác giả có định hướng cụ thể, hãy tuân theo định hướng đó.
                """;

        ChatClient client = chatClientBuilder.defaultSystem(systemPrompt).build();

        String userPrompt = String.format("""
                Đoạn nội dung hiện tại:
                "%s"
                
                Định hướng viết tiếp từ tác giả (nếu có): "%s"
                
                Hãy viết tiếp khoảng đoạn tiếp theo (khoảng 150-300 chữ):
                """, currentContent, (direction != null ? direction : "Hãy tự do sáng tạo diễn biến bất ngờ hợp lý"));

        return client.prompt()
                .user(userPrompt)
                .call()
                .content();
    }
}
