# 博客前端重设计 + 功能扩展计划

## Context

现有博客（Wanderlust）使用温暖米色/棕金色调的自定义 CSS 设计系统，功能涵盖文章、评论、标签、点赞、通知、音乐播放。用户希望：
1. **UI 全面升级为 Glassmorphism 风格**（深紫渐变背景 + 毛玻璃卡片 + 浮动光球）
2. **新增社交互动功能**：文章收藏、分享、个人简介编辑
3. **提升写作体验**：草稿自动保存、阅读时长估算、写作统计栏

---

## 阶段一：Glassmorphism UI 重设计

### 1.1 新设计系统（CSS 变量）

修改文件：`app/layouts/default.vue` 全局 `<style>` 块（`:root` 在第157行左右）

**颜色体系替换：**
```css
:root {
  /* 背景 */
  --color-bg: #0f0c29;
  --color-bg-warm: #302b63;
  --color-surface: rgba(255, 255, 255, 0.06);

  /* 玻璃态 */
  --glass-bg: rgba(255, 255, 255, 0.06);
  --glass-bg-hover: rgba(255, 255, 255, 0.1);
  --glass-border: rgba(255, 255, 255, 0.15);
  --glass-border-accent: rgba(139, 92, 246, 0.5);
  --glass-blur: blur(20px);
  --glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);

  /* 主色调：紫色 + 青色 */
  --color-accent: #8b5cf6;
  --color-accent-hover: #7c3aed;
  --color-accent-light: rgba(139, 92, 246, 0.15);
  --color-accent-cyan: #06b6d4;

  /* 文字 */
  --color-text: rgba(255, 255, 255, 0.9);
  --color-text-secondary: rgba(255, 255, 255, 0.65);
  --color-text-muted: rgba(255, 255, 255, 0.4);

  /* 边框 */
  --color-border: rgba(255, 255, 255, 0.12);
  --color-border-light: rgba(255, 255, 255, 0.08);

  /* 字体：替换 Playfair Display */
  --font-display: 'Poppins', 'Inter', sans-serif;
  --font-body: 'Inter', 'PingFang SC', sans-serif;

  /* 发光阴影 */
  --shadow-glow: 0 0 40px rgba(139, 92, 246, 0.2);
}
```

**字体引入（替换现有 Google Fonts link）：**
```
Inter + Poppins (wght: 300,400,500,600,700,800)
```

**body 背景：**
```css
body {
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  background-attachment: fixed;
}
```

### 1.2 全局工具类（在 default.vue 全局 style 中新增）

```css
.glass-card {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  transition: all var(--transition-smooth);
}
.glass-card:hover {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-accent);
  box-shadow: var(--glass-shadow), var(--shadow-glow);
  transform: translateY(-4px);
}
```

### 1.3 浮动光球背景

在 `default.vue` template 的 `.app-layout` 内顶部插入光球容器：

```html
<div class="orb-container">
  <div class="orb orb-1"></div>
  <div class="orb orb-2"></div>
  <div class="orb orb-3"></div>
</div>
```

CSS：`position: fixed; inset: 0; z-index: -1; pointer-events: none; overflow: hidden`

三个光球：
- `orb-1`：左上，600px，紫色径向渐变，`animation: orbFloat1 25s infinite`
- `orb-2`：右侧，500px，青色，`animation: orbFloat2 30s infinite reverse`
- `orb-3`：底部，400px，紫粉，`animation: orbFloat3 20s infinite`

### 1.4 导航栏毛玻璃

```css
.navbar {
  background: rgba(15, 12, 41, 0.5);
  border-bottom: 1px solid var(--glass-border);
  backdrop-filter: blur(24px);
}
```

### 1.5 各页面适配顺序

| 文件 | 主要改动 |
|------|---------|
| `pages/index.vue` | `.article-card` 改为 glass-card，标签 pill 改紫色 |
| `pages/articles/[id]/index.vue` | 容器玻璃态，点赞/删除按钮紫色 |
| `pages/users/[id].vue` | `.profile-header` 玻璃态 |
| `pages/login.vue` / `register.vue` | `.auth-form` 玻璃态 |
| `pages/articles/create.vue` / `edit.vue` | 输入框玻璃态，focus 紫色边框 |
| `pages/search.vue` / `pages/tags/[id].vue` | 卡片同步玻璃态 |
| `components/MarkdownEditor.vue` | `theme` 改为 `'dark'`，全局 CSS 覆写编辑器样式 |

---

## 阶段二：社交互动功能

### 2.1 文章收藏（localStorage 方案，无需后端改动）

**新建：`app/composables/useBookmark.js`**

数据结构：`bookmarks_{userId}` → `Array<{ id, title, savedAt }>`

主要函数：
- `isBookmarked(articleId)` → boolean
- `toggleBookmark(articleId, articleTitle)` → void
- `getBookmarks()` → BookmarkItem[]
- `clearBookmarks()` → void

**修改 `app/pages/articles/[id]/index.vue`：**
- 在 like-section 旁加书签按钮（书签 SVG，填充/空心两态）
- 调用 `toggleBookmark(article.id, article.title)`

**修改 `app/pages/users/[id].vue`：**
- `isOwnProfile` 时显示"收藏"标签页
- 展示 `getBookmarks()` 列表，支持取消收藏

### 2.2 文章分享（纯前端）

**修改 `app/pages/articles/[id]/index.vue`：**

```javascript
async function handleShare() {
  try {
    await navigator.clipboard.writeText(window.location.href)
  } catch {
    // 降级：execCommand('copy')
  }
  // 显示 3 秒 toast "链接已复制"
}
```

Toast：页面内局部实现（`ref(false)` + setTimeout），玻璃态样式，固定在底部中央。

### 2.3 个人简介编辑（需要后端改动）

**后端改动（按顺序）：**
1. `entity/User.java`：新增 `@Column(length = 500) private String bio;`
2. `dto/UserProfileDTO.java`：新增 `private String bio;`
3. `service/impl/ProfileServiceImpl.java`：`getProfile()` 中加入 `bio`
4. `controller/UserController.java`：新增 `PUT /{userId}/bio` 接口

**前端改动：**
- `composables/useProfile.js`：新增 `updateBio(userId, bio)` 函数
- `pages/users/[id].vue`：bio 展示区 + `isOwnProfile` 时显示编辑按钮，点击切换 textarea 编辑态

---

## 阶段三：写作体验功能

### 3.1 草稿自动保存

**新建：`app/composables/useDraft.js`**

接收 `draftKey` 参数，提供：
- `saveDraft(title, content, tagIds)` → localStorage
- `loadDraft()` → 草稿对象 或 null
- `clearDraft()`
- `hasDraft()` → boolean

**修改 `app/pages/articles/create.vue`：**

draftKey：`draft_article_${authStore.user?.id}`

```javascript
onMounted(() => {
  draftTimer = setInterval(() => {
    if (form.title || form.content) saveDraft(form.title, form.content, form.tagIds)
  }, 30000)
})
onUnmounted(() => clearInterval(draftTimer))
// 发布成功后 clearDraft()
```

Template 加草稿横幅（显示保存时间，提供"恢复"/"忽略"按钮）。

**修改 `app/pages/articles/[id]/edit.vue`：**
draftKey：`draft_article_edit_${articleId}`，逻辑相同。

### 3.2 阅读时长估算

**新建：`app/utils/readingTime.js`**

```javascript
export function estimateReadingTime(text) {
  // 去除 Markdown 语法后统计
  const cleaned = text.replace(/```[\s\S]*?```/g, '').replace(/[#*_\[\]`>]/g, '')
  const chinese = (cleaned.match(/[一-龥]/g) || []).length
  const english = (cleaned.replace(/[一-龥]/g, '').match(/\b\w+\b/g) || []).length
  return Math.max(1, Math.round(chinese / 250 + english / 200))
}
```

**修改 `app/pages/articles/[id]/index.vue`：**
在 `article-meta` 下方插入：`约 {{ readingMinutes }} 分钟阅读`（⏱ 图标）

### 3.3 写作统计栏

**修改 `app/pages/articles/create.vue` 和 `edit.vue`：**

在 `MarkdownEditor` 下方加统计栏：
```html
<div class="writing-stats">
  <span>📝 {{ wordCount }} 字</span>
  <span>·</span>
  <span>¶ {{ paragraphCount }} 段</span>
  <span>·</span>
  <span>⏱ 约 {{ readingTime }} 分钟</span>
</div>
```

`wordCount`、`paragraphCount`、`readingTime` 均为 `computed`，响应 `form.content` 变化实时更新。

---

## 实现顺序

| Step | 内容 | 文件 |
|------|------|------|
| 1 | CSS 变量系统 + 光球背景 + 导航毛玻璃 | `layouts/default.vue` |
| 2 | 各页面玻璃态适配 | 所有 pages + 组件 |
| 3 | 阅读时长工具函数 | `utils/readingTime.js` |
| 4 | 草稿 Composable + create/edit 接入 | `composables/useDraft.js`, `create.vue`, `edit.vue` |
| 5 | 写作统计栏 | `create.vue`, `edit.vue` |
| 6 | 阅读时长展示 | `articles/[id]/index.vue` |
| 7 | 收藏功能 | `composables/useBookmark.js`, `[id]/index.vue`, `users/[id].vue` |
| 8 | 分享功能 + Toast | `articles/[id]/index.vue` |
| 9 | 后端 bio 接口 + 前端简介编辑 | `User.java` → `UserController.java` → `useProfile.js` → `users/[id].vue` |

---

## 验证方式

1. **UI 验证**：`npm run dev`，打开首页确认深紫背景/光球/毛玻璃卡片/紫色主色
2. **MarkdownEditor**：打开创建文章页，确认编辑器为暗色主题
3. **草稿**：创建文章写几个字，等待30秒或强制调用，刷新页面确认弹出恢复草稿横幅
4. **写作统计**：编辑内容，确认字数/段落/时长实时变化
5. **阅读时长**：打开任意文章详情，确认 meta 行显示阅读时长
6. **收藏**：点击文章详情书签按钮，前往个人主页"收藏"标签页确认
7. **分享**：点击分享按钮，粘贴确认链接正确，toast 显示3秒后消失
8. **Bio**：个人主页点击编辑简介，保存后刷新确认持久化（需后端运行）
