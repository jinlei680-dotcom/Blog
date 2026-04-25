package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.ArticleSummaryDTO;
import com.blog.dto.UserProfileDTO;
import com.blog.service.ArticleService;
import com.blog.service.ProfileService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ProfileService profileService;
    private final ArticleService articleService;

    public UserController(ProfileService profileService, ArticleService articleService) {
        this.profileService = profileService;
        this.articleService = articleService;
    }

    @GetMapping("/{userId}/profile")
    public ApiResponse<UserProfileDTO> getProfile(@PathVariable Long userId) {
        UserProfileDTO profile = profileService.getProfile(userId);
        return ApiResponse.success(profile);
    }

    @GetMapping("/{userId}/articles")
    public ApiResponse<Page<ArticleSummaryDTO>> getUserArticles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleSummaryDTO> articles = articleService.listByUser(userId, page, size);
        return ApiResponse.success(articles);
    }

    @PutMapping("/{userId}/bio")
    public ApiResponse<Void> updateBio(
            @PathVariable Long userId,
            @RequestBody java.util.Map<String, String> body) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权修改他人简介");
        }
        String bio = body.get("bio");
        if (bio != null && bio.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "简介不能超过500字");
        }
        profileService.updateBio(userId, bio);
        return ApiResponse.success(null);
    }
}
