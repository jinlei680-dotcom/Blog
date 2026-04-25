package com.blog.service;

import com.blog.dto.*;
import org.springframework.data.domain.Page;

public interface ArticleService {

    ArticleDTO create(Long userId, CreateArticleRequest request);

    ArticleDTO update(Long userId, Long articleId, UpdateArticleRequest request);

    void delete(Long userId, Long articleId);

    Page<ArticleSummaryDTO> list(int page, int size);

    ArticleDetailDTO getDetail(Long articleId, Long currentUserId);

    Page<ArticleSummaryDTO> search(String keyword, int page, int size);

    Page<ArticleSummaryDTO> listByUser(Long userId, int page, int size);
}
