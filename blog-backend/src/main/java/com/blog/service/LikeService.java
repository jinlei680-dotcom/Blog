package com.blog.service;

public interface LikeService {

    boolean toggleLike(Long userId, Long articleId);

    long countByArticle(Long articleId);

    boolean isLikedByUser(Long userId, Long articleId);
}
