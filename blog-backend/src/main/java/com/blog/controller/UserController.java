package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.ArticleSummaryDTO;
import com.blog.dto.UserProfileDTO;
import com.blog.service.ArticleService;
import com.blog.service.ProfileService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

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
}
