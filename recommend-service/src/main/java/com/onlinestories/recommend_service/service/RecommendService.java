package com.onlinestories.recommend_service.service;

import com.onlinestories.recommend_service.entity.StorySearchItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<Object> searchAll(String keyword, int page, int size) {
        List<Object> results = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);
        try {
//            float[] queryVector = aiEmbeddingService.generateEmbedding(keyword);

            SearchRequest request = SearchRequest.of(s -> s
                    .index("users","stories")
                    .source(src -> src.filter(f -> f.excludes("embedding")))
                    .from((int) pageable.getOffset())
                    .size(pageable.getPageSize())
                    .query(q -> q
                            .bool(b -> b
                                    // MUST: Tìm kiếm truyện/tác giả có liên quan đến keyword
                                    .must(m -> m
                                            .multiMatch(mm -> mm
                                                    .query(keyword)
                                                            .fields("title^3", "authorName^2", "nickname^2", "tags^2", "genres^2", "description")
//                                                    .type(TextQueryType.CrossFields)
                                                    .operator(Operator.Or)
                                                    .fuzziness("AUTO")
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
            );

            // Fetch từ OpenSearch dưới dạng Object
            SearchResponse<Object> response = openSearchClient.search(request, Object.class);
            response.hits().hits().forEach(hit -> results.add(hit.source()));

        } catch (IOException e) {
            log.error("Search error", e);
        }
        return new PageImpl<>(results, pageable, results.size());
    }

    /**
     * GỢI Ý VECTOR (kNN SEARCH) - TRUYỆN LIÊN QUAN
     */
    public Page<StorySearchItem> recommendSimilarStories(String storyId, int page, int size) {
        List<StorySearchItem> recommendations = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);
        try {
            GetResponse<StorySearchItem> documentResponse = openSearchClient.get(g -> g
                            .index("stories")
                            .id(storyId),
                    StorySearchItem.class
            );
            if (!documentResponse.found() || documentResponse.source() == null || documentResponse.source().getEmbedding() == null) {
                log.warn("Document with ID {} not found or missing embedding, cannot perform kNN search.", storyId);
                return new PageImpl<>(recommendations, pageable, 0);
            }

            float[] targetVector = documentResponse.source().getEmbedding();
            String authorId = documentResponse.source().getAuthorId();
            List<String> sourceGenres = documentResponse.source().getGenres();

            SearchRequest request = SearchRequest.of(s -> s
                    .index("stories")
                    .from((int) pageable.getOffset())
                    .source(src -> src.filter(f -> f.excludes("embedding")))
                    .size(pageable.getPageSize())
                    .query(q -> q
                            .functionScore(fs -> fs
                                    .query(qq -> qq
                                            .bool(b -> b
                                                    // Loại bỏ chính truyện đang xem khỏi danh sách gợi ý
                                                    .mustNot(mn -> mn
                                                            .term(t -> t
                                                                    .field("_id")
                                                                    .value(FieldValue.of(storyId))
                                                            )
                                                    )
                                                    // Lấy top gần nhất qua vector embedding (Cosinesimil)
                                                    .must(m -> m
                                                            .knn(k -> k
                                                                    .field("embedding")
                                                                    .vector(targetVector)
                                                                    .k(10)
                                                                    .boost(10.0f)
                                                            )
                                                    )
                                                    .filter(f -> f
                                                            .terms(t -> t
                                                                    .field("status")
                                                                    .terms(t2 -> t2.value(List.of(
                                                                            FieldValue.of("ONGOING"),
                                                                            FieldValue.of("COMPLETED")
                                                                    )))
                                                            )
                                                    )
                                                    .should(sh -> sh
                                                            .term(t -> t
                                                                    .field("authorId")
                                                                    .value(FieldValue.of(authorId))
                                                                    .boost(2.0f) // Nhân đôi điểm cho truyện cùng tác giả
                                                            )
                                                    )
                                                    .should(sh -> sh
                                                            .terms(t -> t
                                                                    .field("genres")
                                                                    .terms(t2 -> t2.value(
                                                                            sourceGenres != null
                                                                                    ? sourceGenres.stream().map(FieldValue::of).toList()
                                                                                    : List.of()
                                                                    ))
                                                                    .boost(1.5f) // Cộng thêm điểm cho truyện có chung thể loại
                                                            )
                                                    )
                                            )
                                    )
                                    // Tăng điểm cho truyện có nhiều view và rating cao
                                    .functions(List.of(
                                            FunctionScore.of(f -> f
                                                    .fieldValueFactor(fv -> fv
                                                            .field("numberOfViews")
                                                            .factor(0.5)
                                                            .modifier(FieldValueFactorModifier.Log1p)
                                                            .missing(0.0)
                                                    )
                                            ),
                                            FunctionScore.of(f -> f
                                                    .fieldValueFactor(fv -> fv
                                                            .field("averageRatingScore")
                                                            .factor(1.2)
                                                            .missing(1.0)
                                                    )
                                            )
                                    ))
                                    .scoreMode(FunctionScoreMode.Sum)
                                    .boostMode(FunctionBoostMode.Multiply)
                            )
                    )
            );

            SearchResponse<StorySearchItem> response = openSearchClient.search(request, StorySearchItem.class);
            response.hits().hits().forEach(hit -> recommendations.add(hit.source()));
        } catch (IOException e) {
            log.error("Recommend error", e);
        }
        return new PageImpl<>(recommendations, pageable, recommendations.size());
    }

    /**
     * GỢI Ý TRUYỆN DỰA TRÊN THỂ LOẠI YÊU THÍCH (FAVORITE GENRES)
     */
    public Page<StorySearchItem> recommendStoriesByGenres(
            List<String> genres, int page, int size) {
        List<StorySearchItem> recommendations = new ArrayList<>();
        Pageable pageable = PageRequest.of(page,size);

        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index("stories")
                    .from((int) pageable.getOffset())
                    .source(src -> src.filter(f -> f.excludes("embedding")))
                    .size(pageable.getPageSize())
                    .query(q -> q
                            .functionScore(fs -> fs
                                    // Filter bằng TỪNG THỂ LOẠI
                                    .query(qq -> qq
                                            .bool(b -> b
                                                    .filter(f -> f
                                                            .terms(t -> t
                                                                    .field("status")
                                                                    .terms(t2 -> t2.value(List.of(
                                                                            FieldValue.of("ONGOING"),
                                                                            FieldValue.of("COMPLETED")
                                                                    )))
                                                            )
                                                    )
                                                    .must(m -> m
                                                            .terms(t -> t
                                                                    .field("genres")
                                                                    .terms(t2 -> t2.value(genres.stream().map(FieldValue::of).toList()))
                                                            )
                                                    )
                                            )
                                    )
                                    // Tăng điểm cho truyện có nhiều view và rating cao
                                    .functions(List.of(
                                            FunctionScore.of(f -> f
                                                    .fieldValueFactor(fv -> fv
                                                            .field("numberOfViews")
                                                            .factor(0.5)
                                                            .modifier(FieldValueFactorModifier.Log1p)
                                                            .missing(0.0)
                                                    )
                                            ),
                                            FunctionScore.of(f -> f
                                                    .fieldValueFactor(fv -> fv
                                                            .field("averageRatingScore")
                                                            .factor(1.2)
                                                            .missing(1.0)
                                                    )
                                            )
                                    ))
                                    .scoreMode(FunctionScoreMode.Sum)
                                    .boostMode(FunctionBoostMode.Multiply)
                            )
                    )
            );

            SearchResponse<StorySearchItem> response = openSearchClient.search(request, StorySearchItem.class);
            response.hits().hits().forEach(hit -> recommendations.add(hit.source()));

        } catch (IOException e) {
            log.error("Error fetching recommendations by genres", e);
        }
        return new PageImpl<>(recommendations, pageable, recommendations.size());
    }

}
