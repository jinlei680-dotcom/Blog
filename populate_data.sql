-- ============================================
-- Blog data population script
-- ============================================

USE my_blog;

-- Password BCrypt hash (same as admin's password for all test users)
SET @pw = '$2a$10$tA51u4ihJ9RsAms.IhuT6uHO3KUBRs5bl8UKmP.wjYcbsghuZotyK';

-- ============================================
-- 1. Users
-- ============================================
UPDATE `user` SET role = 'ROLE_ADMIN' WHERE id = 1;

INSERT INTO `user` (username, password, email, role, avatar_url, bio, created_at) VALUES
('zhangsan', @pw, 'zhangsan@blog.com', 'ROLE_USER', 'https://api.dicebear.com/7.x/initials/svg?seed=ZS', '全栈工程师，热爱开源，喜欢写博客分享技术心得。', NOW()),
('lisi',     @pw, 'lisi@blog.com',     'ROLE_USER', 'https://api.dicebear.com/7.x/initials/svg?seed=LS', '前端开发工程师，Vue/React 技术栈，UI/UX 爱好者。', NOW()),
('wangwu',   @pw, 'wangwu@blog.com',   'ROLE_USER', 'https://api.dicebear.com/7.x/initials/svg?seed=WW', '后端开发工程师，专注 Java 生态，微服务实践者。', NOW());

-- ============================================
-- 2. Tags
-- ============================================
INSERT INTO tag (name, created_at) VALUES
('Java',       NOW()),
('Spring Boot', NOW()),
('Vue.js',     NOW()),
('Nuxt3',      NOW()),
('TypeScript', NOW()),
('MySQL',      NOW()),
('Docker',     NOW()),
('前端开发',   NOW()),
('后端开发',   NOW()),
('程序员日记', NOW()),
('性能优化',   NOW()),
('架构设计',   NOW());

-- ============================================
-- 3. Articles (author_id: 1=admin, 2=zhangsan, 3=lisi, 4=wangwu)
-- ============================================
INSERT INTO article (title, content, author_id, featured, created_at, updated_at) VALUES
(
'Spring Boot 微服务架构实战指南',
'<h2>为什么选择 Spring Boot 微服务？</h2>
<p>随着业务规模的增长，单体应用逐渐暴露出维护成本高、部署周期长、扩展困难等问题。Spring Boot 作为 Java 生态中最流行的微服务开发框架，凭借其<strong>自动配置、起步依赖、Actuator 监控</strong>等特性，成为构建微服务的首选方案。</p>

<h3>微服务的核心优势</h3>
<ul>
<li><strong>独立部署</strong>：每个服务可以独立构建、测试、部署，互不影响</li>
<li><strong>技术异构</strong>：不同服务可以选择最适合的技术栈</li>
<li><strong>弹性伸缩</strong>：针对高负载服务进行独立扩容</li>
<li><strong>故障隔离</strong>：单个服务的故障不会导致整个系统崩溃</li>
</ul>

<h3>项目结构设计</h3>
<p>一个典型的 Spring Boot 微服务项目结构如下：</p>
<pre><code>microservice-platform/
├── gateway-service/        # API 网关
├── auth-service/           # 认证授权服务
├── user-service/           # 用户服务
├── article-service/        # 文章服务
├── comment-service/        # 评论服务
└── common/                 # 公共模块</code></pre>

<h3>服务间通信</h3>
<p>在微服务架构中，服务间通信是核心挑战之一。常用的通信方式包括：</p>
<ol>
<li><strong>REST API (OpenFeign)</strong>：声明式 HTTP 客户端，简洁易用</li>
<li><strong>消息队列 (RabbitMQ/Kafka)</strong>：异步解耦，削峰填谷</li>
<li><strong>gRPC</strong>：高性能的 RPC 框架，适合低延迟场景</li>
</ol>

<h3>服务治理</h3>
<p>当微服务数量增长到数十甚至上百个时，服务治理变得至关重要：</p>
<ul>
<li><strong>注册中心 (Nacos/Consul)</strong>：管理服务实例的注册与发现</li>
<li><strong>配置中心 (Nacos/Apollo)</strong>：集中管理配置，支持动态刷新</li>
<li><strong>熔断降级 (Sentinel)</strong>：防止级联故障，保障系统可用性</li>
<li><strong>链路追踪 (SkyWalking)</strong>：可视化调用链路，快速定位问题</li>
</ul>

<blockquote>
<p>微服务不是银弹。对于小型团队或简单业务，单体架构往往是更好的选择。微服务带来的复杂性需要通过完善的 DevOps 体系和自动化工具来消化。</p>
</blockquote>

<p>下一篇文章我们将深入探讨 Spring Cloud Gateway 的路由配置和过滤器链实现。</p>',
1, TRUE, '2025-11-15 09:30:00', '2025-11-15 10:00:00'
),

(
'Vue 3 Composition API 深度解析',
'<h2>从 Options API 到 Composition API</h2>
<p>Vue 3 引入的 Composition API 是 Vue 生态最重大的变革之一。它以<strong>更好的逻辑复用、更灵活的组织方式、更完整的 TypeScript 支持</strong>，重新定义了 Vue 组件的编写方式。</p>

<h3>为什么需要 Composition API？</h3>
<p>在 Options API 中，组件逻辑按照选项类型（data、methods、computed、watch）分散在不同的选项中。当组件变得复杂时，同一功能的逻辑分散在多处，难以阅读和维护：</p>

<pre><code>// Options API — 同一功能的代码分散在各处
export default {
  data() {
    return { searchQuery: "", results: [] }
  },
  methods: {
    async search() { /* ... */ },
    clearSearch() { /* ... */ }
  },
  watch: {
    searchQuery: { /* ... */ }
  }
}</code></pre>

<p>而 Composition API 允许将同一功能的代码组织在一起：</p>

<pre><code>// Composition API — 关注点内聚
export default {
  setup() {
    const searchQuery = ref("")
    const results = ref([])

    async function search() { /* ... */ }
    function clearSearch() { /* ... */ }
    watch(searchQuery, () => { /* ... */ })

    return { searchQuery, results, search, clearSearch }
  }
}</code></pre>

<h3>核心响应式 API</h3>
<ul>
<li><code>ref()</code>：创建包含基本类型的响应式引用</li>
<li><code>reactive()</code>：创建深层响应式对象</li>
<li><code>computed()</code>：创建计算属性</li>
<li><code>watch()</code> & <code>watchEffect()</code>：监听数据变化</li>
</ul>

<h3>自定义组合函数 (Composables)</h3>
<p>Composition API 最大的威力在于可以轻松抽取可复用的逻辑：</p>

<pre><code>// composables/useSearch.js
export function useSearch(fetchFn) {
  const query = ref("")
  const results = ref([])
  const loading = ref(false)

  async function execute() {
    loading.value = true
    results.value = await fetchFn(query.value)
    loading.value = false
  }

  return { query, results, loading, execute }
}</code></pre>

<h3>与 TypeScript 的完美配合</h3>
<p>Composition API 天然支持 TypeScript 类型推导，无需额外的类型标注即可获得完整的类型提示。这是 Options API 难以实现的优势。</p>

<p>总的来说，Composition API 不只是在 API 层面的升级，更是一种<strong>编写 Vue 应用的思维转变</strong>。</p>',
2, TRUE, '2025-11-20 14:00:00', '2025-11-20 15:30:00'
),

(
'MySQL 性能优化：从入门到精通',
'<h2>为什么你的查询这么慢？</h2>
<p>数据库性能问题是后端开发中最常见的挑战之一。一个糟糕的查询可能导致整个系统的响应时间从毫秒级飙升到秒级。<strong>本文将从索引设计、查询优化、表结构设计三个维度</strong>，系统性地介绍 MySQL 性能优化的核心方法。</p>

<h3>一、索引：数据库性能的基石</h3>
<p>索引是提高查询性能最有效的手段，但错误的索引设计可能适得其反：</p>

<ul>
<li><strong>联合索引的最左前缀原则</strong>：联合索引 (a, b, c) 只有在查询条件包含 a 时才能被充分利用</li>
<li><strong>覆盖索引</strong>：当查询的所有列都在索引中时，无需回表查询，性能大幅提升</li>
<li><strong>避免索引失效</strong>：在 WHERE 子句中对列进行函数操作或隐式类型转换会导致索引失效</li>
</ul>

<pre><code>-- 不好的做法：索引失效
SELECT * FROM article WHERE YEAR(created_at) = 2025;

-- 好的做法：使用范围查询
SELECT * FROM article WHERE created_at >= "2025-01-01" AND created_at &lt; "2026-01-01";</code></pre>

<h3>二、EXPLAIN 分析执行计划</h3>
<p>EXPLAIN 是 MySQL 性能调优最常用的工具。关键字段解读：</p>

<ul>
<li><strong>type</strong>：访问类型，从优到劣依次是 system > const > eq_ref > ref > range > index > ALL</li>
<li><strong>rows</strong>：预估需要扫描的行数</li>
<li><strong>Extra</strong>：额外信息，特别注意 "Using filesort" 和 "Using temporary"</li>
</ul>

<h3>三、SQL 查询优化技巧</h3>
<ol>
<li><strong>避免 SELECT *</strong>：只查询需要的列，减少数据传输和内存占用</li>
<li><strong>分页优化</strong>：大偏移量分页使用子查询或延迟关联</li>
<li><strong>JOIN 优化</strong>：小表驱动大表，确保 JOIN 列上有索引</li>
<li><strong>避免在 WHERE 中使用 OR</strong>：使用 UNION ALL 替代</li>
</ol>

<h3>四、慢查询日志与分析</h3>
<p>开启慢查询日志是发现性能瓶颈的第一步：</p>
<pre><code>-- 开启慢查询日志
SET GLOBAL slow_query_log = "ON";
SET GLOBAL long_query_time = 1;
SET GLOBAL log_queries_not_using_indexes = "ON";</code></pre>

<h3>五、表结构设计考量</h3>
<ul>
<li><strong>字段类型选择</strong>：能用 INT 就不用 VARCHAR，能用 DATE 就不用 DATETIME</li>
<li><strong>垂直拆分</strong>：将不常用的列分离到扩展表</li>
<li><strong>水平拆分</strong>：按时间或 ID 范围分表</li>
<li><strong>适当的冗余</strong>：在性能关键场景下，少量冗余可以避免复杂的 JOIN</li>
</ul>

<blockquote>
<p>性能优化的黄金法则：先测量，再优化。不要凭感觉猜测瓶颈所在，用 EXPLAIN 和慢查询日志来指导你的优化方向。</p>
</blockquote>',
4, TRUE, '2025-12-05 10:00:00', '2025-12-05 11:00:00'
),

(
'Docker 容器化部署最佳实践',
'<h2>从开发到生产的容器化之旅</h2>
<p>Docker 已经成为现代软件部署的标准方式。它解决了<strong>"在我机器上能跑"</strong>的经典问题，让应用在任何环境中都能一致地运行。本文将分享 Docker 在实际项目中的最佳实践。</p>

<h3>编写高效的 Dockerfile</h3>
<p>一个好的 Dockerfile 应该遵循以下原则：</p>

<ol>
<li><strong>选择合适的基础镜像</strong>：alpine 版本更小更安全，但可能缺少某些依赖</li>
<li><strong>利用多阶段构建</strong>：分离构建环境和运行环境，大幅减小镜像体积</li>
<li><strong>充分利用缓存</strong>：将不常变化的层放在前面，频繁变化的放在后面</li>
<li><strong>以非 root 用户运行</strong>：提高容器安全性</li>
</ol>

<pre><code># 多阶段构建示例 — Spring Boot 应用
FROM maven:3.8-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:11-jre-slim
RUN groupadd -r app && useradd -r -g app app
COPY --from=build /app/target/*.jar app.jar
USER app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]</code></pre>

<h3>Docker Compose 编排服务</h3>
<p>对于多服务项目，Docker Compose 是管理本地开发和测试环境的最佳工具：</p>

<pre><code>version: "3.8"
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: Blog@2024!
      MYSQL_DATABASE: my_blog
    volumes:
      - mysql-data:/var/lib/mysql
    restart: always

  backend:
    build: ./blog-backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/my_blog

  frontend:
    build: ./blog-frontend
    ports:
      - "3000:3000"
    depends_on:
      - backend

volumes:
  mysql-data:</code></pre>

<h3>生产环境注意事项</h3>
<ul>
<li><strong>资源限制</strong>：使用 --memory 和 --cpus 限制容器资源，防止单容器耗尽宿主机资源</li>
<li><strong>健康检查</strong>：配置 HEALTHCHECK 指令，让编排系统了解容器健康状态</li>
<li><strong>日志管理</strong>：将日志输出到 stdout/stderr，由日志收集系统统一处理</li>
<li><strong>优雅关闭</strong>：正确处理 SIGTERM 信号，确保请求不丢失</li>
</ul>

<p>容器化不是终点，而是云原生之旅的起点。掌握 Docker 之后，Kubernetes 编排、Service Mesh、GitOps 等更高级的主题等着你去探索。</p>',
1, FALSE, '2025-12-18 08:00:00', '2026-01-02 16:00:00'
),

(
'TypeScript 类型体操：高级类型入门',
'<h2>类型不仅是安全网，更是设计工具</h2>
<p>TypeScript 的类型系统远比大多数开发者使用的要强大。<strong>条件类型、映射类型、模板字面量类型、递归类型</strong>——这些高级类型特性可以帮助你在编译期捕获更多的错误，同时让代码的意图更加清晰。</p>

<h3>条件类型 (Conditional Types)</h3>
<p>条件类型允许你根据类型关系创建新的类型：</p>
<pre><code>type IsString&lt;T&gt; = T extends string ? true : false;

type A = IsString&lt;"hello"&gt;;  // true
type B = IsString&lt;42&gt;;        // false</code></pre>

<h3>映射类型 (Mapped Types)</h3>
<p>映射类型可以将对象类型的每个属性转换为新的类型：</p>
<pre><code>type Readonly&lt;T&gt; = {
  readonly [K in keyof T]: T[K]
};

type Optional&lt;T&gt; = {
  [K in keyof T]?: T[K]
};</code></pre>

<h3>模板字面量类型 (Template Literal Types)</h3>
<p>TypeScript 4.1 引入的模板字面量类型，让字符串类型也能被精确描述：</p>
<pre><code>type EventName&lt;T extends string&gt; = `on${Capitalize&lt;T&gt;}`;
type ClickEvent = EventName&lt;"click"&gt;;  // "onClick"

type CSSProperty = `--${string}`;
const valid: CSSProperty = "--main-color";  // ✓</code></pre>

<h3>递归类型</h3>
<p>递归类型可以描述嵌套的数据结构：</p>
<pre><code>type JSONValue =
  | string
  | number
  | boolean
  | null
  | JSONValue[]
  | { [key: string]: JSONValue };</code></pre>

<h3>实战：类型安全的 API 客户端</h3>
<p>利用高级类型，我们可以构建一个类型安全的 API 客户端：</p>
<pre><code>type ApiResponse&lt;T&gt; = {
  code: number;
  message: string;
  data: T;
};

// 自动推断响应类型
async function fetchArticle(id: number): Promise&lt;ApiResponse&lt;Article&gt;&gt; {
  return fetch(`/api/articles/${id}`).then(r => r.json());
}</code></pre>

<p>类型系统的终极目标不是让代码变复杂，而是<strong>让错误的代码无法编译通过</strong>。当你习惯使用高级类型后，你会发现 TypeScript 的类型系统是你最可靠的代码审查伙伴。</p>',
3, TRUE, '2026-01-10 13:00:00', '2026-01-10 14:00:00'
),

(
'Nuxt 3 服务端渲染实战分享',
'<h2>SEO 友好的现代前端框架</h2>
<p>在我用 Nuxt 3 重写博客的过程中，踩了不少坑，也收获了很多经验。这篇文章将分享<strong>Nuxt 3 的核心概念、SSR 渲染策略选择、以及性能优化的实践心得</strong>。</p>

<h3>Nuxt 3 的渲染模式</h3>
<p>Nuxt 3 支持多种渲染模式，可以为不同页面选择合适的策略：</p>

<ul>
<li><strong>SSR (服务端渲染)</strong>：每次请求在服务端渲染，SEO 最佳，适合内容页面</li>
<li><strong>SSG (静态生成)</strong>：构建时预渲染为静态 HTML，性能最好</li>
<li><strong>CSR (客户端渲染)</strong>：在浏览器中渲染，适合需要登录的后台页面</li>
<li><strong>ISR (增量静态再生)</strong>：结合 SSG 和 SSR 的优点，适合更新不频繁的内容</li>
</ul>

<pre><code>// nuxt.config.ts — 为不同路由设置渲染策略
export default defineNuxtConfig({
  routeRules: {
    "/": { ssr: true },
    "/article/**": { ssr: true },
    "/create": { ssr: false },
    "/admin/**": { ssr: false, redirect: "/login" }
  }
});</code></pre>

<h3>数据获取：useFetch vs useAsyncData</h3>
<p>Nuxt 3 提供了两个核心的数据获取 composable：</p>

<ul>
<li><code>useFetch()</code>：最简单的数据获取方式，直接传入 URL 即可</li>
<li><code>useAsyncData()</code>：更灵活，适合需要自定义请求逻辑的场景</li>
</ul>

<h3>中间件与路由守卫</h3>
<p>Nuxt 3 的中间件系统非常灵活，可以在页面渲染前执行逻辑：</p>
<pre><code>// middleware/auth.ts
export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore();
  if (!auth.isLoggedIn) {
    return navigateTo("/login");
  }
});</code></pre>

<h3>我遇到的坑</h3>
<ol>
<li><strong>SSR 中没有 window 对象</strong>：需要使用 process.client 判断或在 onMounted 中访问</li>
<li><strong>Pinia 的状态 hydration</strong>：需要通过插件在服务端和客户端之间同步状态</li>
<li><strong>样式闪烁 (FOUC)</strong>：CSS-in-JS 方案在 SSR 下需要注意提取关键 CSS</li>
</ol>

<p>总的来说，Nuxt 3 是目前 Vue 生态中做全栈应用的最佳选择。它的约定优于配置的理念，让你可以更专注于业务逻辑的实现。</p>',
1, FALSE, '2026-02-20 16:00:00', '2026-02-20 17:30:00'
),

(
'前后端分离架构下的 API 设计规范',
'<h2>好的 API 设计节省的是沟通成本</h2>
<p>在前端与后端分离的架构中，API 是团队协作的<strong>契约</strong>。一个设计良好的 API 不仅能提高开发效率，还能减少前后端之间的沟通摩擦。以下是我们在实际项目中总结的 API 设计规范。</p>

<h3>URL 设计</h3>
<ul>
<li>使用名词而非动词：<code>/articles</code> 而非 <code>/getArticles</code></li>
<li>使用复数形式：<code>/articles/123</code> 而非 <code>/article/123</code></li>
<li>层级关系通过路径表示：<code>/articles/123/comments</code></li>
<li>使用连字符提高可读性：<code>/article-drafts</code></li>
</ul>

<h3>HTTP 方法语义</h3>
<ul>
<li><strong>GET</strong>：获取资源，幂等</li>
<li><strong>POST</strong>：创建新资源</li>
<li><strong>PUT</strong>：完整更新资源，幂等</li>
<li><strong>PATCH</strong>：部分更新资源</li>
<li><strong>DELETE</strong>：删除资源，幂等</li>
</ul>

<h3>统一响应格式</h3>
<pre><code>{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "文章标题",
    "content": "...",
    "author": { "id": 1, "username": "admin" },
    "createdAt": "2026-01-01T00:00:00"
  }
}</code></pre>

<h3>分页响应规范</h3>
<pre><code>{
  "code": 200,
  "data": {
    "content": [...],
    "totalElements": 150,
    "totalPages": 15,
    "size": 10,
    "number": 0
  }
}</code></pre>

<h3>错误处理</h3>
<ul>
<li>使用合适的 HTTP 状态码：400 Bad Request、401 Unauthorized、403 Forbidden、404 Not Found、500 Internal Server Error</li>
<li>错误响应包含详细但不暴露敏感信息的错误消息</li>
<li>表单验证错误使用字段级别的错误信息</li>
</ul>

<h3>版本控制</h3>
<p>API 版本控制建议通过 URL 前缀实现：<code>/api/v1/articles</code>。当需要进行破坏性变更时，创建新版本而非修改现有版本。</p>

<blockquote>
<p>API 是给开发者用的 UI。像设计用户界面一样用心设计你的 API — 保持一致性，遵循最小惊讶原则。</p>
</blockquote>',
1, FALSE, '2026-03-05 09:00:00', '2026-03-05 09:45:00'
),

(
'程序员如何保持持续学习的心态',
'<h2>在技术浪潮中找到自己的节奏</h2>
<p>作为程序员，我们生活在一个技术日新月异的时代。每个月都有新框架发布，每周都有新工具问世，每天都有新的最佳实践被提出。<strong>面对如此快速的变化，如何保持持续学习的心态而不被焦虑淹没？</strong></p>

<h3>建立 T 型知识结构</h3>
<p>T 型人才是指：在某一领域有深度（竖线），同时拥有广泛的知识面（横线）。</p>
<ul>
<li><strong>深度</strong>：选择 1-2 个核心技术栈深入钻研，成为专家</li>
<li><strong>广度</strong>：了解相关领域的基础知识，能够做出正确的技术选型</li>
</ul>

<h3>学习金字塔</h3>
<p>不同的学习方式，知识留存率差异巨大：</p>
<ol>
<li><strong>听讲</strong>：5% 留存率</li>
<li><strong>阅读</strong>：10% 留存率</li>
<li><strong>视听结合</strong>：20% 留存率</li>
<li><strong>演示</strong>：30% 留存率</li>
<li><strong>小组讨论</strong>：50% 留存率</li>
<li><strong>实践练习</strong>：75% 留存率</li>
<li><strong>教会他人</strong>：90% 留存率</li>
</ol>

<p>这就是我写博客的原因——通过写作来加深自己的理解，同时帮助他人。教是最好的学。</p>

<h3>实用的学习策略</h3>
<ul>
<li><strong>每天 30 分钟阅读</strong>：订阅高质量的技术周刊，保持对行业动态的感知</li>
<li><strong>每月一个小项目</strong>：用一个周末做一个 mini project，把学到的东西实践出来</li>
<li><strong>参与开源</strong>：从修文档拼写错误开始，逐步深入到你使用的框架的核心</li>
<li><strong>输出倒逼输入</strong>：给自己设定写作目标，比如每月一篇技术博客</li>
</ul>

<h3>警惕"技术多动症"</h3>
<p>看到新框架就想学，学了几天又跳到另一个新工具——这是很多程序员的通病。应对策略：</p>
<ul>
<li>新技术的出现，先问自己"它解决了什么问题？"</li>
<li>给新技术 2 周的冷静期，如果两周后还感兴趣再深入研究</li>
<li>优先学习那些与当前工作相关的技术</li>
</ul>

<p>技术会过时，但<strong>解决问题的能力、学习能力和沟通能力</strong>永远不会过时。把精力投资在这些核心能力上，比追逐每一个新技术更有价值。</p>',
2, TRUE, '2026-04-01 20:00:00', '2026-04-01 21:00:00'
);

-- ============================================
-- 4. Article-Tag associations
-- ============================================
-- Article 1 (Spring Boot 微服务) -> Java, Spring Boot, 后端开发, 架构设计
INSERT INTO article_tag (article_id, tag_id) VALUES (1, 1), (1, 2), (1, 9), (1, 12);

-- Article 2 (Vue 3) -> Vue.js, TypeScript, 前端开发
INSERT INTO article_tag (article_id, tag_id) VALUES (2, 3), (2, 5), (2, 8);

-- Article 3 (MySQL 性能优化) -> MySQL, 后端开发, 性能优化
INSERT INTO article_tag (article_id, tag_id) VALUES (3, 6), (3, 9), (3, 11);

-- Article 4 (Docker) -> Docker, 后端开发, 架构设计
INSERT INTO article_tag (article_id, tag_id) VALUES (4, 7), (4, 9), (4, 12);

-- Article 5 (TypeScript) -> TypeScript, 前端开发
INSERT INTO article_tag (article_id, tag_id) VALUES (5, 5), (5, 8);

-- Article 6 (Nuxt 3) -> Nuxt3, Vue.js, 前端开发
INSERT INTO article_tag (article_id, tag_id) VALUES (6, 3), (6, 4), (6, 8);

-- Article 7 (API 设计) -> 后端开发, 前端开发, 架构设计
INSERT INTO article_tag (article_id, tag_id) VALUES (7, 8), (7, 9), (7, 12);

-- Article 8 (程序员日记) -> 程序员日记
INSERT INTO article_tag (article_id, tag_id) VALUES (8, 10);

-- ============================================
-- 5. Comments
-- ============================================
-- Comments on Article 1
INSERT INTO comment (content, user_id, article_id, parent_id, created_at) VALUES
('写得很详细！Spring Boot 微服务这块最近正在学习，文章给了我很多启发。请问下一篇文章什么时候出？', 2, 1, NULL, '2025-11-16 08:00:00'),
('补充一点：服务间通信还可以考虑使用 Spring Cloud Stream，它对消息中间件做了很好的抽象。', 4, 1, NULL, '2025-11-16 10:30:00'),
('同意作者说的，微服务不是银弹。我们公司之前盲目上微服务，结果运维成本反而增加了不少。', 3, 1, NULL, '2025-11-17 14:00:00');

-- Reply to first comment
INSERT INTO comment (content, user_id, article_id, parent_id, created_at) VALUES
('已经在写了，预计下周发布，会重点讲 Gateway 的过滤器链实现！', 1, 1, 1, '2025-11-16 09:00:00');

-- Comments on Article 2
INSERT INTO comment (content, user_id, article_id, parent_id, created_at) VALUES
('Composition API 确实比 Options API 好太多，特别是在大型项目中。setup 语法糖更是锦上添花。', 4, 2, NULL, '2025-11-21 09:00:00'),
('请问在 composable 中如何处理异步数据的错误状态？有没有推荐的模式？', 1, 2, NULL, '2025-11-22 11:00:00');

-- Reply to question
INSERT INTO comment (content, user_id, article_id, parent_id, created_at) VALUES
('一般我会在 composable 中暴露 error 和 loading 两个 ref，让组件根据状态渲染不同的 UI。也可以用 vue-use 的 useAsyncState 来简化。', 2, 2, 6, '2025-11-22 14:00:00');

-- Comments on Article 3
INSERT INTO comment (content, user_id, article_id, parent_id, created_at) VALUES
('EXPLAIN 那部分写得特别好，我以前只会看 type，不知道 Extra 字段也这么重要。', 2, 3, NULL, '2025-12-06 08:00:00'),
('关于分页优化，建议补充一下"游标分页"的方案，对于大数据量的场景比 offset 分页效果好很多。', 1, 3, NULL, '2025-12-06 16:00:00');

-- Comments on Article 5
INSERT INTO comment (content, user_id, article_id, parent_id, created_at) VALUES
('模板字面量类型那段太惊艳了，以前不知道 TypeScript 还有这个能力。学习了！', 4, 5, NULL, '2026-01-11 10:00:00');

-- Comments on Article 8
INSERT INTO comment (content, user_id, article_id, parent_id, created_at) VALUES
('深有同感！技术焦虑是每个程序员的必修课。我现在每天都会花 30 分钟看技术文章，感觉心态好多了。', 4, 8, NULL, '2026-04-02 06:00:00'),
('"教是最好的学" — 这句话说得太对了。自从我开始在公司内部做技术分享，很多之前一知半解的概念都变得清晰了。', 3, 8, NULL, '2026-04-02 12:00:00'),
('文章写得很好！建议可以扩展一下"参与开源"这部分，分享一下你的开源经历和具体建议。', 1, 8, NULL, '2026-04-03 09:00:00');

-- ============================================
-- 6. Article Likes
-- ============================================
INSERT INTO article_like (user_id, article_id, created_at) VALUES
(2, 1, '2025-11-16 07:00:00'),
(3, 1, '2025-11-16 07:30:00'),
(4, 1, '2025-11-16 08:00:00'),
(1, 2, '2025-11-21 08:00:00'),
(4, 2, '2025-11-21 09:30:00'),
(2, 3, '2025-12-05 10:00:00'),
(3, 3, '2025-12-05 11:00:00'),
(1, 3, '2025-12-05 12:00:00'),
(4, 3, '2025-12-06 08:00:00'),
(2, 4, '2025-12-18 08:30:00'),
(3, 5, '2026-01-10 14:30:00'),
(4, 5, '2026-01-10 15:00:00'),
(1, 6, '2026-02-20 17:00:00'),
(2, 7, '2026-03-05 10:00:00'),
(3, 8, '2026-04-01 22:00:00'),
(4, 8, '2026-04-02 06:30:00'),
(1, 8, '2026-04-02 08:00:00');

-- ============================================
-- 7. Notifications
-- ============================================
INSERT INTO notification (recipient_id, sender_id, type, article_id, comment_id, is_read, created_at) VALUES
(1, 2, 'comment', 1, 1, TRUE,  '2025-11-16 08:00:00'),
(1, 4, 'comment', 1, 2, TRUE,  '2025-11-16 10:30:00'),
(1, 3, 'comment', 1, 3, FALSE, '2025-11-17 14:00:00'),
(2, 1, 'reply',   1, 4, FALSE, '2025-11-16 09:00:00'),
(1, 2, 'like',    1, NULL, TRUE,  '2025-11-16 07:00:00'),
(1, 3, 'like',    1, NULL, TRUE,  '2025-11-16 07:30:00'),
(2, 1, 'like',    2, NULL, TRUE,  '2025-11-21 08:00:00'),
(2, 4, 'like',    2, NULL, FALSE, '2025-11-21 09:30:00'),
(1, 2, 'like',    4, NULL, FALSE, '2025-12-18 08:30:00'),
(2, 1, 'comment', 8, 13, FALSE, '2026-04-03 09:00:00');

-- ============================================
-- Done!
-- ============================================
SELECT 'Data population complete!' AS status;
SELECT (SELECT COUNT(*) FROM `user`) AS users,
       (SELECT COUNT(*) FROM article) AS articles,
       (SELECT COUNT(*) FROM tag) AS tags,
       (SELECT COUNT(*) FROM article_tag) AS article_tags,
       (SELECT COUNT(*) FROM comment) AS comments,
       (SELECT COUNT(*) FROM article_like) AS likes,
       (SELECT COUNT(*) FROM notification) AS notifications;
