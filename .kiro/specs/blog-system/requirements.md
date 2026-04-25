# 需求文档

## 简介

本项目是一个前后端分离的博客系统。前端使用 Vue / JavaScript 构建，后端使用 Spring Boot 框架，数据库采用 MySQL。系统支持用户注册登录、发表文章、评论文章（含楼中楼嵌套评论）、点赞文章以及音乐播放功能。

## 术语表

- **Blog_System**: 博客系统整体，包含前端和后端
- **Frontend**: 基于 Vue / JavaScript 的前端应用
- **Backend**: 基于 Spring Boot 的后端服务
- **Database**: MySQL 数据库，用于持久化存储所有业务数据
- **User**: 已注册并登录的用户
- **Article**: 用户发表的博客文章
- **Comment**: 用户对文章发表的评论
- **Nested_Comment**: 对已有评论的回复评论（楼中楼）
- **Like**: 用户对文章的点赞操作
- **Music_Player**: 博客系统中的音乐播放组件
- **Auth_Module**: 用户认证模块，负责注册、登录和鉴权

## 需求

### 需求 1：用户注册

**用户故事：** 作为一个访客，我希望能够注册账号，以便使用博客系统的完整功能。

#### 验收标准

1. WHEN 访客提交包含用户名、密码和邮箱的注册请求, THE Auth_Module SHALL 创建一个新的 User 记录并将其持久化到 Database 中
2. WHEN 访客提交的用户名已存在于 Database 中, THE Auth_Module SHALL 返回用户名已被占用的错误信息
3. WHEN 访客提交的邮箱已存在于 Database 中, THE Auth_Module SHALL 返回邮箱已被注册的错误信息
4. WHEN 访客提交的密码长度少于 6 个字符, THE Auth_Module SHALL 返回密码长度不足的错误信息
5. WHEN 注册成功, THE Auth_Module SHALL 返回注册成功的响应（包含用户基本信息，不包含密码）
6. THE Auth_Module SHALL 使用加密算法对用户密码进行加密后再存储到 Database 中

### 需求 2：用户登录

**用户故事：** 作为一个已注册用户，我希望能够登录系统，以便管理我的文章和进行互动操作。

#### 验收标准

1. WHEN 用户提交正确的用户名和密码, THE Auth_Module SHALL 验证凭据并返回一个 JWT Token
2. WHEN 用户提交的用户名不存在于 Database 中, THE Auth_Module SHALL 返回用户不存在的错误信息
3. WHEN 用户提交的密码与 Database 中存储的加密密码不匹配, THE Auth_Module SHALL 返回密码错误的错误信息
4. THE Auth_Module SHALL 在 JWT Token 中包含用户 ID 和用户名信息
5. WHILE 用户持有有效的 JWT Token, THE Backend SHALL 允许用户访问需要认证的接口
6. WHEN JWT Token 过期或无效, THE Backend SHALL 返回 401 未授权的错误响应

### 需求 3：发表文章

**用户故事：** 作为一个已登录用户，我希望能够发表文章，以便分享我的想法和内容。

#### 验收标准

1. WHEN 已登录的 User 提交包含标题和正文的文章创建请求, THE Backend SHALL 创建一个新的 Article 记录并持久化到 Database 中
2. WHEN 已登录的 User 提交的文章标题为空, THE Backend SHALL 返回标题不能为空的错误信息
3. WHEN 已登录的 User 提交的文章正文为空, THE Backend SHALL 返回正文不能为空的错误信息
4. THE Backend SHALL 在 Article 记录中自动记录作者 ID、创建时间和更新时间
5. WHEN 已登录的 User 请求修改自己发表的 Article, THE Backend SHALL 更新该 Article 的标题、正文和更新时间
6. WHEN 已登录的 User 请求删除自己发表的 Article, THE Backend SHALL 从 Database 中删除该 Article 及其关联的 Comment 和 Like 记录
7. WHEN 用户请求修改或删除非自己发表的 Article, THE Backend SHALL 返回 403 无权限的错误响应
8. WHEN 任意用户请求文章列表, THE Backend SHALL 返回按创建时间倒序排列的分页 Article 列表
9. WHEN 任意用户请求单篇文章详情, THE Backend SHALL 返回该 Article 的完整信息（包含标题、正文、作者信息、创建时间、更新时间和点赞数）

### 需求 4：评论文章

**用户故事：** 作为一个已登录用户，我希望能够评论文章，以便与作者和其他读者交流。

#### 验收标准

1. WHEN 已登录的 User 提交包含评论内容的评论请求, THE Backend SHALL 创建一个新的 Comment 记录并关联到对应的 Article
2. WHEN 已登录的 User 提交的评论内容为空, THE Backend SHALL 返回评论内容不能为空的错误信息
3. WHEN 已登录的 User 对一条已有的 Comment 进行回复, THE Backend SHALL 创建一个 Nested_Comment 记录并关联到父级 Comment
4. THE Backend SHALL 在 Comment 记录中自动记录评论者 ID、所属文章 ID、父评论 ID（如果是嵌套评论）和创建时间
5. WHEN 任意用户请求某篇文章的评论列表, THE Backend SHALL 返回该文章的所有顶级 Comment 及其嵌套的 Nested_Comment，按创建时间正序排列
6. WHEN 已登录的 User 请求删除自己发表的 Comment, THE Backend SHALL 从 Database 中删除该 Comment 及其所有子级 Nested_Comment
7. WHEN 用户请求删除非自己发表的 Comment, THE Backend SHALL 返回 403 无权限的错误响应
8. WHEN 评论所属的 Article 不存在于 Database 中, THE Backend SHALL 返回文章不存在的错误信息

### 需求 5：点赞文章

**用户故事：** 作为一个已登录用户，我希望能够点赞文章，以便表达我对文章的喜爱。

#### 验收标准

1. WHEN 已登录的 User 对一篇 Article 发起点赞请求, THE Backend SHALL 创建一个 Like 记录并关联到该 User 和 Article
2. WHEN 已登录的 User 对已经点赞过的 Article 再次发起点赞请求, THE Backend SHALL 取消该点赞（删除 Like 记录）
3. THE Backend SHALL 确保同一 User 对同一 Article 在 Database 中最多存在一条 Like 记录
4. WHEN 任意用户请求某篇文章的点赞数, THE Backend SHALL 返回该 Article 的总点赞数量
5. WHILE User 已登录, WHEN User 查看文章详情, THE Backend SHALL 返回该 User 是否已对该 Article 点赞的状态
6. WHEN 点赞的目标 Article 不存在于 Database 中, THE Backend SHALL 返回文章不存在的错误信息

### 需求 6：音乐播放器

**用户故事：** 作为一个博客访客，我希望在浏览博客时能够播放背景音乐，以便获得更好的阅读体验。

#### 验收标准

1. THE Frontend SHALL 在页面中展示一个 Music_Player 组件
2. WHEN 用户点击播放按钮, THE Music_Player SHALL 开始播放当前选中的音乐曲目
3. WHEN 用户点击暂停按钮, THE Music_Player SHALL 暂停当前正在播放的音乐曲目
4. WHEN 用户点击下一曲按钮, THE Music_Player SHALL 切换到播放列表中的下一首曲目并开始播放
5. WHEN 用户点击上一曲按钮, THE Music_Player SHALL 切换到播放列表中的上一首曲目并开始播放
6. WHEN 当前曲目播放结束, THE Music_Player SHALL 自动切换到播放列表中的下一首曲目并开始播放
7. WHEN 播放列表中最后一首曲目播放结束, THE Music_Player SHALL 从播放列表的第一首曲目重新开始播放
8. THE Music_Player SHALL 展示当前播放曲目的名称和播放进度
9. WHEN 管理员通过后台接口上传音乐文件, THE Backend SHALL 将音乐文件信息（名称、文件路径）持久化到 Database 中
10. WHEN Frontend 请求音乐播放列表, THE Backend SHALL 返回 Database 中所有可用的音乐曲目列表

### 需求 7：前端路由与页面

**用户故事：** 作为一个博客访客，我希望能够通过清晰的页面导航浏览博客内容。

#### 验收标准

1. THE Frontend SHALL 提供首页、文章详情页、登录页和注册页
2. WHEN 未登录的用户尝试访问需要认证的操作（发表文章、评论、点赞）, THE Frontend SHALL 引导用户跳转到登录页
3. WHEN 用户成功登录, THE Frontend SHALL 将 JWT Token 存储在本地并在后续请求中携带该 Token
4. WHEN 用户点击退出登录, THE Frontend SHALL 清除本地存储的 JWT Token 并跳转到首页
5. THE Frontend SHALL 在首页以分页列表形式展示所有文章的标题、作者和创建时间
6. WHEN 用户点击文章标题, THE Frontend SHALL 跳转到该文章的详情页，展示文章完整内容、评论列表和点赞信息

### 需求 8：测试覆盖

**用户故事：** 作为一个开发者，我希望每个功能都有对应的测试，以便确保系统的正确性和稳定性。

#### 验收标准

1. THE Backend SHALL 为每个 REST API 端点提供对应的单元测试
2. THE Backend SHALL 为用户注册和登录功能提供集成测试，验证完整的认证流程
3. THE Backend SHALL 为文章的创建、修改、删除和查询功能提供集成测试
4. THE Backend SHALL 为评论和嵌套评论功能提供集成测试，验证评论的创建、嵌套和删除
5. THE Backend SHALL 为点赞和取消点赞功能提供集成测试
6. THE Frontend SHALL 为 Music_Player 组件提供单元测试，验证播放、暂停、切换曲目功能
7. THE Frontend SHALL 为用户登录和注册表单提供单元测试，验证表单校验和提交逻辑
8. FOR ALL 有效的 Article 对象, 通过 API 创建后再通过 API 查询 SHALL 返回等价的 Article 对象（往返属性测试）
9. FOR ALL 有效的 Comment 对象, 通过 API 创建后再通过 API 查询 SHALL 返回等价的 Comment 对象（往返属性测试）
