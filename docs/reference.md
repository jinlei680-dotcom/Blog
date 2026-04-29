# Project Reference

## Frontend: Pages & Routes

| Page | Route | SSR | Middleware |
|------|-------|-----|------------|
| `index.vue` | `/` | yes | — |
| `login.vue` | `/login` | no | — |
| `register.vue` | `/register` | no | — |
| `search.vue` | `/search?q=&page=` | yes | — |
| `articles/create.vue` | `/articles/create` | no | auth |
| `articles/[id]/index.vue` | `/articles/:id` | yes | — |
| `articles/[id]/edit.vue` | `/articles/:id/edit` | no | auth |
| `tags/[id].vue` | `/tags/:id` | yes | — |
| `users/[id].vue` | `/users/:id` | yes | — |

## Frontend: Composables

| File | Exports | API Calls |
|------|---------|-----------|
| `useArticle.js` | fetchArticles, fetchArticle, createArticle, updateArticle, deleteArticle | GET/POST/PUT/DELETE `/api/articles` |
| `useAuth.js` | login, register, logout | POST `/api/auth/login`, `/api/auth/register` |
| `useBookmark.js` | getBookmarks, isBookmarked, toggleBookmark, removeBookmark, syncState | localStorage only |
| `useComment.js` | fetchComments, createComment, deleteComment | GET/POST/DELETE `/api/articles/:id/comments` |
| `useDraft.js` | saveDraft, loadDraft, clearDraft, hasDraft | localStorage only |
| `useLike.js` | toggleLike | POST `/api/articles/:id/like` |
| `useMusic.js` | fetchMusicList | GET `/api/music` |
| `useNotification.js` | fetchUnreadCount, startPolling(30s), fetchNotifications, markAsRead, markAllAsRead | GET/PUT `/api/notifications` |
| `useProfile.js` | fetchProfile, fetchUserArticles, updateBio | GET/PUT `/api/users` |
| `useSearch.js` | searchArticles | GET `/api/articles/search?q=` |
| `useTag.js` | fetchTags, createTag, deleteTag, fetchArticlesByTag | GET/POST/DELETE `/api/tags` |
| `useUpload.js` | uploadImage, uploadAvatar | POST `/api/upload/image`, `/api/users/avatar` |

## Frontend: Components

| Component | Purpose |
|-----------|---------|
| `MarkdownEditor.vue` | Wraps md-editor-v3, client-only with SSR textarea fallback, image upload handler |
| `MarkdownRenderer.vue` | Wraps MdPreview + DOMPurify sanitization, client-only |
| `CommentForm.vue` | Textarea + char counter (max 1000), login prompt if not authenticated, supports reply mode |
| `CommentList.vue` | Recursive threaded comment tree with TransitionGroup animations |
| `ArticleToc.vue` | Desktop sticky sidebar (>=1200px), IntersectionObserver active heading tracking |
| `MusicPlayer.vue` | Fixed bottom bar, playlist fetch, circular playback with prev/next/play-pause |
| `NotificationBell.vue` | Bell icon + unread count badge, toggle NotificationList dropdown |
| `NotificationList.vue` | Notification items with type label, relative time, unread dot, mark-all-read |
| `TagSelector.vue` | Multi-select tag toggles for article editor |
| `TagBar.vue` | Horizontal scrollable pill list of all tags |
| `SearchBar.vue` | Navbar pill input, navigates to /search on Enter |
| `ImageUploader.vue` | Upload button, validates file type and size (5MB) |
| `UserAvatar.vue` | Image or initial-letter fallback, configurable size |

## Frontend: Other Modules

| Category | Files | Purpose |
|----------|-------|---------|
| Stores | `auth.js`, `notification.js` | Auth (token/user/role, localStorage), Notification (unreadCount) |
| Middleware | `auth.js`, `admin.js` | Require login, require admin role |
| Plugins | `api.client.js`, `auth.client.js` | Global 401 interceptor, session restore on startup |
| Utils | `readingTime.js`, `markdown.js`, `playerNavigation.js` | Word count/reading time, DOMPurify wrapper, circular playlist index |
| Layouts | `default.vue` | App shell: glass navbar, orb backgrounds, footer, MusicPlayer |

---

## Backend: Controllers

| Controller | Base Path | Methods |
|------------|-----------|---------|
| `ArticleController` | `/api/articles` | GET list/detail/search/featured/stats, POST create, PUT update, DELETE |
| `AuthController` | `/api/auth` | POST register, POST login |
| `CommentController` | `/api/articles/{id}/comments` | GET list(tree), POST create, DELETE |
| `FileController` | `/api/upload/image`, `/api/users/avatar` | POST upload |
| `LikeController` | `/api/articles/{id}/like` | POST toggle |
| `MusicController` | `/api/music` | GET list, POST create |
| `NotificationController` | `/api/notifications` | GET list, GET unread-count, PUT mark-read, PUT mark-all-read |
| `TagController` | `/api/tags` | GET list, POST create(admin), DELETE(admin), GET articles-by-tag |
| `UserController` | `/api/users` | GET profile, GET user-articles, PUT bio |

## Backend: Services

| Interface | Implementation | Key Logic |
|-----------|---------------|-----------|
| `ArticleService` | `ArticleServiceImpl` | CRUD with tag associations, paginated listing, keyword LIKE search, site stats |
| `AuthService` | `AuthServiceImpl` | Register (BCrypt + duplicate check), login (verify + JWT issue) |
| `CommentService` | `CommentServiceImpl` | Create with optional parent, cascading delete, notification trigger |
| `FileService` | `FileServiceImpl` | Type/size validation, UUID naming, file system storage |
| `LikeService` | `LikeServiceImpl` | Toggle like/unlike, notification on like (skips self) |
| `MusicService` | `MusicServiceImpl` | List all, create with validation |
| `NotificationService` | `NotificationServiceImpl` | Create COMMENT/REPLY/LIKE, skip self-notifications, paginated list |
| `ProfileService` | `ProfileServiceImpl` | User profile with article count + total likes |
| `TagService` | `TagServiceImpl` | CRUD with duplicate check, articles by tag |

## Backend: Entities & DB Tables

| Entity | Table | Columns (key only) |
|--------|-------|--------------------|
| `User` | `user` | id, username(U), password, email(U), role, avatar_url, bio, created_at |
| `Article` | `article` | id, title, content(TEXT), author_id(FK), featured, created_at, updated_at |
| `Tag` | `tag` | id, name(U), created_at |
| `ArticleTag` | `article_tag` | id, article_id+tag_id(U composite), article(FK), tag(FK) |
| `ArticleLike` | `article_like` | id, user_id+article_id(U composite), created_at |
| `Comment` | `comment` | id, content, user_id(FK), article_id(FK), parent_id(FK→self), created_at |
| `Notification` | `notification` | id, recipient_id(FK), sender_id(FK), type, article_id(FK?), comment_id(FK?), is_read |
| `Music` | `music` | id, name, file_path |

## Backend: DTOs

| Type | Files |
|------|-------|
| Request | CreateArticleRequest, UpdateArticleRequest, CreateCommentRequest, CreateMusicRequest, CreateTagRequest, LoginRequest, RegisterRequest |
| Response | ArticleDTO, ArticleDetailDTO, ArticleSummaryDTO, CommentDTO, CommentTreeDTO, LikeResponse, LoginResponse, MusicDTO, NotificationDTO, SiteStatsDTO, TagDTO, UploadResponse, UserDTO, UserProfileDTO |

## Backend: Security & Config

| File | Role |
|------|------|
| `SecurityConfig.java` | Stateless sessions, CORS, public endpoints, JWT filter registration |
| `JwtAuthenticationFilter.java` | OncePerRequestFilter: Bearer extract → validate → SecurityContext |
| `JwtUtil.java` | HS512 JWT: generate (userId+username+role, 24h), parse, validate |
| `WebConfig.java` | Static resource mapping `/uploads/**` → `file:./uploads/` |
| `ApiResponse.java` | Generic `{code, message, data}` — `success()`, `error()` factories |
| `GlobalExceptionHandler.java` | Maps exceptions to HTTP codes: 400/401/403/404/409/413/500 |

## Backend: Tests

| File | Type | What It Covers |
|------|------|----------------|
| `integration/ArticleIntegrationTest.java` | MockMvc Integration | CRUD, pagination, permissions, validation |
| `integration/AuthIntegrationTest.java` | MockMvc Integration | Register, login, duplicate checks, token validation |
| `integration/CommentIntegrationTest.java` | MockMvc Integration | Create, nested replies, tree structure, cascade |
| `property/ArticlePropertyTest.java` | jqwik Property | Detail completeness, list ordering, round-trip |
| `property/AuthPropertyTest.java` | jqwik Property | BCrypt verification, JWT claim round-trip |
| `property/CommentPropertyTest.java` | jqwik Property | Tree structure, sort order, round-trip |
| `service/impl/ArticleServiceImplTest.java` | Mockito Unit | Create/update/delete/list/detail |
| `service/impl/AuthServiceImplTest.java` | Mockito Unit | Register validation, login success/failure |
| `service/impl/CommentServiceImplTest.java` | Mockito Unit | Create/validation/delete permission/article-not-found |
| `security/JwtUtilTest.java` | JUnit Unit | Token generation, parsing, expiry, invalid tokens |
| `BlogBackendApplicationTests.java` | Smoke | Context loads |
