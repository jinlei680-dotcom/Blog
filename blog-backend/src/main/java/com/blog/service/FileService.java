package com.blog.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String uploadImage(MultipartFile file);

    String uploadAvatar(Long userId, MultipartFile file);
}
