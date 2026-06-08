package com.onlinestories.ai_service.service;

import com.onlinestories.common.exception.AppException;
import com.onlinestories.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageService {

    @Value("${siliconflow.api-key}")
    String apiKey;

    final WebClient webClient;

    public ImageService(WebClient.Builder webClientBuilder) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.webClient = webClientBuilder
                    .baseUrl("https://api.siliconflow.com")
                    .exchangeStrategies(strategies)
                    .build();
    }

    public String generateImage(String prompt) {
        String url = "/v1/images/generations";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "black-forest-labs/FLUX.1-schnell");
        requestBody.put("prompt", prompt);
        requestBody.put("image_size", "768x1024");
        requestBody.put("prompt_enhancement", true);

        try {
            Map response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> imageList = (List<Map<String, Object>>) response.get("images");
            if (imageList == null) {
                imageList = (List<Map<String, Object>>) response.get("data");
            }

            String imageUrl = (String) imageList.getFirst().get("url");
            byte[] imageBytes = webClient.get()
                    .uri(URI.create(imageUrl))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (imageBytes != null) {
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                log.info("Successfully generated image from SiliconFlow for prompt: {}", prompt);
                return "data:image/jpeg;base64," + base64Image;
            }
        } catch (WebClientResponseException e) {
            String errorMessage = e.getResponseBodyAsString();
            log.error("SiliconFlow rejected request (Error {}): {}", e.getStatusCode(), errorMessage);
            throw new AppException(ErrorCode.GENERATE_IMAGE_ERROR);
        } catch (Exception e) {
            log.error("Error when creating/loading images: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.GENERATE_IMAGE_ERROR);
        }
        return null;
    }
}
