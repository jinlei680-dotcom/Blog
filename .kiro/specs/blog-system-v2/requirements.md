# 需求文档 — 博客系统第二期功能扩展

## 简介

本文档描述博客系统第二期扩展功能的需求。在已有博客系统（用户注册登录、文章 CRUD、评论、点赞、音乐播放器）基础上，新增文章标签/分类系统、文章搜索、用户个人主页、Markdown 编辑器、图片上传、角色权限体系和评论/点赞通知七大功能模块。

技术栈沿用一期：前端 Nuxt 3（Vue 3 SSR）+ JavaScript，后端 Spring Boot 2.7.x + Java 8，数据库 MySQL 8。

## 术语表

- **Blog_System**: 博客系统整体，包含前端和后端
- **Frontend**: 基于 Nuxt 3（Vue 3 SSR）的前端应用
- **Backend**: 基于 Spring Boot 2.7.x 的后端服务
- **Database**: MySQL 8 数据库
- **User**: 已注册并登录的用户
- **Article**: 用户发表的博客文章
- **Tag**: 文章标签，用于对文章进行主题分类（如旅行、美食、生活等）
- **Article_Tag**: 文章与标签的多对多关联关系
- **Search_Module**: 文章搜索模块，负责按关键词检索文章
- **Profile_Page**: 用户个人主页，展示某个用户的文章和统计信息
- **Markdown_Editor**: Markdown 编辑器组件，支持 Markdown 格式编写和实时预览
- **Markdown_Renderer**: Markdown 渲染器，将 Markdown 文本转换为 HTML 展示
- **File_Service**: 文件上传服务，负责处理图片上传和存储
- **Role**: 用户角色，区分普通用户（ROLE_USER）和管理员（ROLE_ADMIN）
- **Permission_Guard**: 权限守卫，根据用户角色控制访问权限
- **Notification**: 通知消息，用于告知用户其文章或评论收到了互动
- **Notification_Service**: 通知服务，负责创建、查询和管理通知消息
- **Admin**: 拥有 ROLE_ADMIN 角色的管理员用户

## 需求

### 需求 1：文章标签/分类系统

**用户故事：** 作为一个博客读者，我希望能够按主题标签浏览文章，以便快速找到感兴趣的内容。

#### 验收标准

1. THE Backend SHALL 提供 Tag 的 CRUD 接口，包括创建标签、查询所有标签列表、删除标签
2. WHEN Admin 提交包含标签名称的创建请求, THE Backend SHALL 创建一个新的 Tag 记录并持久化到 Database 中
3. WHEN Admin 提交的标签名称已存在于 Database 中, THE Backend SHALL 返回标签名称已存在的错误信息
4. WHEN Admin 提交的标签名称为空, THE Backend SHALL 返回标签名称不能为空的错误信息
5. WHEN 已登录的 User 创建或修改 Article 时提交标签 ID 列表, THE Backend SHALL 在 Article_Tag 关联表中建立该 Article 与对应 Tag 的多对多关联关系
6. WHEN 已登录的 User 修改 Article 的标签时, THE Backend SHALL 先清除该 Article 的所有旧标签关联，再建立新的标签关联
7. WHEN 任意用户请求某个 Tag 下的文章列表, THE Backend SHALL 返回所有关联该 Tag 的 Article，按创建时间倒序排列并支持分页
8. WHEN 任意用户请求文章详情, THE Backend SHALL 在响应中包含该 Article 关联的所有 Tag 信息
9. THE Frontend SHALL 在首页和文章列表页展示标签筛选栏，用户点击某个 Tag 后筛选展示该标签下的文章
10. THE Frontend SHALL 在文章创建和编辑表单中提供标签选择组件，支持从已有标签中多选
11. THE Frontend SHALL 在文章详情页展示该文章关联的所有标签，每个标签可点击跳转到对应标签的文章列表
12. THE Database SHALL 包含 tag 表（id, name, created_at）和 article_tag 关联表（id, article_id, tag_id），article_tag 表对 article_id 和 tag_id 建立联合唯一约束

### 需求 2：文章搜索

**用户故事：** 作为一个博客读者，我希望能够通过关键词搜索文章，以便快速找到包含特定内容的文章。

#### 验收标准

1. WHEN 用户提交包含关键词的搜索请求, THE Search_Module SHALL 在 Article 的标题和正文中进行模糊匹配，返回匹配的文章列表
2. THE Search_Module SHALL 使用 MySQL LIKE 语句实现关键词模糊匹配
3. WHEN 搜索关键词为空, THE Search_Module SHALL 返回关键词不能为空的错误信息
4. THE Search_Module SHALL 对搜索结果按创建时间倒序排列并支持分页
5. WHEN 搜索关键词未匹配到任何文章, THE Search_Module SHALL 返回空列表和总数为 0 的分页响应
6. THE Backend SHALL 提供搜索接口 `GET /api/articles/search?q=&page=&size=`，接收关键词、页码和每页数量参数
7. THE Frontend SHALL 在导航栏中提供搜索输入框，用户输入关键词并提交后展示搜索结果页
8. THE Frontend SHALL 在搜索结果页展示匹配的文章列表（标题、作者、创建时间、摘要），并支持分页导航
9. WHEN 用户在搜索结果页点击文章标题, THE Frontend SHALL 跳转到该文章的详情页

### 需求 3：用户个人主页

**用户故事：** 作为一个博客读者，我希望能够查看某个用户的个人主页，以便浏览该用户发表的所有文章和了解其活跃度。

#### 验收标准

1. WHEN 任意用户访问某个 User 的个人主页, THE Backend SHALL 返回该 User 的公开信息（用户名、头像 URL、注册时间）
2. WHEN 任意用户访问某个 User 的个人主页, THE Backend SHALL 返回该 User 发表的所有 Article 列表，按创建时间倒序排列并支持分页
3. WHEN 任意用户访问某个 User 的个人主页, THE Backend SHALL 返回该 User 的统计信息（文章总数、获赞总数）
4. THE Backend SHALL 提供用户主页接口 `GET /api/users/{userId}/profile`，返回用户公开信息和统计数据
5. THE Backend SHALL 提供用户文章列表接口 `GET /api/users/{userId}/articles?page=&size=`，返回该用户的分页文章列表
6. WHEN 请求的 User ID 不存在于 Database 中, THE Backend SHALL 返回用户不存在的 404 错误信息
7. THE Frontend SHALL 提供用户个人主页 `pages/users/[id].vue`（SSR），展示用户信息、统计数据和文章列表
8. THE Frontend SHALL 在文章列表和文章详情页的作者名称处添加链接，点击后跳转到该作者的个人主页
9. WHILE User 已登录, WHEN User 访问自己的个人主页, THE Frontend SHALL 展示编辑个人资料的入口

### 需求 4：Markdown 编辑器

**用户故事：** 作为一个博客作者，我希望使用 Markdown 格式编写文章，以便更方便地排版文章内容。

#### 验收标准

1. THE Frontend SHALL 在文章创建和编辑页面集成 Markdown_Editor 组件，替代原有的纯文本输入框
2. THE Markdown_Editor SHALL 提供左右分栏布局：左侧为 Markdown 源码编辑区，右侧为实时预览区
3. THE Markdown_Editor SHALL 支持常用 Markdown 语法：标题（h1-h6）、粗体、斜体、链接、图片、代码块、有序列表、无序列表、引用、分割线、表格
4. THE Markdown_Editor SHALL 提供工具栏按钮，用户点击按钮后在编辑区光标位置插入对应的 Markdown 语法标记
5. THE Markdown_Renderer SHALL 将 Article 的 Markdown 格式正文转换为安全的 HTML 并在文章详情页展示
6. THE Markdown_Renderer SHALL 对渲染输出进行 XSS 过滤，移除潜在的恶意脚本标签和属性
7. THE Backend SHALL 以原始 Markdown 文本格式存储 Article 的正文内容，渲染在前端完成
8. WHEN Markdown 文本包含代码块, THE Markdown_Renderer SHALL 对代码块应用语法高亮
9. FOR ALL 有效的 Markdown 文本, 经过 Markdown_Renderer 渲染后再提取纯文本内容 SHALL 保留原始文本中的所有可见文字内容（渲染往返属性）

### 需求 5：图片上传

**用户故事：** 作为一个博客作者，我希望能够上传图片作为文章配图和个人头像，以便丰富文章内容和个性化个人形象。

#### 验收标准

1. WHEN 已登录的 User 上传图片文件, THE File_Service SHALL 将图片保存到服务器本地文件系统并返回可访问的图片 URL
2. THE File_Service SHALL 仅接受 JPEG、PNG、GIF、WebP 格式的图片文件
3. WHEN 上传的文件格式不在允许范围内, THE File_Service SHALL 返回文件格式不支持的错误信息
4. WHEN 上传的文件大小超过 5MB, THE File_Service SHALL 返回文件大小超出限制的错误信息
5. THE File_Service SHALL 使用 UUID 重命名上传的文件以避免文件名冲突
6. THE Backend SHALL 提供图片上传接口 `POST /api/upload/image`，接收 multipart/form-data 格式的图片文件
7. THE Backend SHALL 提供静态资源访问路径，使上传的图片可通过 URL 直接访问
8. WHEN 已登录的 User 上传头像图片, THE Backend SHALL 更新该 User 的头像 URL 字段
9. THE Backend SHALL 提供头像上传接口 `POST /api/users/avatar`，上传成功后返回新的头像 URL
10. THE Frontend SHALL 在 Markdown_Editor 的工具栏中提供图片上传按钮，上传成功后自动在编辑区插入 Markdown 图片语法 `![alt](url)`
11. THE Frontend SHALL 在用户个人主页和导航栏中展示用户头像，未设置头像时展示默认头像
12. THE Database 的 user 表 SHALL 新增 avatar_url 字段（VARCHAR(500), NULLABLE），用于存储用户头像的 URL

### 需求 6：角色权限体系

**用户故事：** 作为系统管理员，我希望拥有管理所有文章、评论和音乐的权限，以便维护博客内容的质量和秩序。

#### 验收标准

1. THE Database 的 user 表 SHALL 新增 role 字段（VARCHAR(20), NOT NULL, DEFAULT 'ROLE_USER'），取值为 ROLE_USER 或 ROLE_ADMIN
2. WHEN User 注册时, THE Auth_Module SHALL 将该 User 的 role 默认设置为 ROLE_USER
3. THE Backend SHALL 在 JWT Token 中包含用户的 role 信息
4. WHILE User 拥有 ROLE_ADMIN 角色, THE Permission_Guard SHALL 允许该 User 修改和删除任意 Article
5. WHILE User 拥有 ROLE_ADMIN 角色, THE Permission_Guard SHALL 允许该 User 删除任意 Comment
6. WHILE User 拥有 ROLE_ADMIN 角色, THE Permission_Guard SHALL 允许该 User 管理（创建和删除）Music 记录
7. WHILE User 拥有 ROLE_ADMIN 角色, THE Permission_Guard SHALL 允许该 User 创建和删除 Tag
8. WHILE User 拥有 ROLE_USER 角色, THE Permission_Guard SHALL 仅允许该 User 修改和删除自己发表的 Article 和 Comment
9. WHEN 非 Admin 用户尝试执行管理员专属操作, THE Permission_Guard SHALL 返回 403 无权限的错误响应
10. THE Backend SHALL 使用 Spring Security 的 `@PreAuthorize` 注解实现方法级权限控制
11. THE Frontend SHALL 根据当前用户的 role 动态展示或隐藏管理功能按钮（如删除他人文章、删除他人评论、管理标签）
12. THE Frontend SHALL 在导航栏中为 Admin 用户展示管理入口

### 需求 7：评论/点赞通知

**用户故事：** 作为一个博客作者，我希望在有人评论我的文章或回复我的评论时收到通知，以便及时了解互动情况。

#### 验收标准

1. WHEN 某个 User 对一篇 Article 发表 Comment, THE Notification_Service SHALL 为该 Article 的作者创建一条"新评论"类型的 Notification
2. WHEN 某个 User 回复另一个 User 的 Comment, THE Notification_Service SHALL 为被回复的 Comment 的作者创建一条"评论回复"类型的 Notification
3. WHEN 某个 User 对一篇 Article 点赞, THE Notification_Service SHALL 为该 Article 的作者创建一条"新点赞"类型的 Notification
4. THE Notification_Service SHALL 确保不为用户自己的操作创建通知（用户评论自己的文章、回复自己的评论、给自己的文章点赞时不产生通知）
5. THE Backend SHALL 提供获取通知列表接口 `GET /api/notifications?page=&size=`，返回当前登录用户的通知列表，按创建时间倒序排列并支持分页
6. THE Backend SHALL 提供标记通知已读接口 `PUT /api/notifications/{id}/read`，将指定 Notification 标记为已读
7. THE Backend SHALL 提供标记全部已读接口 `PUT /api/notifications/read-all`，将当前用户的所有未读 Notification 标记为已读
8. THE Backend SHALL 提供未读通知计数接口 `GET /api/notifications/unread-count`，返回当前用户的未读通知数量
9. WHEN 已登录的 User 请求通知列表, THE Backend SHALL 返回包含通知类型、触发者信息、关联文章信息、创建时间和已读状态的 Notification 列表
10. THE Frontend SHALL 在导航栏中展示通知图标，并显示未读通知数量的角标
11. THE Frontend SHALL 使用轮询机制（每 30 秒一次）向 Backend 请求未读通知计数，更新角标数字
12. WHEN 用户点击通知图标, THE Frontend SHALL 展示通知下拉列表，显示最近的通知消息
13. WHEN 用户点击某条通知, THE Frontend SHALL 跳转到对应的文章详情页并将该通知标记为已读
14. THE Database SHALL 包含 notification 表（id, recipient_id, sender_id, type, article_id, comment_id, is_read, created_at），recipient_id 和 sender_id 均为 user 表外键，type 取值为 COMMENT、REPLY、LIKE

### 需求 8：测试覆盖（第二期）

**用户故事：** 作为一个开发者，我希望第二期新增的每个功能都有对应的测试，以便确保扩展功能的正确性和稳定性。

#### 验收标准

1. THE Backend SHALL 为标签管理（Tag CRUD、Article_Tag 关联）提供单元测试和集成测试
2. THE Backend SHALL 为文章搜索功能提供单元测试和集成测试，验证关键词匹配、分页和空结果场景
3. THE Backend SHALL 为用户个人主页接口提供单元测试和集成测试，验证用户信息、文章列表和统计数据的正确性
4. THE Backend SHALL 为图片上传功能提供单元测试和集成测试，验证文件格式校验、大小限制和文件存储
5. THE Backend SHALL 为角色权限体系提供单元测试和集成测试，验证 Admin 和普通用户的权限差异
6. THE Backend SHALL 为通知功能提供单元测试和集成测试，验证通知创建、查询、已读标记和计数
7. THE Frontend SHALL 为 Markdown_Editor 组件提供单元测试，验证 Markdown 语法插入和实时预览功能
8. THE Frontend SHALL 为搜索功能提供单元测试，验证搜索输入、结果展示和分页逻辑
9. THE Frontend SHALL 为通知组件提供单元测试，验证通知图标角标、下拉列表和轮询逻辑
10. FOR ALL 有效的 Tag 名称, 通过 API 创建 Tag 后再通过 API 查询标签列表 SHALL 包含该 Tag（标签创建-查询往返属性测试）
11. FOR ALL 有效的搜索关键词（取自已创建文章的标题子串）, 搜索结果 SHALL 包含标题中含有该关键词的文章（搜索一致性属性测试）
12. FOR ALL 有效的 Markdown 文本, 经过 Markdown_Renderer 渲染后的 HTML 中 SHALL 不包含 `<script>` 标签和 `on*` 事件属性（XSS 安全属性测试）
13. FOR ALL 有效的 Notification 创建操作, 通知的 recipient_id SHALL 不等于触发操作的 User 的 ID（自我通知排除属性测试）
