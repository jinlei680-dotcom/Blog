package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.UploadResponse;
import com.blog.service.FileService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/api/upload/image")
    public ApiResponse<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileService.uploadImage(file);
        return ApiResponse.success("图片上传成功", UploadResponse.builder().url(url).build());
    }

    @PostMapping("/api/users/avatar")
    public ApiResponse<UploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        String url = fileService.uploadAvatar(userId, file);
        return ApiResponse.success("头像上传成功", UploadResponse.builder().url(url).build());
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
