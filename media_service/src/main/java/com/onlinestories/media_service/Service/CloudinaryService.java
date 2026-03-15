package com.onlinestories.media_service.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folderName) {
        try {
            // 1. Xác định resource_type (Quan trọng với Cloudinary)
            // Cloudinary chia làm 2 nhóm chính: "image" (ảnh) và "video" (bao gồm cả audio)
            String resourceType = "auto";
            String contentType = file.getContentType();

            if (contentType != null) {
                if (contentType.startsWith("image")) {
                    resourceType = "image";
                } else if (contentType.startsWith("audio") || contentType.startsWith("video")) {
                    resourceType = "video";
                }
            }

            // 2. Cấu hình tham số
            Map params = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", resourceType,
                    "public_id", generateFileName(file) // Tạo tên file ngẫu nhiên
            );

            // 3. Upload (Dùng hàm upload của library nhận byte[] từ MultipartFile)
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            // 4. Trả về URL
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            log.error("Upload failed", e);
            throw new RuntimeException("Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }

    public String deleteFile(String url, String resourceType) {
        try {
            String publicId = extractPublicId(url);
            Map params = ObjectUtils.asMap(
                    "resource_type", resourceType
            );
            cloudinary.uploader().destroy(publicId, params);
            return "File deleted successfully";
        } catch (IOException e) {
            log.error("Delete failed", e);
            throw new RuntimeException("Failed to delete file Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicId(String fileUrl) {
        try {
            // Lấy phần sau /upload/
            String[] parts = fileUrl.split("/upload/");
            if (parts.length == 2) {
                String urlAfterUpload = parts[1];

                // Cắt bỏ phần version nếu có
                if (urlAfterUpload.matches("^v\\d+/.*")) {
                    urlAfterUpload = urlAfterUpload.replaceFirst("^v\\d+/", "");
                }

                // Cắt bỏ phần đuôi mở rộng
                int lastDotIndex = urlAfterUpload.lastIndexOf('.');
                if (lastDotIndex != -1) {
                    return urlAfterUpload.substring(0, lastDotIndex);
                }
                return urlAfterUpload;
            }
        } catch (Exception e) {
            log.error("Failed to extract public ID from URL: {}", fileUrl, e);
        }
        return null;
    }

    // Hàm phụ trợ tạo tên file ngẫu nhiên để không bị trùng
    private String generateFileName(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String fileName = "file";
        if(originalName != null && !originalName.isEmpty()){
            fileName = originalName.substring(0, originalName.lastIndexOf('.'));
        }
        return fileName + "_" + UUID.randomUUID().toString();
    }
}
