# 实施计划：博客系统

## 概述

按功能模块组织实施，每个模块内遵循"后端实现 → 后端测试 → 前端实现 → 前端测试"的顺序。先搭建项目基础架构，再逐个实现用户认证、文章管理、评论系统、点赞功能和音乐播放器。后端使用 Spring Boot 2.7.x（Java 8），前端使用 Nuxt 3（JavaScript，SSR 模式），数据库使用 MySQL 8（测试使用 H2）。

## Tasks

- [x] 1. 搭建项目基础架构
  - [x] 1.1 初始化 Spring Boot 后端项目
    - 创建 Spring Boot 项目，配置 `pom.xml` 依赖（Spring Web、Spring Data JPA、Spring Security、MySQL Connector、H2、jjwt、jqwik、Lombok）
    - 配置 `application.yml`（数据源、JPA、JWT 密钥等）和 `application-test.yml`（H2 内存数据库）
    - 创建全局异常处理器 `GlobalExceptionHandler`（`@RestControllerAdvice`），统一错误响应格式 `{code, message, timestamp}`
    - 创建通用响应类 `ApiResponse`
    - _需求：全部需求的后端基础_

  - [x] 1.2 初始化 Nuxt 3 前端项目
    - 使用 `npx nuxi init` 创建 Nuxt 3 项目
    - 配置 `nuxt.config.ts`：SSR 模式、`routeRules`（首页和文章详情 SSR，登录注册 CSR）、API 代理配置
    - 安装依赖：`@pinia/nuxt`（状态管理）、`vitest`、`@nuxt/test-utils`、`@vue/test-utils`、`fast-check`
    - 创建基础目录结构：`pages/`、`components/`、`composables/`、`stores/`、`middleware/`、`plugins/`、`utils/`
    - 创建基础布局 `layouts/default.vue`（包含导航栏和 MusicPlayer 插槽）
    - _需求：7.1_

  - [x] 1.3 创建数据库实体和 Repository
    - 创建 JPA 实体：`User`、`Article`、`Comment`（自关联 parent_id）、`ArticleLike`（唯一约束 user_id + article_id）、`Music`
    - 创建对应的 Spring Data JPA Repository 接口
    - 配置 JPA 自动建表（`spring.jpa.hibernate.ddl-auto=update`）
    - _需求：1.1, 3.4, 4.4, 5.3, 6.9_

- [x] 2. 用户认证模块
  - [x] 2.1 实现后端认证功能
    - 创建 `JwtUtil` 工具类：Token 生成（包含 userId、username）、Token 解析、Token 验证
    - 创建 `JwtAuthenticationFilter`（Spring Security Filter），拦截请求并验证 Token
    - 配置 Spring Security：放行注册/登录接口，其余需认证接口需携带 Token
    - 配置 `BCryptPasswordEncoder` Bean
    - 创建 `RegisterRequest`、`LoginRequest` DTO 和 `UserDTO`、`LoginResponse` DTO
    - 实现 `AuthService`：注册（用户名/邮箱唯一校验、密码长度校验、BCrypt 加密）、登录（凭据验证、生成 JWT）
    - 实现 `AuthController`：`POST /api/auth/register`、`POST /api/auth/login`
    - _需求：1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [x]* 2.2 编写后端认证单元测试
    - 使用 JUnit 5 + Mockito 测试 `AuthService` 的注册和登录逻辑
    - 测试 `JwtUtil` 的 Token 生成与解析
    - 测试密码加密和校验逻辑
    - 测试用户名/邮箱重复、密码过短等异常场景
    - _需求：8.1_

  - [x]* 2.3 编写后端认证属性测试
    - **属性 1：密码安全性** — 生成随机密码，验证 BCrypt 加密后不等于原文且可验证
    - **验证需求：1.5, 1.6**
    - **属性 2：JWT Token 往返一致性** — 生成随机用户 ID 和用户名，编码后解码验证一致
    - **验证需求：2.4**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.2_

  - [x]* 2.4 编写后端认证集成测试
    - 使用 Spring Boot Test + H2 测试完整认证流程：注册 → 登录 → 获取 Token → 使用 Token 访问认证接口
    - 测试重复注册、错误密码登录、无效 Token 访问等场景
    - _需求：8.2_

  - [x] 2.5 实现前端登录和注册页面
    - 创建 Pinia store `useAuthStore`：管理用户登录状态、JWT Token 存储（localStorage）、用户信息
    - 创建 `composables/useAuth.ts`：封装登录、注册、退出登录逻辑
    - 创建 `plugins/auth.client.ts`：应用启动时从 localStorage 恢复登录状态
    - 创建 `middleware/auth.ts`：路由中间件，未登录用户访问需认证页面时跳转登录页
    - 实现 `pages/login.vue`（CSR）：登录表单（用户名、密码）、表单校验、提交调用后端 API、成功后跳转首页
    - 实现 `pages/register.vue`（CSR）：注册表单（用户名、密码、邮箱）、表单校验（密码≥6位）、提交调用后端 API、成功后跳转登录页
    - 在导航栏中展示登录/注册按钮或用户信息和退出按钮
    - _需求：7.1, 7.2, 7.3, 7.4_

  - [x]* 2.6 编写前端认证测试
    - 使用 Vitest + Vue Test Utils 测试登录和注册表单的校验逻辑
    - 测试表单提交成功和失败场景
    - 测试 `useAuthStore` 的状态管理逻辑
    - _需求：8.7_

- [x] 3. 检查点 — 认证模块验收
  - 确保所有认证相关测试通过，如有问题请向用户确认。

- [x] 4. 文章管理模块
  - [x] 4.1 实现后端文章 CRUD
    - 创建 `CreateArticleRequest`、`UpdateArticleRequest` DTO
    - 创建 `ArticleDTO`、`ArticleSummaryDTO`、`ArticleDetailDTO` DTO
    - 实现 `ArticleService`：创建文章（标题/正文非空校验、自动记录作者和时间）、修改文章（权限校验、更新时间）、删除文章（权限校验、级联删除评论和点赞）、分页列表（按创建时间倒序）、文章详情（含点赞数和当前用户点赞状态）
    - 实现 `ArticleController`：`POST /api/articles`、`PUT /api/articles/{id}`、`DELETE /api/articles/{id}`、`GET /api/articles?page=&size=`、`GET /api/articles/{id}`
    - _需求：3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9_

  - [x]* 4.2 编写后端文章单元测试
    - 使用 JUnit 5 + Mockito 测试 `ArticleService` 的所有业务逻辑
    - 测试标题/正文为空、修改/删除非自己文章等异常场景
    - _需求：8.1_

  - [x]* 4.3 编写后端文章属性测试
    - **属性 3：文章详情完整性** — 生成随机文章，创建后查询验证字段完整且作者 ID 一致
    - **验证需求：3.4, 3.9**
    - **属性 4：文章列表排序不变量** — 创建多篇文章，查询列表验证按创建时间严格倒序
    - **验证需求：3.8**
    - **属性 9：Article 创建-查询往返** — 生成随机文章，创建后查询验证标题和正文一致
    - **验证需求：8.8**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.3, 8.8_

  - [x]* 4.4 编写后端文章集成测试
    - 使用 Spring Boot Test + H2 测试文章完整 CRUD 流程：创建 → 查询 → 修改 → 删除 → 验证级联删除
    - 测试分页查询和排序
    - 测试权限校验（修改/删除非自己文章返回 403）
    - _需求：8.3_

  - [x] 4.5 实现前端文章列表和详情页
    - 实现 `pages/index.vue`（SSR）：使用 `useAsyncData` 在服务端获取分页文章列表，展示标题、作者、创建时间，支持分页导航
    - 实现 `pages/articles/[id].vue`（SSR）：使用 `useAsyncData` 在服务端获取文章详情，展示标题、正文、作者、创建时间、更新时间、点赞数
    - 创建 `composables/useArticle.ts`：封装文章 CRUD API 调用
    - 实现文章创建/编辑功能（已登录用户可见的发表文章入口和编辑按钮）
    - 实现文章删除功能（仅作者可见删除按钮，确认后删除）
    - _需求：7.1, 7.5, 7.6, 3.1, 3.5, 3.6, 3.8, 3.9_

  - [x]* 4.6 编写前端文章页面测试
    - 使用 Vitest + Vue Test Utils 测试文章列表渲染和分页逻辑
    - 测试文章详情页数据展示
    - 测试文章创建/编辑表单校验
    - _需求：8.1_

- [x] 5. 检查点 — 文章模块验收
  - 确保所有文章相关测试通过，如有问题请向用户确认。

- [x] 6. 评论系统模块
  - [x] 6.1 实现后端评论功能
    - 创建 `CreateCommentRequest` DTO（包含 content 和可选的 parentId）
    - 创建 `CommentDTO`、`CommentTreeDTO` DTO（树形结构）
    - 实现 `CommentService`：创建评论（内容非空校验、文章存在校验）、创建嵌套评论（父评论存在校验）、删除评论（权限校验、级联删除子评论）、查询文章评论列表（树形结构、按创建时间正序）
    - 实现 `CommentController`：`POST /api/articles/{articleId}/comments`、`GET /api/articles/{articleId}/comments`、`DELETE /api/comments/{id}`
    - _需求：4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

  - [x]* 6.2 编写后端评论单元测试
    - 使用 JUnit 5 + Mockito 测试 `CommentService` 的所有业务逻辑
    - 测试评论内容为空、文章不存在、删除非自己评论等异常场景
    - _需求：8.1_

  - [x]* 6.3 编写后端评论属性测试
    - **属性 5：评论树形结构与排序** — 创建随机评论树，查询验证顶级评论和嵌套评论均按创建时间正序，嵌套评论的 parent_id 正确
    - **验证需求：4.4, 4.5**
    - **属性 10：Comment 创建-查询往返** — 生成随机评论，创建后查询验证内容、评论者和所属文章一致
    - **验证需求：8.9**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.4, 8.9_

  - [x]* 6.4 编写后端评论集成测试
    - 使用 Spring Boot Test + H2 测试评论完整流程：创建评论 → 创建嵌套评论 → 查询树形结构 → 删除评论验证级联删除
    - 测试权限校验（删除非自己评论返回 403）
    - 测试文章不存在时创建评论返回 404
    - _需求：8.4_

  - [x] 6.5 实现前端评论功能
    - 创建 `components/CommentList.vue`：递归渲染评论树形结构（顶级评论 + 嵌套评论）
    - 创建 `components/CommentForm.vue`：评论输入框，支持回复指定评论（传入 parentId）
    - 创建 `composables/useComment.ts`：封装评论 API 调用
    - 在文章详情页 `pages/articles/[id].vue` 中集成评论列表和评论表单
    - 实现评论删除功能（仅评论者可见删除按钮）
    - _需求：4.1, 4.3, 4.5, 4.6, 7.6_

  - [x]* 6.6 编写前端评论组件测试
    - 使用 Vitest + Vue Test Utils 测试 `CommentList` 的树形渲染
    - 测试 `CommentForm` 的表单校验和提交逻辑
    - 测试嵌套评论的回复交互
    - _需求：8.1_

- [x] 7. 检查点 — 评论模块验收
  - 确保所有评论相关测试通过，如有问题请向用户确认。

- [x] 8. 点赞功能模块
  - [x] 8.1 实现后端点赞功能
    - 实现 `LikeService`：切换点赞（点赞/取消点赞）、查询文章点赞数、查询用户是否已点赞
    - 实现 `LikeController`：`POST /api/articles/{articleId}/like`
    - 确保 article_like 表的唯一约束生效（同一用户对同一文章最多一条记录）
    - 点赞目标文章不存在时返回 404
    - _需求：5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

  - [ ]* 8.2 编写后端点赞单元测试
    - 使用 JUnit 5 + Mockito 测试 `LikeService` 的切换点赞逻辑
    - 测试文章不存在时点赞的异常场景
    - _需求：8.1_

  - [ ]* 8.3 编写后端点赞属性测试
    - **属性 6：点赞切换与唯一性** — 随机用户对随机文章执行偶数次点赞，验证回到初始状态，数据库最多一条记录
    - **验证需求：5.2, 5.3**
    - **属性 7：点赞计数一致性** — 随机数量用户点赞，验证 API 返回的点赞数等于数据库记录数
    - **验证需求：5.4**
    - 使用 jqwik 库，每个属性最少 100 次迭代
    - _需求：8.5_

  - [ ]* 8.4 编写后端点赞集成测试
    - 使用 Spring Boot Test + H2 测试点赞完整流程：点赞 → 验证状态 → 再次点赞取消 → 验证计数
    - 测试文章不存在时点赞返回 404
    - _需求：8.5_

  - [x] 8.5 实现前端点赞功能
    - 创建 `composables/useLike.ts`：封装点赞 API 调用
    - 在文章详情页 `pages/articles/[id].vue` 中集成点赞按钮：展示点赞数、已登录用户可点击切换点赞状态、未登录用户点击引导登录
    - 点赞状态实时更新（乐观更新 UI）
    - _需求：5.1, 5.2, 5.4, 5.5, 7.2_

  - [ ]* 8.6 编写前端点赞功能测试
    - 使用 Vitest + Vue Test Utils 测试点赞按钮的交互逻辑
    - 测试点赞状态切换和计数更新
    - _需求：8.1_

- [x] 9. 检查点 — 点赞模块验收
  - 确保所有点赞相关测试通过，如有问题请向用户确认。

- [x] 10. 音乐播放器模块
  - [x] 10.1 实现后端音乐接口
    - 创建 `CreateMusicRequest` DTO 和 `MusicDTO` DTO
    - 实现 `MusicService`：获取音乐列表、创建音乐记录（管理员）
    - 实现 `MusicController`：`GET /api/music`、`POST /api/music`
    - _需求：6.9, 6.10_

  - [ ]* 10.2 编写后端音乐接口测试
    - 使用 JUnit 5 + Mockito 测试 `MusicService`
    - 使用 Spring Boot Test + H2 测试音乐接口的集成流程
    - _需求：8.1_

  - [x] 10.3 实现前端音乐播放器组件
    - 创建 `utils/playerNavigation.js`：播放列表导航纯函数（next/prev 索引计算，使用模运算实现循环）
    - 创建 `components/MusicPlayer.vue`：全局悬浮音乐播放器组件
      - 使用 HTML5 Audio API 实现播放/暂停
      - 展示当前曲目名称和播放进度
      - 上一曲/下一曲按钮
      - 当前曲目播放结束自动切换下一曲
      - 播放列表最后一首结束后从第一首重新开始（循环播放）
    - 创建 `composables/useMusic.ts`：封装音乐列表 API 调用
    - 在 `layouts/default.vue` 中集成 MusicPlayer 组件
    - _需求：6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.10_

  - [ ]* 10.4 编写前端音乐播放器测试
    - 使用 Vitest + Vue Test Utils 测试 MusicPlayer 组件的播放、暂停、切换曲目功能
    - **属性 8：播放列表导航** — 使用 fast-check 测试 `playerNavigation.js` 的 next/prev 纯函数，验证 `(current + 1) % length` 和 `(current - 1 + length) % length`
    - **验证需求：6.4, 6.5, 6.6, 6.7**
    - 每个属性最少 100 次迭代
    - _需求：8.6_

- [x] 11. 检查点 — 音乐播放器模块验收
  - 确保所有音乐播放器相关测试通过，如有问题请向用户确认。

- [x] 12. 全局集成与收尾
  - [x] 12.1 前端错误处理和全局优化
    - 配置 `useFetch` / `$fetch` 的全局错误拦截：401 清除 Token 跳转登录、403 提示无权限、404 展示错误页
    - 创建 `error.vue` 错误页面
    - 确保 SSR 阶段错误不会导致整页崩溃
    - 在所有需要认证的操作处集成 `middleware/auth.ts` 守卫
    - _需求：7.2, 7.3, 7.4_

  - [x] 12.2 前后端联调与整体验证
    - 验证所有 API 端点与前端页面的对接
    - 验证 SSR 渲染：文章列表页和详情页在服务端正确渲染
    - 验证完整用户流程：注册 → 登录 → 发表文章 → 评论 → 点赞 → 音乐播放
    - _需求：全部需求_

- [x] 13. 最终检查点 — 全部测试通过
  - 运行后端全部测试（单元测试 + 集成测试 + 属性测试），确保全部通过。
  - 运行前端全部测试（组件测试 + 属性测试），确保全部通过。
  - 如有问题请向用户确认。

## 备注

- 标记 `*` 的子任务为可选测试任务，可跳过以加速 MVP 开发
- 每个任务引用了具体的需求编号，确保需求可追溯
- 检查点确保每个功能模块完成后进行增量验证
- 属性测试验证设计文档中定义的正确性属性
- 单元测试和属性测试互为补充：单元测试验证具体场景，属性测试验证通用不变量
- 后端测试使用 H2 内存数据库，不依赖外部 MySQL
- 前端属性测试使用 fast-check 库
