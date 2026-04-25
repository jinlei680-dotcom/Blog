package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.*;
import com.blog.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ApiResponse<ArticleDTO> create(@RequestBody CreateArticleRequest request) {
        Long userId = getCurrentUserId();
        ArticleDTO article = articleService.create(userId, request);
        return ApiResponse.success("文章创建成功", article);
    }

    @PutMapping("/{id}")
    public ApiResponse<ArticleDTO> update(@PathVariable Long id,
                                          @RequestBody UpdateArticleRequest request) {
        Long userId = getCurrentUserId();
        ArticleDTO article = articleService.update(userId, id, request);
        return ApiResponse.success("文章更新成功", article);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        articleService.delete(userId, id);
        return ApiResponse.success("文章删除成功", null);
    }

    @GetMapping
    public ApiResponse<Page<ArticleSummaryDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleSummaryDTO> articles = articleService.list(page, size);
        return ApiResponse.success(articles);
    }

    @GetMapping("/search")
    public ApiResponse<Page<ArticleSummaryDTO>> search(
            @RequestParam("q") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (q == null || q.trim().isEmpty()) {
            return ApiResponse.error(400, "搜索关键词不能为空");
        }
        Page<ArticleSummaryDTO> results = articleService.search(q.trim(), page, size);
        return ApiResponse.success(results);
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailDTO> getDetail(@PathVariable Long id) {
        Long currentUserId = getCurrentUserIdOrNull();
        ArticleDetailDTO article = articleService.getDetail(id, currentUserId);
        return ApiResponse.success(article);
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        // "anonymousUser" string for unauthenticated users
        return null;
    }
}
