# 实施计划：博客系统第二期功能扩展

## 概述

在一期已有系统基础上，按功能模块逐步实现七大扩展功能。实施顺序：先完成基础设施变更（数据库实体扩展、JWT 扩展、权限配置），再按依赖关系逐个实现功能模块。角色权限体系最先实现（其他功能依赖权限控制），随后按标签分类 → 搜索 → Markdown 编辑器 → 图片上传 → 用户主页 → 通知系统的顺序推进。每个功能模块内遵循：后端实现 → 后端测试 → 前端实现 → 前端测试 → chrome-devtools 联调验证。

后端：Spring Boot 2.7.18 + Java 8，前端：Nuxt 3 + JavaScript，测试：后端 JUnit 5 + Mockito + jqwik，前端 Vitest + Vue Test Utils + fast-check。

## Tasks

- [x] 1. 基础设施变更
  - [x] 1.1 扩展数据库实体和 Repository
    - 在 User 实体中新增 `role`（VARCHAR(20), NOT NULL, DEFAULT 'ROLE_USER'）和 `avatar_url`（VARCHAR(500), NULLABLE）字段
    - 创建 Tag 实体（id, name, created_at），name 字段 UNIQUE 约束
    - 创建 ArticleTag 实体（id, article_id, tag_id），联合唯一约束（article_id, tag_id）
    - 创建 Notification 实体（id, recipient_id, sender_id, type, article_id, comment_id, is_read, created_at）
    - 创建对应的 Repository 接口：TagRepository、ArticleTagRepository、NotificationRepository
    - 创建新增 DTO：TagDTO、CreateTagRequest、UserProfileDTO、NotificationDTO、UploadResponse
    - 扩展一期 DTO：ArticleDetailDTO/ArticleSummaryDTO 新增 tags 字段、CreateArticleRequest/UpdateArticleRequest 新增 tagIds 字段、LoginResponse/UserDTO 新增 role 和 avatarUrl 字段
    - _需求：1.12, 5.12, 6.1, 7.14_

  - [x] 1.2 扩展 JWT 工具和安全配置
    - 扩展 JwtUtil：`generateToken` 方法新增 role 参数，新增 `getRoleFromToken` 方法
    - 扩展 JwtAuthenticationFilter：解析 Token 中的 role，设置到 SecurityContext 的 GrantedAuthority 中
    - 扩展 SecurityConfig：启用 `@EnableGlobalMethodSecurity(prePostEnabled = true)`，配置新增接口的访问规则（标签管理、图片上传、通知等）
    - 扩展 AuthServiceImpl：注册时默认设置 role 为 ROLE_USER，登录响应中包含 role 信息
    - _需求：6.1, 6.2, 6.3, 6.10_

  - [x] 1.3 配置图片上传和静态资源
    - 在 `application.yml` 中新增 multipart 配置（max-file-size: 5MB, max-request-size: 10MB）和 upload-dir 配置
    - 创建 WebConfig 类实现 WebMvcConfigurer，配置 `/uploads/**` 静态资源映射到本地文件系统
    - _需求：5.6, 5.7_

  - [x] 1.4 扩展前端基础配置
    - 更新 `nuxt.config.ts` 的 routeRules：新增搜索结果页（SSR）、标签文章列表页（SSR）、用户主页（SSR）、文章创建/编辑页（CSR）
    - 扩展 auth store：存储 role 信息
    - 安装 md-editor-v3 依赖：`npm install md-editor-v3`
    - 创建 admin 中间件 `middleware/admin.ts`：检查用户是否为 ROLE_ADMIN
    - _需求：6.11, 6.12_

- [x] 2. 检查点 — 基础设施验收
  - 确保所有实体创建正确、JWT 扩展正常工作、安全配置生效，如有问题请向用户确认。

- [x] 3. 角色权限体系模块
  - [x] 3.1 实现后端角色权限控制
    - 扩展 ArticleController 的 delete 方法：Admin 可删除任意文章（使用 `@PreAuthorize` 或在 Service 层判断角色）
    - 扩展 ArticleServiceImpl 的 update 和 delete 方法：Admin 可修改/删除任意文章，普通用户仅限自己的文章
    - 扩展 CommentController 的 delete 方法：Admin 可删除任意评论
    - 扩展 CommentServiceImpl 的 delete 方法：Admin 可删除任意评论，普通用户仅限自己的评论
    - 确保非 Admin 用户执行管理员专属操作时返回 403 错误
    - _需求：6.4, 6.5, 6.8, 6.9_

  - [ ]* 3.2 编写后端权限单元测试
    - 使用 JUnit 5 + Mockito 测试 ArticleService 和 CommentService 的权限判断逻辑
    - 测试 Admin 用户修改/删除任意文章和评论成功
    - 测试普通用户修改/删除他人文章和评论返回 403
    - _需求：8.5_

  - [ ]* 3.3 编写后端权限属性测试
    - **属性 11：JWT Token 角色往返一致性** — 生成随机用户 ID、用户名和角色（ROLE_USER/ROLE_ADMIN），编码后解码验证一致
    - **验证需求：6.2, 6.3**
    - **属性 12：管理员权限放行** — 生成随机 Admin 用户，验证可成功修改/删除任意文章和评论
    - **验证需求：6.4, 6.5, 6.6, 6.7**
    - **属性 13：普通用户权限拒绝** — 生成随机普通用户，验证越权操作返回 403
    - **验证需求：6.8, 6.9**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.5_

  - [ ]* 3.4 编写后端权限集成测试
    - 使用 Spring Boot Test + H2 测试完整权限流程
    - 测试 Admin 用户：创建标签成功、删除他人文章成功、删除他人评论成功
    - 测试普通用户：创建标签返回 403、删除他人文章返回 403、删除他人评论返回 403
    - 测试普通用户：修改/删除自己的文章和评论成功
    - _需求：8.5_

  - [x] 3.5 实现前端角色权限展示
    - 扩展导航栏：根据用户 role 动态展示/隐藏管理功能入口（管理标签、管理音乐）
    - 在文章列表和详情页：Admin 用户可见所有文章的删除按钮，普通用户仅可见自己文章的编辑/删除按钮
    - 在评论列表：Admin 用户可见所有评论的删除按钮，普通用户仅可见自己评论的删除按钮
    - 更新 auth store 的登录逻辑：解析并存储 role 信息
    - _需求：6.11, 6.12_

  - [ ]* 3.6 编写前端权限组件测试
    - 使用 Vitest + Vue Test Utils 测试导航栏根据 role 展示/隐藏管理入口
    - 测试文章和评论的删除按钮根据 role 和所有权正确展示/隐藏
    - _需求：8.5_

  - [x] 3.7 chrome-devtools 联调验证 — 角色权限
    - 使用 chrome-devtools MCP 验证：Admin 用户登录后导航栏展示管理入口、可删除他人文章和评论
    - 验证普通用户登录后看不到管理入口、无法操作他人内容
    - 检查 API 请求状态码和控制台错误

- [x] 4. 检查点 — 角色权限模块验收
  - 确保所有权限相关测试通过，如有问题请向用户确认。

- [x] 5. 标签分类模块
  - [x] 5.1 实现后端标签 CRUD 和文章-标签关联
    - 实现 TagService 接口和 TagServiceImpl：创建标签（名称非空校验、唯一性校验）、查询所有标签列表、删除标签（级联删除 article_tag 关联）、查询标签下文章列表（按创建时间倒序、分页）
    - 实现 TagController：`POST /api/tags`（仅 Admin，@PreAuthorize）、`GET /api/tags`、`DELETE /api/tags/{id}`（仅 Admin）、`GET /api/tags/{id}/articles?page=&size=`
    - 扩展 ArticleServiceImpl 的 create 和 update 方法：处理 tagIds 参数，创建/更新 ArticleTag 关联（更新时先清除旧关联再建立新关联）
    - 扩展 ArticleServiceImpl 的 getDetail 和 list 方法：在响应中包含文章关联的标签信息
    - _需求：1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.12_

  - [ ]* 5.2 编写后端标签单元测试
    - 使用 JUnit 5 + Mockito 测试 TagService 的创建、查询、删除逻辑
    - 测试标签名称为空、标签名称重复等异常场景
    - 测试 ArticleService 的标签关联创建和更新逻辑
    - _需求：8.1_

  - [ ]* 5.3 编写后端标签属性测试
    - **属性 1：标签创建-查询往返** — 生成随机标签名称，创建后查询验证存在且名称一致
    - **验证需求：1.2, 8.10**
    - **属性 2：标签名称唯一性** — 生成随机标签名称，创建两次验证第二次失败
    - **验证需求：1.3**
    - **属性 3：文章-标签关联反映最新标签列表** — 生成随机标签列表，更新文章标签后验证关联一致
    - **验证需求：1.5, 1.6, 1.8**
    - **属性 4：标签筛选返回正确且有序的文章** — 创建随机文章和标签，按标签筛选验证排序
    - **验证需求：1.7**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.1, 8.10_

  - [ ]* 5.4 编写后端标签集成测试
    - 使用 Spring Boot Test + H2 测试标签完整流程：创建标签 → 创建文章关联标签 → 查询文章详情验证标签 → 按标签筛选文章 → 更新文章标签 → 删除标签
    - 测试标签名称重复返回 409
    - 测试非 Admin 创建标签返回 403
    - _需求：8.1_

  - [x] 5.5 实现前端标签功能
    - 创建 `composables/useTag.js`：封装标签 API 调用（获取标签列表、创建标签、删除标签、获取标签下文章）
    - 创建 `components/TagBar.vue`：首页标签筛选栏，展示所有标签，点击筛选文章
    - 创建 `components/TagSelector.vue`：文章编辑时的标签多选组件
    - 创建 `pages/tags/[id].vue`（SSR）：标签文章列表页，展示该标签下的文章列表（分页）
    - 在首页 `pages/index.vue` 中集成 TagBar 组件
    - 在文章创建/编辑页面中集成 TagSelector 组件
    - 在文章详情页展示文章关联的标签，每个标签可点击跳转到标签文章列表页
    - _需求：1.9, 1.10, 1.11_

  - [ ]* 5.6 编写前端标签组件测试
    - 使用 Vitest + Vue Test Utils 测试 TagBar 的标签渲染和点击筛选逻辑
    - 测试 TagSelector 的多选交互
    - 测试标签文章列表页的数据展示和分页
    - _需求：8.1_

  - [x] 5.7 chrome-devtools 联调验证 — 标签分类
    - 使用 chrome-devtools MCP 验证：首页标签筛选栏正确展示、点击标签筛选文章、标签文章列表页正常渲染
    - 验证文章创建/编辑页面的标签选择组件、文章详情页的标签展示和跳转
    - 检查 API 请求和控制台错误

- [x] 6. 检查点 — 标签分类模块验收
  - 确保所有标签相关测试通过，如有问题请向用户确认。

- [x] 7. 文章搜索模块
  - [x] 7.1 实现后端搜索功能
    - 在 ArticleService 接口中新增 `search(String keyword, int page, int size)` 方法
    - 在 ArticleServiceImpl 中实现搜索：使用 MySQL LIKE 语句在 title 和 content 中模糊匹配，按创建时间倒序排列，支持分页
    - 在 ArticleRepository 中新增自定义查询方法（使用 @Query 或 JPA 方法命名）
    - 在 ArticleController 中新增搜索接口：`GET /api/articles/search?q=&page=&size=`，关键词为空时返回 400
    - _需求：2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ]* 7.2 编写后端搜索单元测试
    - 使用 JUnit 5 + Mockito 测试 ArticleService 的 search 方法
    - 测试关键词匹配、空关键词、无结果等场景
    - _需求：8.2_

  - [ ]* 7.3 编写后端搜索属性测试
    - **属性 5：搜索返回匹配且有序的文章** — 创建随机文章，用标题子串搜索验证结果包含该文章且按创建时间倒序
    - **验证需求：2.1, 2.4, 8.11**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.2, 8.11_

  - [ ]* 7.4 编写后端搜索集成测试
    - 使用 Spring Boot Test + H2 测试搜索完整流程：创建多篇文章 → 搜索关键词 → 验证结果正确 → 验证分页 → 空关键词返回 400 → 无匹配返回空列表
    - _需求：8.2_

  - [x] 7.5 实现前端搜索功能
    - 创建 `composables/useSearch.js`：封装搜索 API 调用
    - 创建 `components/SearchBar.vue`：导航栏搜索输入框，回车或点击搜索后跳转搜索结果页
    - 创建 `pages/search.vue`（SSR）：搜索结果页，展示匹配的文章列表（标题、作者、创建时间、摘要），支持分页导航
    - 在导航栏 `layouts/default.vue` 中集成 SearchBar 组件
    - 搜索结果页中点击文章标题跳转到文章详情页
    - _需求：2.7, 2.8, 2.9_

  - [ ]* 7.6 编写前端搜索组件测试
    - 使用 Vitest + Vue Test Utils 测试 SearchBar 的输入和提交逻辑
    - 测试搜索结果页的文章列表渲染和分页
    - _需求：8.8_

  - [x] 7.7 chrome-devtools 联调验证 — 文章搜索
    - 使用 chrome-devtools MCP 验证：导航栏搜索框正常展示、输入关键词搜索后跳转结果页、结果页正确展示文章列表
    - 验证空搜索和无结果场景的提示信息
    - 检查 API 请求和控制台错误

- [x] 8. 检查点 — 文章搜索模块验收
  - 确保所有搜索相关测试通过，如有问题请向用户确认。

- [x] 9. Markdown 编辑器模块
  - [x] 9.1 实现前端 Markdown 编辑器
    - 创建 `components/MarkdownEditor.vue`：封装 md-editor-v3 组件，使用 `ClientOnly` 包裹（仅客户端渲染）
      - 左右分栏布局：左侧 Markdown 源码编辑区，右侧实时预览区
      - 提供工具栏按钮：标题（h1-h6）、粗体、斜体、链接、图片、代码块、有序/无序列表、引用、分割线、表格
      - 集成图片上传回调（后续图片上传模块实现时对接）
    - 创建 `components/MarkdownRenderer.vue`：使用 md-editor-v3 的 MdPreview 组件渲染 Markdown，配合 DOMPurify 进行 XSS 过滤
    - 创建 `utils/markdown.js`：Markdown 渲染和 XSS 过滤工具函数
    - 创建 `pages/articles/create.vue`（CSR）：文章创建页，集成 MarkdownEditor 和 TagSelector
    - 创建 `pages/articles/edit/[id].vue`（CSR）：文章编辑页，加载已有文章内容到 MarkdownEditor
    - 更新文章详情页 `pages/articles/[id].vue`：使用 MarkdownRenderer 渲染文章正文（替代原有纯文本展示）
    - 后端以原始 Markdown 文本存储文章正文，渲染在前端完成
    - _需求：4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

  - [ ]* 9.2 编写前端 Markdown 组件测试
    - 使用 Vitest + Vue Test Utils 测试 MarkdownEditor 的渲染和工具栏按钮交互
    - 测试 MarkdownRenderer 的 Markdown 渲染输出和代码高亮
    - _需求：8.7_

  - [ ]* 9.3 编写前端 Markdown 属性测试
    - **属性 7：Markdown 渲染 XSS 安全性** — 使用 fast-check 生成包含 XSS payload 的随机 Markdown 文本，验证渲染后 HTML 不含 `<script>` 标签和 `on*` 事件属性
    - **验证需求：4.6, 8.12**
    - **属性 8：Markdown 渲染保留可见文字** — 使用 fast-check 生成随机 Markdown 文本，渲染后提取纯文本，验证原始可见文字均被保留
    - **验证需求：4.9**
    - 每个属性最少 100 次迭代
    - _需求：8.7, 8.12_

  - [x] 9.4 chrome-devtools 联调验证 — Markdown 编辑器
    - 使用 chrome-devtools MCP 验证：文章创建页 Markdown 编辑器正常渲染、左右分栏布局、工具栏按钮可用
    - 验证文章详情页 Markdown 渲染效果（标题、代码块、列表等）
    - 验证文章编辑页加载已有内容到编辑器
    - 检查控制台错误和 API 请求

- [x] 10. 检查点 — Markdown 编辑器模块验收
  - 确保所有 Markdown 相关测试通过，如有问题请向用户确认。

- [x] 11. 图片上传模块
  - [x] 11.1 实现后端图片上传功能
    - 实现 FileService 接口和 FileServiceImpl：
      - `uploadImage`：校验文件格式（JPEG/PNG/GIF/WebP）、校验文件大小（≤5MB）、使用 UUID 重命名文件、保存到 `uploads/images/` 目录、返回可访问的图片 URL
      - `uploadAvatar`：校验格式和大小、保存到 `uploads/avatars/` 目录、更新 User 的 avatar_url 字段、返回新头像 URL
    - 实现 FileController：`POST /api/upload/image`（需认证）、`POST /api/users/avatar`（需认证）
    - 确保上传目录不存在时自动创建
    - _需求：5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.8, 5.9_

  - [ ]* 11.2 编写后端图片上传单元测试
    - 使用 JUnit 5 + Mockito 测试 FileService 的格式校验、大小校验、UUID 命名逻辑
    - 测试不支持的文件格式返回错误
    - 测试超过 5MB 的文件返回错误
    - _需求：8.4_

  - [ ]* 11.3 编写后端图片上传属性测试
    - **属性 9：图片上传格式校验** — 生成随机文件类型，验证 JPEG/PNG/GIF/WebP 允许上传，其他格式拒绝
    - **验证需求：5.2, 5.3**
    - **属性 10：上传文件 UUID 命名** — 上传随机文件，验证存储文件名符合 UUID 格式
    - **验证需求：5.5**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.4_

  - [ ]* 11.4 编写后端图片上传集成测试
    - 使用 Spring Boot Test 测试图片上传完整流程：上传图片 → 验证文件存在 → 验证返回 URL 可访问
    - 测试头像上传：上传头像 → 验证 User 的 avatar_url 更新
    - 测试格式不支持和文件过大的错误场景
    - _需求：8.4_

  - [x] 11.5 实现前端图片上传功能
    - 创建 `composables/useUpload.js`：封装图片上传 API 调用
    - 创建 `components/ImageUploader.vue`：图片上传组件，支持点击选择文件，格式和大小前端预校验，上传后返回图片 URL
    - 在 MarkdownEditor 的工具栏中集成图片上传按钮：上传成功后自动在编辑区光标位置插入 `![alt](url)` 语法
    - 在用户个人主页（后续实现）预留头像上传入口
    - _需求：5.10, 5.11_

  - [ ]* 11.6 编写前端图片上传组件测试
    - 使用 Vitest + Vue Test Utils 测试 ImageUploader 的文件选择、格式校验和上传回调
    - 测试 MarkdownEditor 中图片上传后自动插入 Markdown 图片语法
    - _需求：8.4_

  - [x] 11.7 chrome-devtools 联调验证 — 图片上传
    - 使用 chrome-devtools MCP 验证：Markdown 编辑器中图片上传按钮可用、上传后自动插入图片语法
    - 验证上传的图片在文章详情页正确展示
    - 检查 API 请求（multipart/form-data）和控制台错误

- [x] 12. 检查点 — 图片上传模块验收
  - 确保所有图片上传相关测试通过，如有问题请向用户确认。

- [x] 13. 用户个人主页模块
  - [x] 13.1 实现后端用户主页功能
    - 实现 ProfileService 接口和 ProfileServiceImpl：
      - `getProfile`：查询用户公开信息（用户名、头像 URL、注册时间）和统计数据（文章总数、获赞总数）
      - 用户不存在时抛出 EntityNotFoundException（404）
    - 在 ArticleService 中新增 `listByUser(Long userId, int page, int size)` 方法：查询指定用户的文章列表，按创建时间倒序，支持分页
    - 实现 UserController：`GET /api/users/{userId}/profile`、`GET /api/users/{userId}/articles?page=&size=`
    - _需求：3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ]* 13.2 编写后端用户主页单元测试
    - 使用 JUnit 5 + Mockito 测试 ProfileService 的用户信息和统计数据查询
    - 测试用户不存在返回 404
    - 测试 ArticleService 的 listByUser 方法
    - _需求：8.3_

  - [ ]* 13.3 编写后端用户主页属性测试
    - **属性 6：用户主页统计数据一致性** — 创建随机用户、文章和点赞，验证主页返回的文章总数和获赞总数与实际数据一致
    - **验证需求：3.1, 3.2, 3.3**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.3_

  - [ ]* 13.4 编写后端用户主页集成测试
    - 使用 Spring Boot Test + H2 测试用户主页完整流程：创建用户和文章 → 查询主页 → 验证统计数据 → 查询用户文章列表 → 验证分页和排序
    - 测试用户不存在返回 404
    - _需求：8.3_

  - [x] 13.5 实现前端用户个人主页
    - 创建 `composables/useProfile.js`：封装用户主页 API 调用
    - 创建 `components/UserAvatar.vue`：用户头像展示组件，未设置头像时显示默认头像
    - 创建 `pages/users/[id].vue`（SSR）：用户个人主页，展示用户信息（头像、用户名、注册时间）、统计数据（文章总数、获赞总数）、文章列表（分页）
    - 在文章列表和文章详情页的作者名称处添加链接，点击跳转到作者个人主页
    - 在导航栏和评论区展示用户头像
    - 已登录用户访问自己的主页时展示头像上传入口，集成 ImageUploader 组件上传头像
    - _需求：3.7, 3.8, 3.9, 5.11_

  - [ ]* 13.6 编写前端用户主页组件测试
    - 使用 Vitest + Vue Test Utils 测试 UserAvatar 的头像展示和默认头像逻辑
    - 测试用户主页的数据展示和文章列表分页
    - _需求：8.3_

  - [x] 13.7 chrome-devtools 联调验证 — 用户个人主页
    - 使用 chrome-devtools MCP 验证：用户主页正确展示用户信息、统计数据和文章列表
    - 验证作者名称链接跳转到个人主页
    - 验证头像上传功能（已登录用户访问自己主页时）
    - 检查 SSR 渲染、API 请求和控制台错误

- [x] 14. 检查点 — 用户个人主页模块验收
  - 确保所有用户主页相关测试通过，如有问题请向用户确认。

- [x] 15. 通知系统模块
  - [x] 15.1 实现后端通知功能
    - 实现 NotificationService 接口和 NotificationServiceImpl：
      - `createCommentNotification`：用户评论文章时为文章作者创建"新评论"通知（排除自我通知）
      - `createReplyNotification`：用户回复评论时为被回复评论的作者创建"评论回复"通知（排除自我通知）
      - `createLikeNotification`：用户点赞文章时为文章作者创建"新点赞"通知（排除自我通知）
      - `listByUser`：查询用户通知列表，按创建时间倒序，支持分页
      - `markAsRead`：标记单条通知已读
      - `markAllAsRead`：标记当前用户所有未读通知已读
      - `countUnread`：查询未读通知计数
    - 实现 NotificationController：`GET /api/notifications?page=&size=`、`PUT /api/notifications/{id}/read`、`PUT /api/notifications/read-all`、`GET /api/notifications/unread-count`
    - 在 CommentServiceImpl 的创建评论方法中调用 NotificationService 创建评论/回复通知
    - 在 LikeServiceImpl 的点赞方法中调用 NotificationService 创建点赞通知（仅点赞时创建，取消点赞不创建）
    - _需求：7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.14_

  - [ ]* 15.2 编写后端通知单元测试
    - 使用 JUnit 5 + Mockito 测试 NotificationService 的所有方法
    - 测试评论通知、回复通知、点赞通知的创建逻辑
    - 测试自我通知排除逻辑（用户评论自己文章、回复自己评论、给自己文章点赞不产生通知）
    - 测试通知查询、已读标记和未读计数
    - _需求：8.6_

  - [ ]* 15.3 编写后端通知属性测试
    - **属性 14：互动通知创建** — 生成随机用户 A 和用户 B（A ≠ B），A 对 B 的文章评论/回复/点赞，验证 B 收到正确类型的通知
    - **验证需求：7.1, 7.2, 7.3, 7.9**
    - **属性 15：自我通知排除** — 生成随机用户对自己的文章评论/回复/点赞，验证无通知产生
    - **验证需求：7.4, 8.13**
    - **属性 16：未读通知计数与标记一致性** — 创建随机数量通知，验证未读计数正确，标记全部已读后计数为 0
    - **验证需求：7.7, 7.8**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.6, 8.13_

  - [ ]* 15.4 编写后端通知集成测试
    - 使用 Spring Boot Test + H2 测试通知完整流程：用户 A 评论用户 B 的文章 → 验证 B 收到通知 → 用户 A 回复用户 B 的评论 → 验证 B 收到回复通知 → 用户 A 点赞用户 B 的文章 → 验证 B 收到点赞通知
    - 测试自我操作不产生通知
    - 测试标记已读和全部已读
    - 测试未读计数
    - _需求：8.6_

  - [x] 15.5 实现前端通知功能
    - 创建 Pinia store `stores/notification.js`：管理未读通知计数和通知列表
    - 创建 `composables/useNotification.js`：封装通知 API 调用 + 轮询逻辑（每 30 秒请求未读计数，仅登录时启动，退出时停止）
    - 创建 `components/NotificationBell.vue`：导航栏通知铃铛图标，显示未读数角标
    - 创建 `components/NotificationList.vue`：通知下拉列表，展示最近通知（类型、触发者、文章标题、时间、已读状态）
    - 在导航栏中集成 NotificationBell 组件（仅登录用户可见）
    - 点击通知：跳转到对应文章详情页并标记该通知已读
    - 提供"全部已读"按钮
    - _需求：7.10, 7.11, 7.12, 7.13_

  - [ ]* 15.6 编写前端通知组件测试
    - 使用 Vitest + Vue Test Utils 测试 NotificationBell 的未读数角标展示
    - 测试 NotificationList 的通知列表渲染和点击跳转逻辑
    - 测试 useNotification 的轮询启动/停止逻辑
    - _需求：8.9_

  - [x] 15.7 chrome-devtools 联调验证 — 通知系统
    - 使用 chrome-devtools MCP 验证：登录后导航栏展示通知铃铛、未读数角标正确显示
    - 验证点击铃铛展开通知列表、点击通知跳转到文章详情页
    - 验证评论/点赞后通知正确创建（可通过另一用户登录验证）
    - 检查轮询请求（30 秒间隔）、API 请求和控制台错误

- [x] 16. 检查点 — 通知系统模块验收
  - 确保所有通知相关测试通过，如有问题请向用户确认。

- [x] 17. 全局集成与收尾
  - [x] 17.1 前端错误处理扩展
    - 扩展全局错误拦截：图片上传失败 Toast 提示、搜索无结果提示、通知接口失败静默处理、权限不足（403）Toast 提示
    - Markdown 编辑器加载失败时降级为纯文本输入框
    - 确保所有新增 SSR 页面的错误不会导致整页崩溃
    - _需求：全部需求的错误处理_

  - [x] 17.2 前后端联调与整体验证
    - 验证所有新增 API 端点与前端页面的对接
    - 验证新增 SSR 页面：搜索结果页、标签文章列表页、用户个人主页
    - 验证完整用户流程：登录 → 创建标签（Admin）→ 创建文章（Markdown + 标签 + 图片）→ 搜索文章 → 评论 → 点赞 → 查看通知 → 查看用户主页
    - _需求：全部需求_

  - [x] 17.3 chrome-devtools 最终联调验证
    - 使用 chrome-devtools MCP 对所有新增功能进行端到端验证
    - 验证 Admin 和普通用户的完整操作流程
    - 检查所有页面的 DOM 结构、控制台错误和网络请求

- [x] 18. 最终检查点 — 全部测试通过
  - 运行后端全部测试（单元测试 + 集成测试 + 属性测试），确保全部通过。
  - 运行前端全部测试（组件测试 + 属性测试），确保全部通过。
  - 如有问题请向用户确认。

## 备注

- 标记 `*` 的子任务为可选测试任务，可跳过以加速 MVP 开发
- 每个任务引用了具体的需求编号，确保需求可追溯
- 检查点确保每个功能模块完成后进行增量验证
- 属性测试验证设计文档中定义的 16 个正确性属性
- 单元测试和属性测试互为补充：单元测试验证具体场景和边界条件，属性测试验证通用不变量
- 后端测试使用 H2 内存数据库，不依赖外部 MySQL
- 前端属性测试使用 fast-check 库，后端属性测试使用 jqwik 库
- 每个前端任务完成后必须使用 chrome-devtools MCP 进行联调验证
- 角色权限体系最先实现，因为标签管理、音乐管理等功能依赖 Admin 权限
