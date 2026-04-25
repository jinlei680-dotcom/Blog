<template>
  <div class="home">
    <!-- Hero Section (compact) -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-gradient"></div>
      </div>
      <div class="hero-content">
        <span class="hero-eyebrow">✦ 探索世界，记录美好</span>
        <h1 class="hero-title">
          <span class="hero-title-line">生活不止眼前的代码</span>
          <span class="hero-title-line hero-title-italic">还有诗和远方</span>
        </h1>
        <p class="hero-desc">在这里分享旅途中的风景、生活中的感悟，用文字和影像记录每一个值得铭记的瞬间。</p>
        <div class="hero-actions">
          <NuxtLink v-if="authStore.isLoggedIn" to="/articles/create" class="btn btn-primary">✍ 写文章</NuxtLink>
          <NuxtLink v-else to="/register" class="btn btn-primary">开始写作</NuxtLink>
          <a href="#articles" class="btn btn-ghost">浏览文章 ↓</a>
        </div>
      </div>
    </section>

    <!-- Articles Section -->
    <section id="articles" class="articles-section">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-header-left">
            <h2 class="section-title">最新文章</h2>
            <p class="section-subtitle">Latest Stories</p>
          </div>
          <NuxtLink v-if="authStore.isLoggedIn" to="/articles/create" class="btn btn-write">
            <span class="btn-write-icon">✦</span> 写文章
          </NuxtLink>
        </div>

        <!-- Tag Filter Bar -->
        <TagBar />

        <!-- Loading -->
        <div v-if="status === 'pending'" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="empty-state">
          <div class="empty-icon">⚠️</div>
          <h3 class="empty-title">加载失败</h3>
          <p class="empty-desc">{{ error.message || '无法获取文章列表' }}</p>
          <button class="btn btn-primary btn-sm" @click="refresh()">重试</button>
        </div>

        <!-- Empty -->
        <div v-else-if="!articles || articles.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <h3 class="empty-title">还没有文章</h3>
          <p class="empty-desc">成为第一个分享故事的人吧</p>
          <NuxtLink to="/register" class="btn btn-primary btn-sm">立即注册</NuxtLink>
        </div>

        <!-- Article List -->
        <div v-else class="article-list">
          <article
            v-for="(article, index) in articles"
            :key="article.id"
            class="article-card"
            :style="{ animationDelay: `${index * 0.08}s` }"
          >
            <NuxtLink :to="`/articles/${article.id}`" class="article-card-link">
              <div class="article-card-body">
                <h3 class="article-card-title">{{ article.title }}</h3>
                <div class="article-card-meta">
                  <NuxtLink :to="`/users/${article.authorId}`" class="meta-author" @click.stop>
                    <span class="meta-icon">👤</span>
                    {{ article.authorName }}
                  </NuxtLink>
                  <span class="meta-divider">·</span>
                  <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
                  <span v-if="article.likeCount > 0" class="meta-divider">·</span>
                  <span v-if="article.likeCount > 0" class="meta-likes">
                    <span class="meta-icon">♥</span>
                    {{ article.likeCount }}
                  </span>
                </div>
              </div>
              <div class="article-card-arrow">→</div>
            </NuxtLink>
          </article>
        </div>

        <!-- Pagination -->
        <div v-if="pageData && pageData.totalPages > 1" class="pagination">
          <button
            class="pagination-btn"
            :disabled="currentPage <= 0"
            @click="goToPage(currentPage - 1)"
          >
            ← 上一页
          </button>
          <div class="pagination-pages">
            <button
              v-for="p in visiblePages"
              :key="p"
              class="pagination-page"
              :class="{ active: p === currentPage }"
              @click="goToPage(p)"
            >
              {{ p + 1 }}
            </button>
          </div>
          <button
            class="pagination-btn"
            :disabled="currentPage >= pageData.totalPages - 1"
            @click="goToPage(currentPage + 1)"
          >
            下一页 →
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '~/stores/auth'

const authStore = useAuthStore()
const route = useRoute()

const currentPage = computed(() => {
  const p = parseInt(route.query.page)
  return isNaN(p) ? 0 : p
})

const { data: pageData, error, status, refresh } = await useAsyncData(
  `articles-page-${currentPage.value}`,
  () => $fetch('/api/articles', {
    params: { page: currentPage.value, size: 10 }
  }).then(res => res.data),
  { watch: [currentPage] }
)

const articles = computed(() => pageData.value?.content || [])

const visiblePages = computed(() => {
  if (!pageData.value) return []
  const total = pageData.value.totalPages
  const current = currentPage.value
  const pages = []
  const start = Math.max(0, current - 2)
  const end = Math.min(total - 1, current + 2)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

function goToPage(page) {
  navigateTo({ path: '/', query: { page } })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const year = d.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  return `${year}年${month}月${day}日`
}
</script>

<style scoped>
/* ===== Hero (compact) ===== */
.hero {
  position: relative;
  min-height: 55vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: transparent;
}

.hero-gradient {
  position: absolute;
  top: -30%;
  right: -10%;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.08) 0%, transparent 70%);
  animation: floatSlow 20s ease-in-out infinite;
}

@keyframes floatSlow {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-30px, 20px) scale(1.05); }
  66% { transform: translate(20px, -15px) scale(0.95); }
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 720px;
  padding: 0 var(--space-xl);
  animation: heroEnter 0.8s ease-out;
}

@keyframes heroEnter {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-eyebrow {
  display: inline-block;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  font-weight: 400;
  color: var(--color-accent);
  letter-spacing: 0.2em;
  margin-bottom: var(--space-md);
}

.hero-title {
  font-family: var(--font-chinese);
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: var(--space-md);
}

.hero-title-line {
  display: block;
  font-size: clamp(1.8rem, 4vw, 2.8rem);
  color: var(--color-dark);
}

.hero-title-italic {
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 700;
  background: linear-gradient(135deg, #a78bfa, #06b6d4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: clamp(1.5rem, 3.5vw, 2.4rem);
  margin-top: 0.2rem;
}

.hero-desc {
  font-family: var(--font-chinese);
  font-size: 1rem;
  font-weight: 300;
  color: var(--color-text-secondary);
  line-height: 1.8;
  margin-bottom: var(--space-lg);
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: var(--space-md);
  justify-content: center;
}

/* ===== Buttons ===== */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  font-weight: 500;
  padding: 0.75rem 2rem;
  border-radius: 100px;
  border: none;
  cursor: pointer;
  transition: all var(--transition-smooth);
  text-decoration: none;
  letter-spacing: 0.03em;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-cyan));
  color: white;
}

.btn-primary:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(139, 92, 246, 0.4);
}

.btn-ghost {
  background: var(--glass-bg);
  color: var(--color-text-secondary);
  border: 1.5px solid var(--glass-border);
  backdrop-filter: blur(8px);
}

.btn-ghost:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: var(--color-accent-light);
  transform: translateY(-2px);
}

.btn-sm {
  padding: 0.6rem 1.5rem;
  font-size: 0.85rem;
}

.btn-write {
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-cyan));
  color: white;
  padding: 0.6rem 1.5rem;
  font-size: 0.85rem;
  gap: 0.4rem;
}

.btn-write:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.4);
}

.btn-write-icon {
  font-size: 0.75rem;
}

/* ===== Articles Section ===== */
.articles-section {
  padding: var(--space-3xl) 0;
}

.section-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-xl);
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: var(--space-2xl);
}

.section-header-left {
  text-align: left;
}

.section-title {
  font-family: var(--font-chinese);
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(135deg, #e2d9fa, #a5f3fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.2rem;
}

.section-subtitle {
  font-family: var(--font-display);
  font-size: 0.9rem;
  font-style: italic;
  color: var(--color-text-muted);
  letter-spacing: 0.1em;
}

/* ===== Loading ===== */
.loading-state {
  text-align: center;
  padding: var(--space-3xl);
  color: var(--color-text-muted);
  font-family: var(--font-chinese);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  margin: 0 auto var(--space-md);
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== Empty State ===== */
.empty-state {
  text-align: center;
  padding: var(--space-3xl) var(--space-xl);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-lg);
  border: 1.5px dashed var(--glass-border);
}

.empty-icon { font-size: 3rem; margin-bottom: var(--space-md); }

.empty-title {
  font-family: var(--font-chinese);
  font-size: 1.2rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: var(--space-sm);
}

.empty-desc {
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  color: var(--color-text-muted);
  margin-bottom: var(--space-lg);
}

/* ===== Article Cards ===== */
.article-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.article-card {
  background: var(--glass-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  transition: all var(--transition-smooth);
  animation: cardEnter 0.5s ease-out both;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.article-card:hover {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-accent);
  box-shadow: var(--glass-shadow), var(--shadow-glow);
  transform: translateY(-3px);
}

.article-card-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-lg) var(--space-xl);
  text-decoration: none;
  color: inherit;
  gap: var(--space-lg);
}

.article-card-body {
  flex: 1;
  min-width: 0;
}

.article-card-title {
  font-family: var(--font-chinese);
  font-size: 1.15rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0.5rem;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--transition-fast);
}

.article-card:hover .article-card-title {
  color: #a78bfa;
}

.article-card-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  color: var(--color-text-muted);
}

.meta-icon {
  font-size: 0.75rem;
  margin-right: 0.15rem;
}

.meta-author {
  font-weight: 500;
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
  display: inline-flex;
  align-items: center;
}

.meta-author:hover { color: var(--color-accent); }

.meta-likes { color: var(--color-accent); }

.meta-divider { color: var(--color-border); }

.article-card-arrow {
  font-size: 1.2rem;
  color: var(--color-text-muted);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.article-card:hover .article-card-arrow {
  color: var(--color-accent);
  transform: translateX(4px);
}

/* ===== Pagination ===== */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  margin-top: var(--space-2xl);
}

.pagination-btn {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  padding: 0.5rem 1.2rem;
  border-radius: 100px;
  border: 1.5px solid var(--glass-border);
  background: var(--glass-bg);
  backdrop-filter: blur(8px);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.pagination-btn:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: var(--color-accent-light);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-pages {
  display: flex;
  gap: 0.3rem;
}

.pagination-page {
  width: 36px;
  height: 36px;
  border-radius: 100px;
  border: 1.5px solid var(--glass-border);
  background: var(--glass-bg);
  backdrop-filter: blur(8px);
  color: var(--color-text-secondary);
  font-family: var(--font-body);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.pagination-page:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.pagination-page.active {
  background: var(--color-accent);
  border-color: var(--color-accent);
  color: white;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .hero { min-height: 45vh; }
  .hero-content { padding: 0 var(--space-lg); }
  .hero-actions { flex-direction: column; align-items: center; }
  .section-inner { padding: 0 var(--space-lg); }
  .section-header { flex-direction: column; align-items: flex-start; gap: var(--space-md); }
  .article-card-link { padding: var(--space-md) var(--space-lg); }
  .article-card-title { font-size: 1rem; }
  .article-card-arrow { display: none; }
  .pagination { flex-wrap: wrap; }
}
</style>
