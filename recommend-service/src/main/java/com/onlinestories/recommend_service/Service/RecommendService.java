package com.onlinestories.recommend_service.Service;

import com.onlinestories.recommend_service.Entity.StorySearchItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendService {

    private final OpenSearchClient openSearchClient;
    private final AiEmbeddingService aiEmbeddingService;

    /**
     * TÌM KIẾM ĐA THỰC THỂ (TRUYỆN + TÁC GIẢ)
     */
    public List<Object> searchAll(String keyword) {
        List<Object> results = new ArrayList<>();
        try {
//            float[] queryVector = aiEmbeddingService.generateEmbedding(keyword);

            SearchRequest request = SearchRequest.of(s -> s
                    .index("users","stories")
                    .source(src -> src.filter(f -> f.excludes("embedding")))
                    .query(q -> q
                            .bool(b -> b
                                    // MUST: Tìm kiếm truyện/tác giả có liên quan đến keyword
                                    .must(m -> m
                                            .multiMatch(mm -> mm
                                                    .query(keyword)
                                                    .fields("title^3", "authorName^2", "nickname^2", "description")
                                                    .type(TextQueryType.CrossFields)
                                                    .operator(Operator.Or)
                                            )
                                    )
                                    // SHOULD: Tìm kiếm truyện có embedding vector gần với query vector (tìm kiếm ngữ nghĩa)
//                                    .should(sh -> sh
//                                            // Giúp tìm ra các truyện miêu tả gián tiếp keyword
//                                            .knn(k -> k
//                                                    .field("embedding")
//                                                    .vector(queryVector)
//                                                    .k(20)
//                                            )
//                                    )
//                                    .minimumShouldMatch("1")
                                    // FILTER: Bắt buộc truyện phải có status != DRAFT
                                    .filter(f -> f
                                            .bool(filterBool -> filterBool
                                                    .should(sh -> sh
                                                            .terms(t -> t
                                                                    .field("status")
                                                                    .terms(t2 -> t2.value(List.of(
                                                                            FieldValue.of("ONGOING"),
                                                                            FieldValue.of("COMPLETED"))))
                                                            )
                                                    )
                                                    // Document là User không có trường status, cho phép lọt qua bộ lọc
                                                    .should(sh -> sh
                                                            .bool(userCheck -> userCheck
                                                                    .mustNot(mn -> mn.exists(e -> e.field("status")))
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
                    .size(20)
            );

            // Fetch từ OpenSearch dưới dạng Object
            SearchResponse<Object> response = openSearchClient.search(request, Object.class);
            response.hits().hits().forEach(hit -> results.add(hit.source()));

        } catch (IOException e) {
            log.error("Search error", e);
        }
        return results;
    }

    /**
     * GỢI Ý VECTOR (kNN SEARCH) - TRUYỆN LIÊN QUAN
     */
    public List<StorySearchItem> recommendSimilarStories(float[] targetVector) {
        List<StorySearchItem> recommendations = new ArrayList<>();
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index("stories")
                    .query(q -> q
                            .knn(k -> k
                                    .field("embedding")
                                    .vector(targetVector)
                                    .k(10) // Lấy top 10 gần nhất
                            )
                    )
            );

            SearchResponse<StorySearchItem> response = openSearchClient.search(request, StorySearchItem.class);
            response.hits().hits().forEach(hit -> recommendations.add(hit.source()));
        } catch (IOException e) {
            log.error("Recommend error", e);
        }
        return recommendations;
    }

    /**
     * GỢI Ý TRUYỆN DỰA TRÊN THỂ LOẠI YÊU THÍCH (FAVORITE GENRES)
     */
    public List<StorySearchItem> recommendStoriesByGenres(List<String> genres, int size) {
        List<StorySearchItem> recommendations = new ArrayList<>();

        // Cấu hình danh sách thể loại vào format FieldValue của OpenSearch
        List<FieldValue> genreValues = genres.stream()
                .map(FieldValue::of)
                .toList();

        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index("stories")
                    .query(q -> q
                            .bool(b -> b
                                    // MUST: Truyện phải nằm trong danh sách thể loại yêu thích này
                                    .must(m -> m
                                            .terms(t -> t
                                                    .field("genres")
                                                    .terms(t2 -> t2.value(genreValues))
                                            )
                                    )
                                    // FILTER: đảm bảo truyện đang hiển thị (Ongoing / Completed)
                                    .filter(f -> f
                                            .terms(t -> t
                                                    .field("status.keyword")
                                                    .terms(t2 -> t2.value(List.of(
                                                            FieldValue.of("ONGOING"),
                                                            FieldValue.of("COMPLETED")
                                                    )))
                                            )
                                    )
                            )
                    )
                    .size(size)
            );

            SearchResponse<StorySearchItem> response = openSearchClient.search(request, StorySearchItem.class);
            response.hits().hits().forEach(hit -> recommendations.add(hit.source()));

        } catch (IOException e) {
            log.error("Error fetching recommendations by genres", e);
        }
        return recommendations;
    }

}
