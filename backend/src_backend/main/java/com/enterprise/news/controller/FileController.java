package com.enterprise.news.controller;

import com.enterprise.news.common.ApiResponse;
import com.enterprise.news.dto.file.UploadResponse;
import com.enterprise.news.exception.BusinessException;
import com.enterprise.news.security.AuthContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private static final Set<String> IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final Set<String> VIDEO_TYPES = Set.of("mp4", "mov", "avi", "mkv", "webm", "m4v");

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (AuthContext.getUser() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        if (file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        String originalName = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalName);
        if (!StringUtils.hasText(ext)) {
            throw new BusinessException("文件格式不支持");
        }
        ext = ext.toLowerCase(Locale.ROOT);
        String type;
        if (IMAGE_TYPES.contains(ext)) {
            type = "image";
        } else if (VIDEO_TYPES.contains(ext)) {
            type = "video";
        } else {
            throw new BusinessException("仅支持图片或视频文件上传");
        }
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Files.copy(file.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return ApiResponse.ok("上传成功", new UploadResponse("/uploads/" + filename, type, originalName));
    }
}