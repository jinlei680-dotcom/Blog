package com.blog.service.impl;

import com.blog.entity.User;
import com.blog.repository.UserRepository;
import com.blog.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private final UserRepository userRepository;

    public FileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir, "images"));
            Files.createDirectories(Paths.get(uploadDir, "avatars"));
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        validateFile(file);
        String filename = generateFilename(file.getOriginalFilename());
        Path targetPath = Paths.get(uploadDir, "images", filename);
        saveFile(file, targetPath);
        return "/uploads/images/" + filename;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        validateFile(file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        String filename = generateFilename(file.getOriginalFilename());
        Path targetPath = Paths.get(uploadDir, "avatars", filename);
        saveFile(file, targetPath);

        String avatarUrl = "/uploads/avatars/" + filename;
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件格式，仅支持JPEG、PNG、GIF、WebP");
        }
    }

    String generateFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private void saveFile(MultipartFile file, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }
}
