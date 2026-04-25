package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.entity.ArticleLike;
import com.blog.entity.User;
import com.blog.repository.ArticleLikeRepository;
import com.blog.repository.ArticleRepository;
import com.blog.repository.UserRepository;
import com.blog.service.LikeService;
import com.blog.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LikeServiceImpl(ArticleLikeRepository articleLikeRepository,
                           ArticleRepository articleRepository,
                           UserRepository userRepository,
                           NotificationService notificationService) {
        this.articleLikeRepository = articleLikeRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        Optional<ArticleLike> existing = articleLikeRepository.findByUserIdAndArticleId(userId, articleId);

        if (existing.isPresent()) {
            articleLikeRepository.delete(existing.get());
            return false;
        } else {
            ArticleLike like = ArticleLike.builder()
                    .user(user)
                    .article(article)
                    .build();
            articleLikeRepository.save(like);
            notificationService.createLikeNotification(userId, articleId);
            return true;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByArticle(Long articleId) {
        return articleLikeRepository.countByArticleId(articleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long userId, Long articleId) {
        return articleLikeRepository.existsByUserIdAndArticleId(userId, articleId);
    }
}
