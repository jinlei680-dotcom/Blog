package com.blog.property;

import com.blog.dto.*;
import com.blog.entity.User;
import com.blog.repository.ArticleLikeRepository;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.NotificationRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.spring.JqwikSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature: blog-system
 * 后端评论模块属性测试
 */
@JqwikSpringSupport
@SpringBootTest
@ActiveProfiles("test")
class CommentPropertyTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleLikeRepository articleLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Long testUserId;

    private Long ensureTestUser() {
        if (testUserId != null && userRepository.existsById(testUserId)) {
            return testUserId;
        }
        notificationRepository.deleteAll();
        articleLikeRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username("commentpropuser")
                .password(passwordEncoder.encode("password123"))
                .email("commentprop@example.com")
                .build();
        user = userRepository.save(user);
        testUserId = user.getId();
        return testUserId;
    }

    @AfterProperty
    void tearDown() {
        notificationRepository.deleteAll();
        articleLikeRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();
        testUserId = null;
    }

    // =========================================================================
    // Feature: blog-system, Property 5: 评论树形结构与排序
    // 对于任意文章的评论列表，顶级评论应按创建时间正序排列，每条顶级评论下的
    // 嵌套评论也应按创建时间正序排列，且嵌套评论的 parent_id 应指向其所属的顶级评论。
    // Validates: Requirements 4.4, 4.5
    // =========================================================================

    @Property(tries = 100)
    void commentTreeStructureAndSorting(
            @ForAll @IntRange(min = 1, max = 3) int topLevelCount,
            @ForAll @IntRange(min = 0, max = 2) int repliesPerComment
    ) throws InterruptedException {
        Long userId = ensureTestUser();

        // Clean previous data
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();

        // Create an article
        ArticleDTO article = articleService.create(userId,
                new CreateArticleRequest("PropArticle", "PropContent", null));

        // Create top-level comments with slight delay for ordering
        List<Long> topLevelIds = new ArrayList<>();
        for (int i = 0; i < topLevelCount; i++) {
            if (i > 0) Thread.sleep(1);
            CommentDTO c = commentService.create(userId, article.getId(),
                    new CreateCommentRequest("Top " + i, null));
            topLevelIds.add(c.getId());
        }

        // Create nested replies
        for (Long parentId : topLevelIds) {
            for (int j = 0; j < repliesPerComment; j++) {
                if (j > 0) Thread.sleep(1);
                commentService.create(userId, article.getId(),
                        new CreateCommentRequest("Reply " + j, parentId));
            }
        }

        // Query and verify
        List<CommentTreeDTO> tree = commentService.listByArticle(article.getId());

        // Verify top-level count
        assertThat(tree).hasSize(topLevelCount);

        // Verify top-level sorted by createdAt ASC
        for (int i = 0; i < tree.size() - 1; i++) {
            LocalDateTime current = tree.get(i).getCreatedAt();
            LocalDateTime next = tree.get(i + 1).getCreatedAt();
            assertThat(current).isBeforeOrEqualTo(next);
        }

        // Verify nested comments sorted by createdAt ASC and count
        for (CommentTreeDTO topComment : tree) {
            List<CommentTreeDTO> children = topComment.getChildren();
            assertThat(children).hasSize(repliesPerComment);

            for (int i = 0; i < children.size() - 1; i++) {
                LocalDateTime current = children.get(i).getCreatedAt();
                LocalDateTime next = children.get(i + 1).getCreatedAt();
                assertThat(current).isBeforeOrEqualTo(next);
            }
        }

        // Clean up
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        articleService.delete(userId, article.getId());
    }

    // =========================================================================
    // Feature: blog-system, Property 10: Comment 创建-查询往返
    // 对于任意有效的 Comment 对象（包含评论内容和所属文章），通过 API 创建后
    // 再通过 API 查询，返回的评论内容、评论者和所属文章应与创建时一致。
    // Validates: Requirements 8.9
    // =========================================================================

    @Property(tries = 100)
    void commentCreateQueryRoundTrip(
            @ForAll @StringLength(min = 1, max = 500) @AlphaChars String content
    ) {
        Long userId = ensureTestUser();

        // Clean previous data
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();

        // Create an article
        ArticleDTO article = articleService.create(userId,
                new CreateArticleRequest("RoundTripArticle", "RoundTripContent", null));

        // Create a comment
        CommentDTO created = commentService.create(userId, article.getId(),
                new CreateCommentRequest(content, null));

        // Query the comment tree
        List<CommentTreeDTO> tree = commentService.listByArticle(article.getId());

        assertThat(tree).hasSize(1);
        CommentTreeDTO queried = tree.get(0);

        // Verify round-trip consistency
        assertThat(queried.getContent()).isEqualTo(content.trim());
        assertThat(queried.getUserId()).isEqualTo(userId);
        assertThat(queried.getUsername()).isEqualTo("commentpropuser");

        // Also verify the created DTO
        assertThat(created.getContent()).isEqualTo(content.trim());
        assertThat(created.getUserId()).isEqualTo(userId);
        assertThat(created.getArticleId()).isEqualTo(article.getId());

        // Clean up
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        articleService.delete(userId, article.getId());
    }
}
