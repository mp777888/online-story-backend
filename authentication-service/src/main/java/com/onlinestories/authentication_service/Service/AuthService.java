package com.onlinestories.authentication_service.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestories.authentication_service.Client.UserClient;
import com.onlinestories.authentication_service.DTO.AuthRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
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
    final UserClient userClient;

    public ResponseEntity<Map<String, Object>> authenticate(AuthRequest request) {
        try {
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
        } catch (Exception e) {
            log.error("Error during authentication for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed"));
        }
    }

    public ResponseEntity<Map<String, Object>> refreshToken(String refreshToken) {
        try {
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
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Token refresh failed"));
        }
    }

    public ResponseEntity<String> logout(String refreshToken) {
        try {
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
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.status(400).body("Logout failed");
        }
    }

    public ResponseEntity<Map<String, Object>> authenticateBySocial(String code, String redirectUri) {
        try {
            log.info("Authenticating with Social, code: {}, redirectUri: {}", code, redirectUri);
            String url = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("code", code);
            formData.add("redirect_uri", redirectUri);

            Mono<Map> responseMono = webClient.build()
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Map.class);

            Map<String, Object> responseMap = responseMono.block();
            log.info("Received token from Social authentication");
            if (responseMap == null || !responseMap.containsKey("access_token")) {
                log.error("Invalid response from Social authentication: {}", responseMap);
                return ResponseEntity.status(401).body(Map.of("error", "Social authentication failed"));
            }

            String accessToken = (String) responseMap.get("access_token");
            String[] chunks = accessToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payloadJson = new String(decoder.decode(chunks[1]));

            JsonNode payloadNode = new ObjectMapper().readTree(payloadJson);
            String userId = payloadNode.get("sub").asText();

            Boolean isUserExist = userClient.checkUserExistence(userId);

            Map<String, Object> finalResponse = new HashMap<>(responseMap);

            finalResponse.put("require_onboarding", !isUserExist);

            if (!isUserExist) {
                log.info("User {} needs onboarding. Flag set to true.", userId);
            }

            return ResponseEntity.ok(finalResponse);
        } catch (WebClientResponseException e) {
            log.error("Keycloak rejected the request. Status: {}, Error Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(401).body(Map.of("error", "Social authentication failed"));
        } catch (Exception e) {
            log.error("Error during Social authentication: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Social authentication failed"));
        }
    }

    public ResponseEntity<Map<String, Object>> forgotPassword(String email) {
        try {
            log.info("Initiating forgot password flow for email: {}", email);
            Keycloak keycloakAdmin = KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm(realm)
                    .grantType("client_credentials")
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            UsersResource usersResource = keycloakAdmin.realm(realm).users();
            List<UserRepresentation> users = usersResource.searchByEmail(email, true);

            if (users.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "If an account with that email exists, a password reset link has been sent"));
            }


            String keycloakUserId = users.getFirst().getId();

            usersResource.get(keycloakUserId).executeActionsEmail(List.of("UPDATE_PASSWORD"));

            log.info("Forgot password email sent successfully to: {}", email);
            return ResponseEntity.ok(Map.of("message", "Password reset initiated"));
        } catch (Exception e) {
            log.error("Error during forgot password flow for email {}: {}", email, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to initiate password reset"));
        }
    }
}
