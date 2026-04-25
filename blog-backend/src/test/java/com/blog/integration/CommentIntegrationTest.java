package com.blog.integration;

import com.blog.dto.CreateArticleRequest;
import com.blog.dto.CreateCommentRequest;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
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
public class CommentIntegrationTest {

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
    private Long articleId;

    @BeforeEach
    void setUp() throws Exception {
        notificationRepository.deleteAll();
        articleLikeRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();

        tokenUser1 = registerAndLogin("cuser1", "password123", "cuser1@example.com");
        tokenUser2 = registerAndLogin("cuser2", "password123", "cuser2@example.com");

        // Create an article for comments
        MvcResult result = mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateArticleRequest("Comment Test Article", "Content", null))))
                .andExpect(status().isOk())
                .andReturn();
        articleId = extractId(result);
    }

    @Test
    @DisplayName("创建评论成功")
    void createComment_success() throws Exception {
        mockMvc.perform(post("/api/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("Great article!", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("Great article!"))
                .andExpect(jsonPath("$.data.username").value("cuser1"))
                .andExpect(jsonPath("$.data.articleId").value(articleId))
                .andExpect(jsonPath("$.data.parentId").isEmpty());
    }

    @Test
    @DisplayName("创建嵌套评论成功")
    void createNestedComment_success() throws Exception {
        // Create parent comment
        MvcResult parentResult = mockMvc.perform(post("/api/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("Parent comment", null))))
                .andExpect(status().isOk())
                .andReturn();
        Long parentId = extractId(parentResult);

        // Create nested reply
        mockMvc.perform(post("/api/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + tokenUser2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("Reply to parent", parentId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Reply to parent"))
                .andExpect(jsonPath("$.data.username").value("cuser2"))
                .andExpect(jsonPath("$.data.parentId").value(parentId));
    }

    @Test
    @DisplayName("查询评论树形结构")
    void listComments_treeStructure() throws Exception {
        // Create top-level comment
        MvcResult parentResult = mockMvc.perform(post("/api/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("Top level", null))))
                .andExpect(status().isOk())
                .andReturn();
        Long parentId = extractId(parentResult);

        // Create nested reply
        mockMvc.perform(post("/api/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + tokenUser2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("Nested reply", parentId))))
                .andExpect(status().isOk());

        // Query tree
        mockMvc.perform(get("/api/articles/" + articleId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].content").value("Top level"))
                .andExpect(jsonPath("$.data[0].children", hasSize(1)))
                .andExpect(jsonPath("$.data[0].children[0].content").value("Nested reply"));
    }

    @Test
    @DisplayName("删除评论级联删除子评论")
    void deleteComment_cascadeChildren() throws Exception {
        // Create parent
        MvcResult parentResult = mockMvc.perform(post("/api/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("Parent to delete", null))))
                .andExpect(status().isOk())
                .andReturn();
        Long parentId = extractId(parentResult);

        // Create child
        mockMvc.perform(post("/api/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("Child comment", parentId))))
                .andExpect(status().isOk());

        long countBefore = commentRepository.count();

        // Delete parent — should cascade delete child
        mockMvc.perform(delete("/api/comments/" + parentId)
                        .header("Authorization", "Bearer " + tokenUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        long countAfter = commentRepository.count();
        org.junit.jupiter.api.Assertions.assertEquals(countBefore - 2, countAfter);

        // Verify empty tree
        mockMvc.perform(get("/api/articles/" + articleId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("删除非自己的评论返回403")
    void deleteComment_nonOwner_returns403() throws Exception {
        // User1 creates comment
        MvcResult commentResult = mockMvc.perform(post("/api/articles/" + articleId + "/comments")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("User1 comment", null))))
                .andExpect(status().isOk())
                .andReturn();
        Long commentId = extractId(commentResult);

        // User2 tries to delete
        mockMvc.perform(delete("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + tokenUser2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("文章不存在时创建评论返回404")
    void createComment_articleNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/articles/99999/comments")
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCommentRequest("Comment on missing article", null))))
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

    private Long extractId(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(body, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return ((Number) data.get("id")).longValue();
    }
}
