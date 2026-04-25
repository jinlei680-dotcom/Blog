package com.blog.service.impl;

import com.blog.dto.CommentDTO;
import com.blog.dto.CommentTreeDTO;
import com.blog.dto.CreateCommentRequest;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.UserRepository;
import com.blog.service.CommentService;
import com.blog.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              ArticleRepository articleRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public CommentDTO create(Long userId, Long articleId, CreateCommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .user(user)
                .article(article)
                .build();

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("父评论不存在"));
            comment.setParent(parent);
        }

        comment = commentRepository.save(comment);

        // Create notifications
        if (request.getParentId() != null) {
            notificationService.createReplyNotification(userId, request.getParentId(), comment.getId());
        } else {
            notificationService.createCommentNotification(userId, articleId, comment.getId());
        }

        return toCommentDTO(comment);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));

        if (!isCurrentUserAdmin() && !comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("无权删除此评论");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentTreeDTO> listByArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new EntityNotFoundException("文章不存在");
        }

        List<Comment> topLevelComments = commentRepository
                .findByArticleIdAndParentIsNullOrderByCreatedAtAsc(articleId);

        return topLevelComments.stream()
                .map(this::toCommentTreeDTO)
                .collect(Collectors.toList());
    }

    private CommentDTO toCommentDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .articleId(comment.getArticle().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private CommentTreeDTO toCommentTreeDTO(Comment comment) {
        List<CommentTreeDTO> childDTOs = comment.getChildren().stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(this::toCommentTreeDTO)
                .collect(Collectors.toList());

        return CommentTreeDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .createdAt(comment.getCreatedAt())
                .children(childDTOs)
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
