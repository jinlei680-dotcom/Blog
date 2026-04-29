package com.blog.service.impl;

import com.blog.dto.*;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.repository.*;
import com.blog.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final CommentRepository commentRepository;
    private final ArticleTagRepository articleTagRepository;
    private final TagRepository tagRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository,
                              UserRepository userRepository,
                              ArticleLikeRepository articleLikeRepository,
                              CommentRepository commentRepository,
                              ArticleTagRepository articleTagRepository,
                              TagRepository tagRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.articleLikeRepository = articleLikeRepository;
        this.commentRepository = commentRepository;
        this.articleTagRepository = articleTagRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public ArticleDTO create(Long userId, CreateArticleRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("正文不能为空");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        Article article = Article.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .author(author)
                .featured(request.getFeatured() != null && request.getFeatured())
                .build();

        article = articleRepository.save(article);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            createArticleTagAssociations(article, request.getTagIds());
        }

        return toArticleDTO(article);
    }

    @Override
    @Transactional
    public ArticleDTO update(Long userId, Long articleId, UpdateArticleRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("正文不能为空");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));

        if (!isCurrentUserAdmin() && !article.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("无权修改此文章");
        }

        article.setTitle(request.getTitle().trim());
        article.setContent(request.getContent().trim());
        if (request.getFeatured() != null) {
            article.setFeatured(request.getFeatured());
        }
        article = articleRepository.save(article);

        // Update tag associations: clear old, create new
        articleTagRepository.deleteByArticleId(articleId);
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            createArticleTagAssociations(article, request.getTagIds());
        }

        return toArticleDTO(article);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));

        if (!isCurrentUserAdmin() && !article.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("无权删除此文章");
        }

        commentRepository.deleteByArticleId(articleId);
        articleLikeRepository.deleteByArticleId(articleId);
        articleTagRepository.deleteByArticleId(articleId);
        articleRepository.delete(article);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleSummaryDTO> list(int page, int size) {
        Page<Article> articles = articleRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(page, size));
        return articles.map(this::toArticleSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetailDTO getDetail(Long articleId, Long currentUserId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));

        long likeCount = articleLikeRepository.countByArticleId(articleId);
        boolean likedByCurrentUser = false;
        if (currentUserId != null) {
            likedByCurrentUser = articleLikeRepository.existsByUserIdAndArticleId(currentUserId, articleId);
        }

        return ArticleDetailDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .authorId(article.getAuthor().getId())
                .authorName(article.getAuthor().getUsername())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .likeCount(likeCount)
                .likedByCurrentUser(likedByCurrentUser)
                .tags(getTagsForArticle(articleId))
                .build();
    }

    private ArticleDTO toArticleDTO(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .authorId(article.getAuthor().getId())
                .authorName(article.getAuthor().getUsername())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    private ArticleSummaryDTO toArticleSummaryDTO(Article article) {
        long likeCount = articleLikeRepository.countByArticleId(article.getId());
        return ArticleSummaryDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .authorName(article.getAuthor().getUsername())
                .authorId(article.getAuthor().getId())
                .createdAt(article.getCreatedAt())
                .likeCount(likeCount)
                .featured(article.getFeatured())
                .tags(getTagsForArticle(article.getId()))
                .build();
    }

    private List<TagDTO> getTagsForArticle(Long articleId) {
        List<ArticleTag> articleTags = articleTagRepository.findByArticleId(articleId);
        if (articleTags == null || articleTags.isEmpty()) {
            return Collections.emptyList();
        }
        return articleTags.stream()
                .map(at -> TagDTO.builder()
                        .id(at.getTag().getId())
                        .name(at.getTag().getName())
                        .build())
                .collect(Collectors.toList());
    }

    private void createArticleTagAssociations(Article article, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new EntityNotFoundException("标签不存在: " + tagId));
            ArticleTag articleTag = ArticleTag.builder()
                    .article(article)
                    .tag(tag)
                    .build();
            articleTagRepository.save(articleTag);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleSummaryDTO> search(String keyword, int page, int size) {
        Page<Article> articles = articleRepository.searchByKeyword(keyword, PageRequest.of(page, size));
        return articles.map(this::toArticleSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleSummaryDTO> listByUser(Long userId, int page, int size) {
        Page<Article> articles = articleRepository.findByAuthorIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return articles.map(this::toArticleSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleSummaryDTO> listFeatured(int page, int size) {
        Page<Article> articles = articleRepository.findByFeaturedTrueOrderByCreatedAtDesc(PageRequest.of(page, size));
        return articles.map(this::toArticleSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SiteStatsDTO getSiteStats() {
        return SiteStatsDTO.builder()
                .articleCount(articleRepository.count())
                .totalWords(articleRepository.sumContentLength())
                .totalLikes(articleLikeRepository.count())
                .userCount(userRepository.count())
                .commentCount(commentRepository.count())
                .build();
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
