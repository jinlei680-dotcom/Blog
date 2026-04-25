package com.blog.property;

import com.blog.dto.*;
import com.blog.entity.User;
import com.blog.repository.ArticleLikeRepository;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.NotificationRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ArticleService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.spring.JqwikSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature: blog-system
 * 后端文章模块属性测试
 */
@JqwikSpringSupport
@SpringBootTest
@ActiveProfiles("test")
class ArticlePropertyTest {

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
        // Clean existing data
        notificationRepository.deleteAll();
        articleLikeRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username("propuser")
                .password(passwordEncoder.encode("password123"))
                .email("prop@example.com")
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
    // Feature: blog-system, Property 3: 文章详情完整性
    // 对于任意已创建的文章，通过详情接口查询应返回包含标题、正文、作者信息、
    // 创建时间、更新时间和点赞数的完整信息，且作者 ID 与创建者一致。
    // Validates: Requirements 3.4, 3.9
    // =========================================================================

    @Property(tries = 100)
    void articleDetailContainsCompleteInfoWithCorrectAuthor(
            @ForAll @StringLength(min = 1, max = 200) @AlphaChars String title,
            @ForAll @StringLength(min = 1, max = 500) @AlphaChars String content
    ) {
        Long userId = ensureTestUser();

        ArticleDTO created = articleService.create(userId, new CreateArticleRequest(title, content, null));

        ArticleDetailDTO detail = articleService.getDetail(created.getId(), userId);

        assertThat(detail.getId()).isEqualTo(created.getId());
        assertThat(detail.getTitle()).isEqualTo(title.trim());
        assertThat(detail.getContent()).isEqualTo(content.trim());
        assertThat(detail.getAuthorId()).isEqualTo(userId);
        assertThat(detail.getAuthorName()).isNotNull().isNotEmpty();
        assertThat(detail.getCreatedAt()).isNotNull();
        assertThat(detail.getUpdatedAt()).isNotNull();
        assertThat(detail.getLikeCount()).isGreaterThanOrEqualTo(0);

        // Clean up for next iteration
        articleService.delete(userId, created.getId());
    }

    // =========================================================================
    // Feature: blog-system, Property 4: 文章列表排序不变量
    // 对于任意文章列表查询结果，返回的文章应按创建时间严格倒序排列，
    // 即对于结果中任意相邻的两篇文章，前一篇的创建时间不早于后一篇。
    // Validates: Requirements 3.8
    // =========================================================================

    @Property(tries = 100)
    void articleListIsInDescendingCreatedAtOrder(
            @ForAll @IntRange(min = 2, max = 5) int count,
            @ForAll @StringLength(min = 1, max = 50) @AlphaChars String titlePrefix
    ) throws InterruptedException {
        Long userId = ensureTestUser();

        // Clean up articles from previous iterations
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        articleLikeRepository.deleteAll();
        articleRepository.deleteAll();

        List<Long> createdIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                Thread.sleep(1);
            }
            ArticleDTO created = articleService.create(userId,
                    new CreateArticleRequest(titlePrefix + " " + i, "Content " + i, null));
            createdIds.add(created.getId());
        }

        Page<ArticleSummaryDTO> page = articleService.list(0, count + 10);
        List<ArticleSummaryDTO> articles = page.getContent();

        for (int i = 0; i < articles.size() - 1; i++) {
            LocalDateTime current = articles.get(i).getCreatedAt();
            LocalDateTime next = articles.get(i + 1).getCreatedAt();
            assertThat(current).isAfterOrEqualTo(next);
        }

        // Clean up
        for (Long id : createdIds) {
            articleService.delete(userId, id);
        }
    }

    // =========================================================================
    // Feature: blog-system, Property 9: Article 创建-查询往返
    // 对于任意有效的 Article 对象（包含标题和正文），通过 API 创建后再通过
    // API 查询，返回的标题和正文应与创建时提交的一致。
    // Validates: Requirements 8.8
    // =========================================================================

    @Property(tries = 100)
    void articleCreateQueryRoundTrip(
            @ForAll @StringLength(min = 1, max = 200) @AlphaChars String title,
            @ForAll @StringLength(min = 1, max = 500) @AlphaChars String content
    ) {
        Long userId = ensureTestUser();

        ArticleDTO created = articleService.create(userId, new CreateArticleRequest(title, content, null));

        ArticleDetailDTO queried = articleService.getDetail(created.getId(), null);

        assertThat(queried.getTitle()).isEqualTo(title.trim());
        assertThat(queried.getContent()).isEqualTo(content.trim());

        // Clean up
        articleService.delete(userId, created.getId());
    }
}
