package com.blog.service;

import com.blog.dto.CommentDTO;
import com.blog.dto.CommentTreeDTO;
import com.blog.dto.CreateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentDTO create(Long userId, Long articleId, CreateCommentRequest request);

    void delete(Long userId, Long commentId);

    List<CommentTreeDTO> listByArticle(Long articleId);
}
