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

    public String improveGrammar(String content) {
        String systemPrompt = """
                Bạn là một biên tập viên văn học chuyên nghiệp. Nhiệm vụ của bạn là chỉnh sửa ngữ pháp,
                làm mượt mà cấu trúc câu, và cải thiện cách diễn đạt cho đoạn văn bản được cung cấp.
                YÊU CẦU QUAN TRỌNG:
                - Chỉnh sửa các câu lủng củng, câu thiếu chủ/vị ngữ để mạch văn trôi chảy, tự nhiên hơn.
                - Chỉnh sửa theo ngôn ngữ của đoạn văn bản gốc, không thay đổi phong cách viết của tác giả.
                - Sử dụng từ ngữ chuẩn xác hơn nếu cần, nhưng BẮT BUỘC giữ nguyên cốt truyện và ý nghĩa gốc.
                - CHỈ TRẢ VỀ đoạn văn bản đã được tái cấu trúc và chỉnh sửa.
                - TUYỆT ĐỐI KHÔNG thêm lời bình luận, giải thích hay câu chào hỏi.
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
                Bạn chỉ được gợi ý cho tác giả bối cảnh, câu chuyện, nhân vật dựa trên những gì họ đang viết.
                Không sử dụng những kiến thức bạn đã biết trước đó để gợi ý cho tác giả.
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
