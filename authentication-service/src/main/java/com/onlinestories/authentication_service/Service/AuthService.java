package com.onlinestories.authentication_service.Service;

import com.onlinestories.authentication_service.DTO.AuthRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthService {
    @Value("${keycloak.server-url}")
    private String authServerUrl;

    @Value("${keycloak.client-id}")
    private String clientId;
    
    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    final WebClient.Builder webClient;

    public ResponseEntity<Map<String, Object>> authenticate(AuthRequest request) {
        log.info("Requesting token for user: {}", request.getUsername());

        // Tạo URL endpoint để lấy token từ Keycloak
        String url = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", request.getUsername());
        formData.add("password", request.getPassword());
        formData.add("scope", "openid");

        // Gửi yêu cầu POST đến Keycloak để lấy token
        Mono<Map> responseMono = webClient.build()
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class);

        // Block và lấy kết quả
        Map<String, Object> responseMap = responseMono.block();
        log.info("Received token for user: {}", request.getUsername());
        return ResponseEntity.ok(responseMap);
    }

    public ResponseEntity<String> refreshToken(String refreshToken){
        log.info("Requesting token refresh with refresh token: {}", refreshToken);
        String url = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        Mono<Map> responseMono = webClient.build()
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class);

        Map<String, Object> responseMap = responseMono.block();
        log.info("Received refreshed token");
        return ResponseEntity.ok(responseMap.toString());
    }

    public ResponseEntity<String> logout(String refreshToken) {
        log.info("Logging out with refresh token: {}", refreshToken);
        String url = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        Mono<String> responseMono = webClient.build()
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class);

        String response = responseMono.block();
        log.info("Logout response: {}", response);
        return ResponseEntity.ok(response);
    }
}
