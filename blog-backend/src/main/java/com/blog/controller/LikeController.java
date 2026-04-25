package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.LikeResponse;
import com.blog.service.LikeService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/api/articles/{articleId}/like")
    public ApiResponse<LikeResponse> toggleLike(@PathVariable Long articleId) {
        Long userId = getCurrentUserId();
        boolean liked = likeService.toggleLike(userId, articleId);
        long likeCount = likeService.countByArticle(articleId);

        LikeResponse response = LikeResponse.builder()
                .liked(liked)
                .likeCount(likeCount)
                .build();

        return ApiResponse.success(response);
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
