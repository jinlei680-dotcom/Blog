package com.blog.service.impl;

import com.blog.dto.*;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArticleLikeRepository articleLikeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ArticleTagRepository articleTagRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private User author;
    private Article article;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        article = Article.builder()
                .id(10L)
                .title("Test Title")
                .content("Test Content")
                .author(author)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ========== create ==========

    @Test
    void create_success() {
        CreateArticleRequest request = new CreateArticleRequest("My Title", "My Content", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> {
            Article a = inv.getArgument(0);
            a.setId(10L);
            a.setCreatedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            return a;
        });

        ArticleDTO result = articleService.create(1L, request);

        assertNotNull(result);
        assertEquals("My Title", result.getTitle());
        assertEquals("My Content", result.getContent());
        assertEquals(1L, result.getAuthorId());
        assertEquals("testuser", result.getAuthorName());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void create_emptyTitle_throwsIllegalArgument() {
        CreateArticleRequest request = new CreateArticleRequest("", "content", null);
        assertThrows(IllegalArgumentException.class, () -> articleService.create(1L, request));
        verify(articleRepository, never()).save(any());
    }

    @Test
    void create_nullTitle_throwsIllegalArgument() {
        CreateArticleRequest request = new CreateArticleRequest(null, "content", null);
        assertThrows(IllegalArgumentException.class, () -> articleService.create(1L, request));
    }

    @Test
    void create_emptyContent_throwsIllegalArgument() {
        CreateArticleRequest request = new CreateArticleRequest("title", "", null);
        assertThrows(IllegalArgumentException.class, () -> articleService.create(1L, request));
    }

    @Test
    void create_nullContent_throwsIllegalArgument() {
        CreateArticleRequest request = new CreateArticleRequest("title", null, null);
        assertThrows(IllegalArgumentException.class, () -> articleService.create(1L, request));
    }

    @Test
    void create_userNotFound_throwsEntityNotFound() {
        CreateArticleRequest request = new CreateArticleRequest("title", "content", null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> articleService.create(999L, request));
        verify(articleRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    void update_success() {
        UpdateArticleRequest request = new UpdateArticleRequest("Updated Title", "Updated Content", null);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        ArticleDTO result = articleService.update(1L, 10L, request);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
    }

    @Test
    void update_nonOwner_throwsAccessDenied() {
        UpdateArticleRequest request = new UpdateArticleRequest("Title", "Content", null);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));

        assertThrows(AccessDeniedException.class, () -> articleService.update(999L, 10L, request));
        verify(articleRepository, never()).save(any());
    }

    @Test
    void update_articleNotFound_throwsEntityNotFound() {
        UpdateArticleRequest request = new UpdateArticleRequest("Title", "Content", null);
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> articleService.update(1L, 99L, request));
    }

    @Test
    void update_emptyTitle_throwsIllegalArgument() {
        UpdateArticleRequest request = new UpdateArticleRequest("  ", "Content", null);
        assertThrows(IllegalArgumentException.class, () -> articleService.update(1L, 10L, request));
    }

    @Test
    void update_emptyContent_throwsIllegalArgument() {
        UpdateArticleRequest request = new UpdateArticleRequest("Title", "  ", null);
        assertThrows(IllegalArgumentException.class, () -> articleService.update(1L, 10L, request));
    }

    // ========== delete ==========

    @Test
    void delete_success() {
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));

        articleService.delete(1L, 10L);

        verify(commentRepository).deleteByArticleId(10L);
        verify(articleLikeRepository).deleteByArticleId(10L);
        verify(articleTagRepository).deleteByArticleId(10L);
        verify(articleRepository).delete(article);
    }

    @Test
    void delete_articleNotFound_throwsEntityNotFound() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> articleService.delete(1L, 99L));
        verify(articleRepository, never()).delete(any());
    }

    @Test
    void delete_nonOwner_throwsAccessDenied() {
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));

        assertThrows(AccessDeniedException.class, () -> articleService.delete(999L, 10L));
        verify(articleRepository, never()).delete(any());
    }

    @Test
    void delete_cascadeDeletesCommentsAndLikes() {
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));

        articleService.delete(1L, 10L);

        // Verify cascade: comments, likes, and tags deleted before article
        org.mockito.InOrder inOrder = inOrder(commentRepository, articleLikeRepository, articleTagRepository, articleRepository);
        inOrder.verify(commentRepository).deleteByArticleId(10L);
        inOrder.verify(articleLikeRepository).deleteByArticleId(10L);
        inOrder.verify(articleTagRepository).deleteByArticleId(10L);
        inOrder.verify(articleRepository).delete(article);
    }

    // ========== list ==========

    @Test
    void list_returnsPaginatedResults() {
        Article a1 = Article.builder().id(1L).title("A1").content("C1").author(author)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        Article a2 = Article.builder().id(2L).title("A2").content("C2").author(author)
                .createdAt(LocalDateTime.now().minusHours(1)).updatedAt(LocalDateTime.now()).build();

        Page<Article> page = new PageImpl<>(Arrays.asList(a1, a2), PageRequest.of(0, 10), 2);
        when(articleRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(page);
        when(articleLikeRepository.countByArticleId(anyLong())).thenReturn(0L);
        when(articleTagRepository.findByArticleId(anyLong())).thenReturn(Collections.emptyList());

        Page<ArticleSummaryDTO> result = articleService.list(0, 10);

        assertEquals(2, result.getContent().size());
        assertEquals("A1", result.getContent().get(0).getTitle());
        assertEquals("A2", result.getContent().get(1).getTitle());
    }

    // ========== getDetail ==========

    @Test
    void getDetail_withCurrentUser() {
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));
        when(articleLikeRepository.countByArticleId(10L)).thenReturn(5L);
        when(articleLikeRepository.existsByUserIdAndArticleId(1L, 10L)).thenReturn(true);
        when(articleTagRepository.findByArticleId(10L)).thenReturn(Collections.emptyList());

        ArticleDetailDTO result = articleService.getDetail(10L, 1L);

        assertEquals(10L, result.getId());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals(1L, result.getAuthorId());
        assertEquals("testuser", result.getAuthorName());
        assertEquals(5L, result.getLikeCount());
        assertTrue(result.isLikedByCurrentUser());
    }

    @Test
    void getDetail_withoutCurrentUser() {
        when(articleRepository.findById(10L)).thenReturn(Optional.of(article));
        when(articleLikeRepository.countByArticleId(10L)).thenReturn(3L);
        when(articleTagRepository.findByArticleId(10L)).thenReturn(Collections.emptyList());

        ArticleDetailDTO result = articleService.getDetail(10L, null);

        assertEquals(3L, result.getLikeCount());
        assertFalse(result.isLikedByCurrentUser());
        verify(articleLikeRepository, never()).existsByUserIdAndArticleId(any(), any());
    }

    @Test
    void getDetail_articleNotFound_throwsEntityNotFound() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> articleService.getDetail(99L, null));
    }
}
