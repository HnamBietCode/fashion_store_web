package com.fashionstore.fashion_store.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    /**
     * Lưu file ảnh upload vào src/main/resources/static/uploads/
     * Trả về URL path dạng /uploads/filename để dùng trong <img src="...">
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            return null;

        // Kiểm tra loại file
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (jpg, png, webp, gif...)");
        }

        // Kiểm tra kích thước (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File không được vượt quá 10MB");
        }

        // Dùng working directory để build path tuyệt đối đến static/uploads
        // Khi chạy trong IDE, working dir = project root (fashion-store/)
        Path uploadPath = Paths.get("src", "main", "resources", "static", "uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo filename unique
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        // Trả về URL path từ context root
        return "/uploads/" + filename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return ".jpg";
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}
