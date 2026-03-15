package com.onlinestories.media_service.Controller;

import com.onlinestories.media_service.Service.CloudinaryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MediaController {
    @Autowired
    private CloudinaryService cloudinaryService; // Logic upload như bài trước

    // API nhận MultipartFile và trả về URL
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("folder") String folder) { // Cho phép tham số folder để phân loại
        log.info("Received file upload request for folder: {}", folder);
        // Gọi Cloudinary upload
        String url = cloudinaryService.uploadFile(file, folder);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(
            @RequestParam String url,
            @RequestParam String resourceType) {
        log.info("Received file delete request for URL: {}", url);
        return ResponseEntity.ok(cloudinaryService.deleteFile(url, resourceType));
    }

}
