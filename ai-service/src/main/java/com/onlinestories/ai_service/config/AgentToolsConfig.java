package com.onlinestories.ai_service.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.onlinestories.ai_service.client.StoryClient;
import com.onlinestories.common.exception.ApiResponse;
import com.onlinestories.common.story.dto.GenreResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class AgentToolsConfig {
    private final StoryClient storyClient;

    public AgentToolsConfig(StoryClient storyClient) {
        this.storyClient = storyClient;
    }

    public record GenreRequest() {}
    public record ContentSearchRequest(String searchPhrase) {}
    public record TopStoriesRequest(String period, int topN) {}


    @Bean
    @Description("Dùng để lấy danh sách thể loại truyện có trong hệ thống, giúp người dùng biết được những thể loại truyện mà hệ thống đang có.")
    public Function<GenreRequest, String> getAvailableGenres() {
        return request -> {
            log.info("Agent calling getAvailableGenres");
            try {
                ApiResponse<List<GenreResponse>> response = storyClient.getAllGenres();
                if (response == null || response.getResult() == null) {
                    log.warn("No response or empty result");
                    return "Cơ sở dữ liệu đang quá tải, báo với người dùng hãy thử lại sau.";
                }

                if (response.getResult().isEmpty()) {
                    return "Hệ thống hiện chưa có thể loại truyện nào.";
                }

                String genresList = response.getResult().stream()
                        .map(GenreResponse::getName)
                        .collect(Collectors.joining(", "));
                return "Các thể loại truyện hiện có trong hệ thống: " + genresList;
            } catch (Exception e) {
                log.error("Error calling to Story Service: {}", e.getMessage());
                return "Cơ sở dữ liệu đang quá tải, báo với người dùng hãy thử lại sau.";
            }
        };
    }

    @Bean
    @Description("Dùng để tìm kiếm và gợi ý truyện dựa trên cốt truyện, nội dung, tính cách nhân vật hoặc các từ khóa ngữ nghĩa (ví dụ: truyện có công chúa, nhân vật chính điên rồ, bối cảnh tận thế...).")
    public Function<ContentSearchRequest, String> searchStoryByContent(VectorStore vectorStore) {
        return request -> {
            log.info("Agent calling searchStoryByContent with searchPhrase: {}", request.searchPhrase());

            try {
                SearchRequest searchRequest = SearchRequest.builder()
                        .query(request.searchPhrase())
                        .topK(10)
                        .build();

                List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

                // Dùng Java Stream để lọc truyện PUBLISHED
                String context = similarDocs.stream()
                        .filter(doc -> "PUBLISHED".equals(doc.getMetadata().getOrDefault("status", "Unknown").toString()))
                        .limit(3)
                        .map(doc -> {
                            String storyTitle = doc.getMetadata().getOrDefault("storyTitle", "Chưa rõ").toString();
                            String storyId = doc.getMetadata().getOrDefault("storyId", "Unknown").toString();
                            return String.format("Tên truyện: %s (Mã ID truyện: %s) \n---\nTrích đoạn nội dung: %s",
                                    storyTitle, storyId, doc.getText());
                        })
                        .collect(Collectors.joining("\n\n=====\n\n"));

                if (context.isEmpty()) {
                    return "Không tìm thấy dữ liệu nào phù hợp với yêu cầu trong hệ thống truyện PUBLISHED.";
                }

                return "ĐÂY LÀ DỮ LIỆU TỪ HỆ THỐNG, HÃY DỰA VÀO ĐÂY ĐỂ TRẢ LỜI:\n" + context;

            } catch (Exception e) {
                log.error("Lỗi khi OpenSearch query: {}", e.getMessage());
                return "Cơ sở dữ liệu đang quá tải, báo với người dùng hãy thử lại sau.";
            }
        };
    }

    @Bean
    @Description("Dùng để lấy danh sách CÁC TRUYỆN ĐƯỢC XEM NHIỀU NHẤT (Top truyện hot, phổ biến, top-view) THEO THỜI GIAN. Tham số 'period' BẮT BUỘC chỉ được phép nhận 1 trong 4 giá trị Tiếng Anh sau: 'TODAY' (ngày), 'WEEK' (tuần), 'MONTH' (tháng), 'ALL_TIME' (hiện tại/tất cả).")
    public Function<TopStoriesRequest, String> getTopStoriesFunction() {
        return request -> {
            log.info("Agent đang gọi getTopStoriesFunction với period: {}", request.period());

            try {
                // Đảm bảo AI truyền đúng tên biến Period (nếu nó truyền tiếng việt sẽ bị lỗi)
                String validPeriod = request.period().toUpperCase();
                if (!List.of("TODAY", "WEEK", "MONTH", "ALL_TIME").contains(validPeriod)) {
                    validPeriod = "WEEK";
                }

                // Chọc sang story-service để lấy dữ liệu top truyện được xem nhiều nhất
                ApiResponse<JsonNode> response =
                        storyClient.getTopViewedStories(validPeriod, 0, request.topN());

                if (response == null || response.getResult() == null) {
                    return "Không thể lấy thông tin top truyện lúc này.";
                }

                // Dùng JsonNode gắp xuất trực tiếp mảng content
                JsonNode contentArray = response.getResult().get("content");
                if (contentArray == null || contentArray.isEmpty()) {
                    return "Hiện tại chưa có xếp hạng bảng vàng cho thời gian này.";
                }

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < contentArray.size(); i++) {
                    JsonNode story = contentArray.get(i);
                    String title = story.get("title").asText();
                    String id = story.get("storyId").asText();
                    int views = story.get("numberOfViews").asInt();

                    sb.append(i + 1).append(". Tên truyện: ").append(title)
                            .append(" (Mã ID truyện: ").append(id).append(") ")
                            .append("- Lượt xem: ").append(views)
                            .append("\n");
                }

                return "ĐÂY LÀ KẾT QUẢ TOP TRUYỆN ĐƯỢC XEM NHIỀU NHẤT TỪ HỆ THỐNG:\n" + sb;

            } catch (Exception e) {
                log.error("Lỗi khi lấy top truyện bằng AI Tool: {}", e.getMessage());
                return "Hệ thống truyện đang bận hoặc quá tải, không thể xem xếp hạng lúc này.";
            }
        };
    }
}
