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
import com.blog.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Article article;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        article = Article.builder()
                .id(10L)
                .title("Test Article")
                .content("Test Content")
                .author(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        comment = Comment.builder()
                .id(100L)
                .content("Test Comment")
                .user(user)
                .article(article)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========== create ==========

    @Test
    void create_success() {
        CreateCommentRequest request = new CreateCommentRequest("Nice article!", null);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(100L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });

        CommentDTO result = commentService.create(1L, 10L, request);

        assertNotNull(result);
        assertEquals("Nice article!", result.getContent());
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals(10L, result.getArticleId());
        assertNull(result.getParentId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void create_emptyContent_throwsIllegalArgument() {
        CreateCommentRequest request = new CreateCommentRequest("", null);
        assertThrows(IllegalArgumentException.class, () -> commentService.create(1L, 10L, request));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_nullContent_throwsIllegalArgument() {
        CreateCommentRequest request = new CreateCommentRequest(null, null);
        assertThrows(IllegalArgumentException.class, () -> commentService.create(1L, 10L, request));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_articleNotFound_throwsEntityNotFound() {
        CreateCommentRequest request = new CreateCommentRequest("content", null);
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> commentService.create(1L, 999L, request));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_parentNotFound_throwsEntityNotFound() {
        CreateCommentRequest request = new CreateCommentRequest("reply content", 999L);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> commentService.create(1L, 10L, request));
        verify(commentRepository, never()).save(any());
    }

    // ========== delete ==========

    @Test
    void delete_success() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        commentService.delete(1L, 100L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_nonOwner_throwsAccessDenied() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.delete(999L, 100L));
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void delete_notFound_throwsEntityNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> commentService.delete(1L, 999L));
        verify(commentRepository, never()).delete(any());
    }

    // ========== listByArticle ==========

    @Test
    void listByArticle_success() {
        Comment c1 = Comment.builder()
                .id(1L).content("First").user(user).article(article)
                .createdAt(LocalDateTime.now().minusHours(2))
                .children(Collections.emptyList())
                .build();
        Comment c2 = Comment.builder()
                .id(2L).content("Second").user(user).article(article)
                .createdAt(LocalDateTime.now().minusHours(1))
                .children(Collections.emptyList())
                .build();

        when(articleRepository.existsById(10L)).thenReturn(true);
        when(commentRepository.findByArticleIdAndParentIsNullOrderByCreatedAtAsc(10L))
                .thenReturn(Arrays.asList(c1, c2));

        java.util.List<CommentTreeDTO> result = commentService.listByArticle(10L);

        assertEquals(2, result.size());
        assertEquals("First", result.get(0).getContent());
        assertEquals("Second", result.get(1).getContent());
    }

    @Test
    void listByArticle_articleNotFound_throwsEntityNotFound() {
        when(articleRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> commentService.listByArticle(999L));
    }
}
