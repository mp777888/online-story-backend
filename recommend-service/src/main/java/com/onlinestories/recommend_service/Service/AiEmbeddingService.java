package com.onlinestories.recommend_service.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiEmbeddingService {

    // Lấy API Key từ application.yaml
    @Value("${cohere.api.key}")
    private String cohereApiKey;

    // URL của Cohere API để tạo embedding
    private static final String COHERE_EMBEDDING_URL = "https://api.cohere.ai/v1/embed";

    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable(value = "search_embeddings", key = "#textToEmbed?.toLowerCase()?.trim()", unless = "#result == null")
    public float[] generateEmbedding(String textToEmbed) {
        if (textToEmbed == null || textToEmbed.isBlank()) {
            return fallbackVector();
        }

        try {
            log.info("Generating embedding for text: {}", textToEmbed);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cohereApiKey);
            String cleanText = textToEmbed.replace("\n", " ").trim();

            // Cấu trúc Request Body chuẩn của Gemini API
            Map<String, Object> requestBody = Map.of(
                    "texts", List.of(cleanText),
                    "model", "embed-multilingual-v3.0",
                    "input_type", "search_document"
            );


            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(COHERE_EMBEDDING_URL, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || !responseBody.containsKey("embeddings")) {
                log.warn("Cohere API response structure is invalid, using fallback vector.");
                return fallbackVector();
            }


            List<List<Double>> embeddingsList = (List<List<Double>>) responseBody.get("embeddings");
            List<Double> embeddingDoubleList = embeddingsList.get(0);

            float[] vectorResult = new float[embeddingDoubleList.size()];
            for (int i = 0; i < embeddingDoubleList.size(); i++) {
                vectorResult[i] = embeddingDoubleList.get(i).floatValue();
            }

            log.info("Successfully generated embedding with Cohere. Dimension: {}", vectorResult.length);
            return vectorResult;

        } catch (Exception e) {
            log.error("Failed to generate embedding with Cohere API. Fallback applied.", e);
            return fallbackVector();
        }
    }

    private float[] fallbackVector() {
        // LƯU Ý: Model embed-multilingual-v3.0 của Cohere luôn tạo ra Vector có độ lớn là 1024 chiều (không phải 768 như Gemini)
        float[] fallback = new float[1024];
        Arrays.fill(fallback, 0.0001f);
        return fallback;
    }
}
