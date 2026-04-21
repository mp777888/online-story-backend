package com.onlinestories.ai_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageService {
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateImageBase64(String promptText) {
        try {
            log.info("Calling Pollinations API with prompt: {}", promptText);

            //Encode prompt
            String encodedPrompt = URLEncoder.encode(promptText, StandardCharsets.UTF_8);

            // Tạo URL gọi API của Pollinations
            // (tỉ lệ 512x768 hợp làm ảnh bìa truyện)
            String url = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=512&height=768&nologo=true";

            // Gọi API và nhận về ảnh dưới dạng byte array
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            byte[] imageBytes = response.getBody();

            if (imageBytes != null) {
                // Mã hóa lại thành Base64 như cũ để không làm gãy luồng của Controller
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                log.info("Successfully generated image from Pollinations API");
                return "data:image/jpeg;base64," + base64Image;
            }

        } catch (Exception e) {
            log.error("Error generating image from Pollinations API: {}", e.getMessage());
            return "https://t3.ftcdn.net/jpg/04/62/93/66/360_F_462936689_BpEEcxfgMuYPfTaIAOC1tCDurmsno7Sp.jpg";
        }

        return null;
    }
}
