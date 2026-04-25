<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-bg-image"></div>
        <div class="hero-bg-overlay"></div>
        <div class="hero-aurora"></div>
        <div class="hero-gradient"></div>
      </div>
      <div class="hero-content">
        <span class="hero-eyebrow">✦ 探索世界，记录美好</span>
        <h1 class="hero-title">
          <span
            v-for="(w, i) in heroWords"
            :key="i"
            class="hero-word"
            :style="{ animationDelay: (0.2 + i * 0.12) + 's' }"
          >{{ w }}</span>
        </h1>
        <p class="hero-desc">在这里分享旅途中的风景、生活中的感悟，用文字和影像记录每一个值得铭记的瞬间。</p>
        <div class="hero-actions">
          <NuxtLink
            v-if="authStore.isLoggedIn"
            to="/articles/create"
            class="btn btn-primary"
            @click="addRipple"
          >✍ 写文章</NuxtLink>
          <NuxtLink
            v-else
            to="/register"
            class="btn btn-primary"
            @click="addRipple"
          >开始写作</NuxtLink>
          <a href="#articles" class="btn btn-ghost" @click="addRipple">浏览文章 ↓</a>
        </div>
      </div>
    </section>

    <!-- Stats Bar -->
    <section class="stats-bar" ref="statsBarEl">
      <div class="stat-chip">
        <span class="stat-num" ref="statCountEl">{{ statCountDisplay }}</span>
        <span class="stat-lab">篇文章</span>
      </div>
      <div class="stat-sep"></div>
      <div class="stat-chip">
        <span class="stat-num">每天</span>
        <span class="stat-lab">在更新</span>
      </div>
      <div class="stat-sep"></div>
      <div class="stat-chip">
        <span class="stat-num">∞</span>
        <span class="stat-lab">个故事</span>
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
          <NuxtLink v-if="authStore.isLoggedIn" to="/articles/create" class="btn btn-write" @click="addRipple">
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
          <!-- Featured first article -->
          <article v-if="articles[0]" class="article-featured" data-reveal>
            <NuxtLink :to="`/articles/${articles[0].id}`" class="featured-link">
              <div class="featured-body">
                <span class="featured-label">✦ 精选文章</span>
                <h2 class="featured-title">{{ articles[0].title }}</h2>
                <div class="featured-meta">
                  <NuxtLink :to="`/users/${articles[0].authorId}`" class="meta-author" @click.stop>
                    <span class="meta-icon">👤</span>
                    {{ articles[0].authorName }}
                  </NuxtLink>
                  <span class="meta-divider">·</span>
                  <span class="meta-date">{{ formatDate(articles[0].createdAt) }}</span>
                  <template v-if="articles[0].likeCount > 0">
                    <span class="meta-divider">·</span>
                    <span class="meta-likes"><span class="meta-icon">♥</span>{{ articles[0].likeCount }}</span>
                  </template>
                </div>
              </div>
              <div class="featured-arrow">→</div>
            </NuxtLink>
          </article>

          <!-- Rest of articles -->
          <article
            v-for="(article, index) in articles.slice(1)"
            :key="article.id"
            class="article-card"
            :style="{ '--accent': accentColor(article.id), transitionDelay: (index * 60) + 'ms' }"
            data-reveal
            @mousemove="!isTouch && tilt($event, $el)"
            @mouseleave="!isTouch && resetTilt($el)"
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
                  <template v-if="article.likeCount > 0">
                    <span class="meta-divider">·</span>
                    <span class="meta-likes"><span class="meta-icon">♥</span>{{ article.likeCount }}</span>
                  </template>
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
import { computed, ref, onMounted, nextTick } from 'vue'
import { useAuthStore } from '~/stores/auth'

const authStore = useAuthStore()
const route = useRoute()

const heroWords = ['生活', '不止', '眼前的代码，', '还有', '诗和', '远方']

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
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

// Accent colors for article cards
const ACCENTS = ['#8b5cf6','#06b6d4','#f59e0b','#10b981','#f43f5e','#3b82f6','#ec4899']
function accentColor(id) {
  return ACCENTS[Number(id) % ACCENTS.length]
}

// 3D tilt
function tilt(e, el) {
  const r = el.getBoundingClientRect()
  const x = (e.clientY - r.top) / r.height - 0.5
  const y = (e.clientX - r.left) / r.width - 0.5
  el.style.transform = `perspective(700px) rotateX(${-x * 6}deg) rotateY(${y * 6}deg) translateY(-3px)`
}
function resetTilt(el) {
  el.style.transform = ''
}

// Ripple effect
function addRipple(e) {
  const btn = e.currentTarget
  const span = document.createElement('span')
  const { left, top, width, height } = btn.getBoundingClientRect()
  const size = Math.max(width, height)
  Object.assign(span.style, {
    position: 'absolute',
    borderRadius: '50%',
    background: 'rgba(255,255,255,0.25)',
    width: size + 'px',
    height: size + 'px',
    left: (e.clientX - left - size / 2) + 'px',
    top: (e.clientY - top - size / 2) + 'px',
    animation: 'rippleOut 0.6s ease-out forwards',
    pointerEvents: 'none'
  })
  btn.style.position = 'relative'
  btn.style.overflow = 'hidden'
  btn.appendChild(span)
  span.addEventListener('animationend', () => span.remove())
}

// Stats countUp
const statsBarEl = ref(null)
const statCountEl = ref(null)
const statCountDisplay = ref('—')

function countUp(el, target, dur = 1400) {
  const t0 = performance.now()
  const tick = (now) => {
    const p = Math.min((now - t0) / dur, 1)
    el.textContent = Math.floor((1 - (1 - p) ** 3) * target)
    if (p < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

// Intersection Observer for scroll reveal + stats
const isTouch = ref(false)

if (import.meta.client) {
  onMounted(async () => {
    isTouch.value = window.matchMedia('(pointer: coarse)').matches

    await nextTick()

    // Scroll reveal for cards
    const io = new IntersectionObserver((entries) => {
      entries.forEach(e => {
        if (e.isIntersecting) {
          e.target.classList.add('revealed')
          io.unobserve(e.target)
        }
      })
    }, { threshold: 0.1, rootMargin: '0px 0px -40px 0px' })

    document.querySelectorAll('[data-reveal]').forEach(el => io.observe(el))

    // Stats countUp
    const total = pageData.value?.totalElements ?? 0
    const statsIo = new IntersectionObserver((entries) => {
      entries.forEach(e => {
        if (e.isIntersecting && statCountEl.value) {
          countUp(statCountEl.value, total)
          statsIo.disconnect()
        }
      })
    }, { threshold: 0.5 })
    if (statsBarEl.value) statsIo.observe(statsBarEl.value)
  })
}
</script>

<style scoped>
/* ===== Hero ===== */
.hero {
  position: relative;
  min-height: 65vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
}

.hero-bg-image {
  position: absolute;
  inset: 0;
  background: url('/images/hero-bg.jpg') center 30% / cover;
  opacity: 0.25;
}

.hero-bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(15,12,41,0.3) 0%, rgba(15,12,41,0.85) 100%);
}

.hero-aurora {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 50% at 20% 40%, rgba(139,92,246,0.12) 0%, transparent 50%),
    radial-gradient(ellipse 60% 40% at 80% 60%, rgba(6,182,212,0.1) 0%, transparent 50%);
  animation: auroraShift 12s ease-in-out infinite alternate;
}

@keyframes auroraShift {
  0%   { opacity: 0.6; transform: scale(1); }
  100% { opacity: 1;   transform: scale(1.05) rotate(1deg); }
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
}

.hero-eyebrow {
  display: inline-block;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  font-weight: 400;
  color: var(--color-accent);
  letter-spacing: 0.2em;
  margin-bottom: var(--space-md);
  animation: heroEnter 0.8s ease-out both;
}

@keyframes heroEnter {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-title {
  font-family: var(--font-chinese);
  font-weight: 700;
  font-size: clamp(1.8rem, 4vw, 2.8rem);
  line-height: 1.4;
  margin-bottom: var(--space-md);
  color: var(--color-dark);
}

.hero-word {
  display: inline-block;
  margin-right: 0.2em;
  opacity: 0;
  transform: translateY(20px);
  animation: wordEnter 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes wordEnter {
  to { opacity: 1; transform: translateY(0); }
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
  animation: heroEnter 0.8s 0.4s ease-out both;
}

.hero-actions {
  display: flex;
  gap: var(--space-md);
  justify-content: center;
  animation: heroEnter 0.8s 0.6s ease-out both;
}

/* ===== Stats Bar ===== */
.stats-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xl);
  padding: var(--space-xl) var(--space-xl);
  background: rgba(255,255,255,0.04);
  border-top: 1px solid var(--glass-border);
  border-bottom: 1px solid var(--glass-border);
  backdrop-filter: blur(12px);
}

.stat-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.2rem;
}

.stat-num {
  font-family: var(--font-display);
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(135deg, #a78bfa, #06b6d4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.stat-lab {
  font-family: var(--font-chinese);
  font-size: 0.78rem;
  color: var(--color-text-muted);
  letter-spacing: 0.08em;
}

.stat-sep {
  width: 1px;
  height: 40px;
  background: var(--glass-border);
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
  position: relative;
  overflow: hidden;
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
  position: relative;
  overflow: hidden;
}

.btn-write:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.4);
}

.btn-write-icon {
  font-size: 0.75rem;
}

@keyframes rippleOut {
  from { transform: scale(0); opacity: 1; }
  to   { transform: scale(2.5); opacity: 0; }
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

/* ===== Article List ===== */
.article-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

/* Scroll reveal */
[data-reveal] {
  opacity: 0;
  transform: translateY(28px);
  transition: opacity 0.5s ease-out, transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

[data-reveal].revealed {
  opacity: 1;
  transform: translateY(0);
}

/* ===== Featured Article ===== */
.article-featured {
  background: linear-gradient(135deg, rgba(139,92,246,0.12), rgba(6,182,212,0.08));
  border-radius: var(--radius-lg);
  border: 1px solid rgba(139,92,246,0.3);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  transition: all var(--transition-smooth);
  border-left: 4px solid;
  border-image: linear-gradient(to bottom, #8b5cf6, #06b6d4) 1;
  position: relative;
  overflow: hidden;
}

.article-featured::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at top left, rgba(139,92,246,0.06), transparent 60%);
  pointer-events: none;
}

.article-featured:hover {
  border-color: rgba(139,92,246,0.5);
  box-shadow: var(--glass-shadow), var(--shadow-glow);
  transform: translateY(-4px);
}

.featured-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-xl) var(--space-2xl);
  text-decoration: none;
  color: inherit;
  gap: var(--space-lg);
}

.featured-body { flex: 1; min-width: 0; }

.featured-label {
  display: inline-block;
  font-family: var(--font-chinese);
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-accent);
  letter-spacing: 0.15em;
  margin-bottom: 0.6rem;
  padding: 0.2rem 0.7rem;
  background: var(--color-accent-light);
  border-radius: 100px;
}

.featured-title {
  font-family: var(--font-chinese);
  font-size: clamp(1.2rem, 2.5vw, 1.6rem);
  font-weight: 700;
  color: rgba(255,255,255,0.95);
  margin-bottom: 0.7rem;
  line-height: 1.4;
  transition: color var(--transition-fast);
}

.article-featured:hover .featured-title { color: #c4b5fd; }

.featured-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  color: var(--color-text-muted);
}

.featured-arrow {
  font-size: 1.5rem;
  color: var(--color-accent);
  transition: transform var(--transition-fast);
  flex-shrink: 0;
}

.article-featured:hover .featured-arrow { transform: translateX(6px); }

/* ===== Regular Article Cards ===== */
.article-card {
  background: var(--glass-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  transition: all var(--transition-smooth);
  position: relative;
  overflow: hidden;
}

.article-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 15%;
  bottom: 15%;
  width: 3px;
  background: var(--accent, var(--color-accent));
  border-radius: 2px;
  opacity: 0;
  transition: opacity var(--transition-smooth);
}

.article-card:hover::before { opacity: 1; }

.article-card:hover {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-accent);
  box-shadow: var(--glass-shadow), var(--shadow-glow);
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

.pagination-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.pagination-pages { display: flex; gap: 0.3rem; }

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
  .hero { min-height: 55vh; }
  .hero-content { padding: 0 var(--space-lg); }
  .hero-actions { flex-direction: column; align-items: center; }
  .stats-bar { gap: var(--space-lg); padding: var(--space-lg); }
  .stat-num { font-size: 1.4rem; }
  .section-inner { padding: 0 var(--space-lg); }
  .section-header { flex-direction: column; align-items: flex-start; gap: var(--space-md); }
  .article-card-link { padding: var(--space-md) var(--space-lg); }
  .featured-link { padding: var(--space-lg); }
  .article-card-title { font-size: 1rem; }
  .article-card-arrow { display: none; }
  .featured-arrow { display: none; }
  .pagination { flex-wrap: wrap; }
}

/* ===== Reduced motion ===== */
@media (prefers-reduced-motion: reduce) {
  .hero-aurora { animation: none; }
  .hero-word { opacity: 1; transform: none; animation: none; }
  [data-reveal] { opacity: 1; transform: none; }
}
</style>
