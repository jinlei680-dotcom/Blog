package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentTreeDTO;
import com.blog.dto.CreateCommentRequest;
import com.blog.service.CommentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/api/articles/{articleId}/comments")
    public ApiResponse<CommentDTO> create(@PathVariable Long articleId,
                                          @RequestBody CreateCommentRequest request) {
        Long userId = getCurrentUserId();
        CommentDTO comment = commentService.create(userId, articleId, request);
        return ApiResponse.success("评论创建成功", comment);
    }

    @GetMapping("/api/articles/{articleId}/comments")
    public ApiResponse<List<CommentTreeDTO>> list(@PathVariable Long articleId) {
        List<CommentTreeDTO> comments = commentService.listByArticle(articleId);
        return ApiResponse.success(comments);
    }

    @DeleteMapping("/api/comments/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        commentService.delete(userId, id);
        return ApiResponse.success("评论删除成功", null);
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
