package com.onlinestories.ai_service.config;

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
}
