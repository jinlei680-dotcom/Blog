package com.blog.service.impl;

import com.blog.dto.NotificationDTO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.Notification;
import com.blog.entity.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.NotificationRepository;
import com.blog.repository.UserRepository;
import com.blog.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   ArticleRepository articleRepository,
                                   CommentRepository commentRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void createCommentNotification(Long senderId, Long articleId, Long commentId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));

        Long recipientId = article.getAuthor().getId();
        if (recipientId.equals(senderId)) {
            return; // skip self-notification
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        User recipient = article.getAuthor();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));

        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .type("COMMENT")
                .article(article)
                .comment(comment)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createReplyNotification(Long senderId, Long parentCommentId, Long commentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new EntityNotFoundException("父评论不存在"));

        Long recipientId = parentComment.getUser().getId();
        if (recipientId.equals(senderId)) {
            return; // skip self-notification
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        User recipient = parentComment.getUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("评论不存在"));

        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .type("REPLY")
                .article(parentComment.getArticle())
                .comment(comment)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createLikeNotification(Long senderId, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("文章不存在"));

        Long recipientId = article.getAuthor().getId();
        if (recipientId.equals(senderId)) {
            return; // skip self-notification
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        User recipient = article.getAuthor();

        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .type("LIKE")
                .article(article)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> listByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::toDTO);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("通知不存在"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("无权操作此通知");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalse(userId);
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .senderId(n.getSender().getId())
                .senderName(n.getSender().getUsername())
                .senderAvatarUrl(n.getSender().getAvatarUrl())
                .articleId(n.getArticle() != null ? n.getArticle().getId() : null)
                .articleTitle(n.getArticle() != null ? n.getArticle().getTitle() : null)
                .commentId(n.getComment() != null ? n.getComment().getId() : null)
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
