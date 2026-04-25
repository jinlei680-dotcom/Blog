package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.NotificationDTO;
import com.blog.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<Page<NotificationDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService.listByUser(userId, page, size);
        return ApiResponse.success(notifications);
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(userId, id);
        return ApiResponse.success("标记已读成功", null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("全部标记已读成功", null);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        Long userId = getCurrentUserId();
        long count = notificationService.countUnread(userId);
        return ApiResponse.success(count);
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
