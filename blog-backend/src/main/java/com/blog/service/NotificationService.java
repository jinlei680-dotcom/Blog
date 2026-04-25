package com.blog.service;

import com.blog.dto.NotificationDTO;
import org.springframework.data.domain.Page;

public interface NotificationService {

    void createCommentNotification(Long senderId, Long articleId, Long commentId);

    void createReplyNotification(Long senderId, Long parentCommentId, Long commentId);

    void createLikeNotification(Long senderId, Long articleId);

    Page<NotificationDTO> listByUser(Long userId, int page, int size);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    long countUnread(Long userId);
}
