<template>
  <div class="tag-articles-page">
    <div class="page-inner">
      <NuxtLink to="/" class="back-link">← 返回首页</NuxtLink>

      <!-- Loading -->
      <div v-if="status === 'pending'" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="empty-state">
        <div class="empty-icon">😔</div>
        <h3 class="empty-title">加载失败</h3>
        <p class="empty-desc">{{ error.message || '无法加载文章列表' }}</p>
        <NuxtLink to="/" class="btn btn-primary btn-sm">返回首页</NuxtLink>
      </div>

      <!-- Content -->
      <template v-else>
        <header class="page-header">
          <span class="page-tag-pill">🏷️ {{ tagName }}</span>
          <h1 class="page-title">{{ tagName }}</h1>
          <p class="page-subtitle">共 {{ totalElements }} 篇文章</p>
        </header>

        <!-- Empty -->
        <div v-if="!articles || articles.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <h3 class="empty-title">暂无文章</h3>
          <p class="empty-desc">该标签下还没有文章</p>
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
                    <span class="meta-icon">♥</span> {{ article.likeCount }}
                  </span>
                </div>
                <!-- Article tags -->
                <div v-if="article.tags && article.tags.length" class="article-card-tags">
                  <NuxtLink
                    v-for="t in article.tags"
                    :key="t.id"
                    :to="`/tags/${t.id}`"
                    class="mini-tag"
                    @click.stop
                  >
                    {{ t.name }}
                  </NuxtLink>
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
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const route = useRoute()
const tagId = route.params.id

const currentPage = computed(() => {
  const p = parseInt(route.query.page)
  return isNaN(p) ? 0 : p
})

const { data: pageData, error, status } = await useAsyncData(
  `tag-articles-${tagId}-${currentPage.value}`,
  () => $fetch(`/api/tags/${tagId}/articles`, {
    params: { page: currentPage.value, size: 10 }
  }).then(res => res.data),
  { watch: [currentPage] }
)

const articles = computed(() => pageData.value?.content || [])
const totalElements = computed(() => pageData.value?.totalElements || 0)

// Extract tag name from the first article's tags, or use a fallback
const tagName = computed(() => {
  if (articles.value.length > 0 && articles.value[0].tags) {
    const found = articles.value[0].tags.find(t => String(t.id) === String(tagId))
    if (found) return found.name
  }
  return `标签 #${tagId}`
})

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
  navigateTo({ path: `/tags/${tagId}`, query: { page } })
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
.tag-articles-page {
  min-height: 60vh;
  padding: var(--space-2xl) 0 var(--space-3xl);
}

.page-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-xl);
  animation: fadeUp 0.5s ease-out;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.back-link {
  display: inline-block;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin-bottom: var(--space-xl);
  transition: color var(--transition-fast);
}

.back-link:hover { color: var(--color-accent); }

/* Header */
.page-header {
  margin-bottom: var(--space-2xl);
}

.page-tag-pill {
  display: inline-block;
  padding: 0.35rem 1rem;
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  font-weight: 500;
  color: var(--color-accent);
  background: var(--color-accent-light);
  border-radius: 100px;
  margin-bottom: var(--space-md);
}

.page-title {
  font-family: var(--font-chinese);
  font-size: clamp(1.6rem, 3.5vw, 2.2rem);
  font-weight: 700;
  color: var(--color-dark);
  margin-bottom: 0.3rem;
}

.page-subtitle {
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

/* Loading & Empty */
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

@keyframes spin { to { transform: rotate(360deg); } }

.empty-state {
  text-align: center;
  padding: var(--space-3xl) var(--space-xl);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1.5px dashed var(--color-border);
}

.empty-icon { font-size: 3rem; margin-bottom: var(--space-md); }

.empty-title {
  font-family: var(--font-chinese);
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--color-dark);
  margin-bottom: var(--space-sm);
}

.empty-desc {
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  color: var(--color-text-muted);
  margin-bottom: var(--space-lg);
}

/* Article Cards */
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
  color: var(--color-text);
  margin-bottom: 0.5rem;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--transition-fast);
}

.article-card:hover .article-card-title {
  color: var(--color-accent);
}

.article-card-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  color: var(--color-text-muted);
}

.meta-icon { font-size: 0.75rem; margin-right: 0.15rem; }
.meta-author {
  font-weight: 500;
  color: var(--color-text-secondary);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  transition: color var(--transition-fast);
}
.meta-author:hover { color: var(--color-accent); }
.meta-likes { color: var(--color-accent); }
.meta-divider { color: var(--color-border); }

.article-card-tags {
  display: flex;
  gap: 0.4rem;
  margin-top: 0.5rem;
  flex-wrap: wrap;
}

.mini-tag {
  display: inline-block;
  padding: 0.15rem 0.6rem;
  font-family: var(--font-chinese);
  font-size: 0.72rem;
  font-weight: 500;
  color: var(--color-accent);
  background: var(--color-accent-light);
  border-radius: 100px;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.mini-tag:hover {
  background: var(--color-accent);
  color: white;
}

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

/* Pagination */
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
  border: 1.5px solid var(--color-border);
  background: var(--color-surface);
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
  border: 1.5px solid var(--color-border);
  background: var(--color-surface);
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
}

.btn-primary {
  background: var(--color-dark);
  color: white;
}

.btn-primary:hover {
  background: var(--color-accent);
}

.btn-sm {
  padding: 0.6rem 1.5rem;
  font-size: 0.85rem;
}

@media (max-width: 768px) {
  .page-inner { padding: 0 var(--space-lg); }
  .article-card-link { padding: var(--space-md) var(--space-lg); }
  .article-card-title { font-size: 1rem; }
  .article-card-arrow { display: none; }
  .pagination { flex-wrap: wrap; }
}
</style>
