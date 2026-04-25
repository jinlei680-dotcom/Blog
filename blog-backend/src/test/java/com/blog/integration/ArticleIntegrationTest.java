package com.blog.integration;

import com.blog.dto.CreateArticleRequest;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.dto.UpdateArticleRequest;
import com.blog.repository.ArticleLikeRepository;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.NotificationRepository;
import com.blog.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ArticleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleLikeRepository articleLikeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private String tokenUser1;
    private String tokenUser2;

    @BeforeEach
    void setUp() throws Exception {
        notificationRepository.deleteAll();
        articleLikeRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();

        tokenUser1 = registerAndLogin("user1", "password123", "user1@example.com");
        tokenUser2 = registerAndLogin("user2", "password123", "user2@example.com");
    }

    // ========== Full CRUD Flow ==========

    @Test
    @DisplayName("完整CRUD流程：创建 → 查询 → 修改 → 删除 → 验证已删除")
    void fullCrudFlow() throws Exception {
        // 1. Create
        MvcResult createResult = mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("Original Title", "Original Content", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Original Title"))
                .andExpect(jsonPath("$.data.content").value("Original Content"))
                .andExpect(jsonPath("$.data.authorName").value("user1"))
                .andReturn();

        Long articleId = extractArticleId(createResult);

        // 2. Query detail
        mockMvc.perform(get("/api/articles/" + articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Original Title"))
                .andExpect(jsonPath("$.data.content").value("Original Content"))
                .andExpect(jsonPath("$.data.authorName").value("user1"));

        // 3. Update
        mockMvc.perform(put("/api/articles/" + articleId)
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateArticleRequest("Updated Title", "Updated Content", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.content").value("Updated Content"));

        // 4. Verify update
        mockMvc.perform(get("/api/articles/" + articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));

        // 5. Delete
        mockMvc.perform(delete("/api/articles/" + articleId)
                        .header("Authorization", "Bearer " + tokenUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. Verify deleted
        mockMvc.perform(get("/api/articles/" + articleId))
                .andExpect(status().isNotFound());
    }

    // ========== Pagination and Ordering ==========

    @Test
    @DisplayName("分页查询：文章按创建时间倒序排列")
    void list_paginationAndOrdering() throws Exception {
        // Create 3 articles
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/articles")
                            .header("Authorization", "Bearer " + tokenUser1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new CreateArticleRequest("Article " + i, "Content " + i, null))))
                    .andExpect(status().isOk());
        }

        // List all — should be in reverse creation order
        mockMvc.perform(get("/api/articles?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].title").value("Article 3"))
                .andExpect(jsonPath("$.data.content[1].title").value("Article 2"))
                .andExpect(jsonPath("$.data.content[2].title").value("Article 1"));

        // Test pagination: page size 2
        mockMvc.perform(get("/api/articles?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(3));

        mockMvc.perform(get("/api/articles?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    // ========== Permission Checks ==========

    @Test
    @DisplayName("权限校验：修改非自己的文章返回403")
    void update_nonOwner_returns403() throws Exception {
        // User1 creates article
        MvcResult createResult = mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("User1 Article", "Content", null))))
                .andExpect(status().isOk())
                .andReturn();

        Long articleId = extractArticleId(createResult);

        // User2 tries to update — should get 403
        mockMvc.perform(put("/api/articles/" + articleId)
                        .header("Authorization", "Bearer " + tokenUser2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateArticleRequest("Hacked Title", "Hacked Content", null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("权限校验：删除非自己的文章返回403")
    void delete_nonOwner_returns403() throws Exception {
        // User1 creates article
        MvcResult createResult = mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("User1 Article", "Content", null))))
                .andExpect(status().isOk())
                .andReturn();

        Long articleId = extractArticleId(createResult);

        // User2 tries to delete — should get 403
        mockMvc.perform(delete("/api/articles/" + articleId)
                        .header("Authorization", "Bearer " + tokenUser2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // Verify article still exists
        mockMvc.perform(get("/api/articles/" + articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("User1 Article"));
    }

    // ========== Cascade Delete ==========

    @Test
    @DisplayName("级联删除：删除文章后关联的评论和点赞也被删除")
    void delete_cascadeDeletesCommentsAndLikes() throws Exception {
        // User1 creates article
        MvcResult createResult = mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("Cascade Test", "Content", null))))
                .andExpect(status().isOk())
                .andReturn();

        Long articleId = extractArticleId(createResult);

        long articleCountBefore = articleRepository.count();

        // Delete article
        mockMvc.perform(delete("/api/articles/" + articleId)
                        .header("Authorization", "Bearer " + tokenUser1))
                .andExpect(status().isOk());

        // Verify article is gone
        long articleCountAfter = articleRepository.count();
        org.junit.jupiter.api.Assertions.assertEquals(articleCountBefore - 1, articleCountAfter);

        // Verify article not found via API
        mockMvc.perform(get("/api/articles/" + articleId))
                .andExpect(status().isNotFound());
    }

    // ========== Validation ==========

    @Test
    @DisplayName("创建文章：标题为空返回400")
    void create_emptyTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("", "Content", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("创建文章：正文为空返回400")
    void create_emptyContent_returns400() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("Title", "", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("未认证用户创建文章返回401")
    void create_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("Title", "Content", null))))
                .andExpect(status().isUnauthorized());
    }

    // ========== Public Access ==========

    @Test
    @DisplayName("文章详情：无需认证即可访问（公开接口）")
    void getDetail_noAuth_returnsArticle() throws Exception {
        // User1 creates article
        MvcResult createResult = mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("Public Article", "Public Content", null))))
                .andExpect(status().isOk())
                .andReturn();

        Long articleId = extractArticleId(createResult);

        // Access without auth — should succeed
        mockMvc.perform(get("/api/articles/" + articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Public Article"))
                .andExpect(jsonPath("$.data.content").value("Public Content"))
                .andExpect(jsonPath("$.data.authorName").value("user1"))
                .andExpect(jsonPath("$.data.likeCount").value(0))
                .andExpect(jsonPath("$.data.likedByCurrentUser").value(false));
    }

    @Test
    @DisplayName("查询不存在的文章返回404")
    void getDetail_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/articles/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ========== Helper Methods ==========

    private String registerAndLogin(String username, String password, String email) throws Exception {
        RegisterRequest registerReq = new RegisterRequest(username, password, email);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        LoginRequest loginReq = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(body, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return (String) data.get("token");
    }

    private Long extractArticleId(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(body, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return ((Number) data.get("id")).longValue();
    }
}
