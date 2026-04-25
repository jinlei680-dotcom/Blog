package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;
    private String type;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private Long articleId;
    private String articleTitle;
    private Long commentId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
