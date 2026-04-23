package com.onlinestories.ai_service.client;

import com.onlinestories.common.media.dto.UploadBase64Request;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "media-service")
public interface MediaClient {
    @PostMapping(value = "/api/media/uploadBase64")
    String uploadBase64(@RequestBody UploadBase64Request request);

    @DeleteMapping("/api/media/delete")
    String deleteFile(@RequestParam String url,
                      @RequestParam String resourceType);

}
