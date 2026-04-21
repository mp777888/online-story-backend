package com.onlinestories.recommend_service.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class TestGeminiEmbeddingService {

    // Lấy API Key từ application.yaml
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // URL của Cohere API để tạo embedding
    private static final String GEMINI_EMBEDDING_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();

    public float[] generateEmbedding(String textToEmbed) {
        if (textToEmbed == null || textToEmbed.isBlank()) {
            return new float[3072];
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String cleanText = textToEmbed.replace("\n", " ").trim();

            Map<String, Object> requestBody = Map.of(
                    "content", Map.of(
                            "parts", List.of(
                                    Map.of("text", cleanText)
                            )
                    )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String urlWithKey = GEMINI_EMBEDDING_URL + geminiApiKey;

            ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("embedding")) {
                throw new RuntimeException("Gemini API response structure is invalid");
            }

            Map<String, Object> embeddingNode = (Map<String, Object>) responseBody.get("embedding");
            List<Double> embeddingDoubleList = (List<Double>) embeddingNode.get("values");

            float[] vectorResult = new float[embeddingDoubleList.size()];
            for (int i = 0; i < embeddingDoubleList.size(); i++) {
                vectorResult[i] = embeddingDoubleList.get(i).floatValue();
            }

            return vectorResult;

        } catch (Exception e) {
            log.error("Failed to generate embedding with Gemini API", e);
            throw new RuntimeException("Cannot generate embedding, process aborted", e);
        }
    }
}
