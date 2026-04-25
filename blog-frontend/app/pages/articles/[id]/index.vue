<template>
  <!-- Reading progress bar -->
  <div class="reading-progress">
    <div class="reading-progress-fill" :style="{ width: readProgress + '%' }"></div>
  </div>

  <div class="article-detail">
    <!-- Loading -->
    <div v-if="status === 'pending'" class="detail-loading">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="detail-error">
      <div class="error-icon">😔</div>
      <h2>文章不存在</h2>
      <p>{{ error.message || '无法加载文章' }}</p>
      <NuxtLink to="/" class="btn btn-primary">返回首页</NuxtLink>
    </div>

    <!-- Article Content -->
    <template v-else-if="article">
      <ArticleToc />
      <article class="article-container">
        <!-- Hero Banner -->
        <div class="article-hero-banner" aria-hidden="true">
          <div class="article-hero-img"></div>
          <div class="article-hero-fade"></div>
        </div>
        <!-- Article Header -->
        <header class="article-header">
          <NuxtLink to="/" class="back-link">← 返回文章列表</NuxtLink>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <NuxtLink :to="`/users/${article.authorId}`" class="meta-author meta-author-link">
              <span class="meta-icon">👤</span>
              {{ article.authorName }}
            </NuxtLink>
            <span class="meta-divider">·</span>
            <time class="meta-date">{{ formatDate(article.createdAt) }}</time>
            <template v-if="article.updatedAt && article.updatedAt !== article.createdAt">
              <span class="meta-divider">·</span>
              <span class="meta-updated">编辑于 {{ formatDate(article.updatedAt) }}</span>
            </template>
            <span class="meta-divider">·</span>
            <span class="meta-reading-time">⏱ 约 {{ readingMinutes }} 分钟阅读</span>
          </div>
          <!-- Author / Admin Actions -->
          <div v-if="article.tags && article.tags.length" class="article-tags">
            <NuxtLink
              v-for="tag in article.tags"
              :key="tag.id"
              :to="`/tags/${tag.id}`"
              class="article-tag-pill"
            >
              {{ tag.name }}
            </NuxtLink>
          </div>
          <div v-if="canEditOrDelete" class="article-actions">
            <NuxtLink :to="`/articles/${article.id}/edit`" class="btn btn-edit">
              ✏️ 编辑
            </NuxtLink>
            <button class="btn btn-delete" @click="handleDelete">
              🗑️ 删除
            </button>
          </div>
        </header>

        <!-- Article Body -->
        <div class="article-body">
          <MarkdownRenderer :content="article.content" />
        </div>

        <!-- Article Footer -->
        <footer class="article-footer">
          <div class="like-section">
            <button
              class="like-btn"
              :class="{ 'like-btn--liked': liked }"
              :disabled="likeLoading"
              @click="handleLike"
              :aria-label="liked ? '取消点赞' : '点赞'"
            >
              <span class="like-heart" :class="{ 'like-heart--bounce': likeBounce }">
                <svg
                  class="heart-icon"
                  viewBox="0 0 24 24"
                  width="22"
                  height="22"
                  :fill="liked ? '#a78bfa' : 'none'"
                  :stroke="liked ? '#a78bfa' : 'currentColor'"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
                </svg>
              </span>
              <span class="like-count-num">{{ likeCount }}</span>
              <span class="like-label">喜欢</span>
            </button>

            <!-- Bookmark button -->
            <button
              class="action-btn bookmark-btn"
              :class="{ 'action-btn--active': isBookmarkedState }"
              @click="handleBookmark"
              :aria-label="isBookmarkedState ? '取消收藏' : '收藏'"
            >
              <svg viewBox="0 0 24 24" width="18" height="18"
                :fill="isBookmarkedState ? '#a78bfa' : 'none'"
                :stroke="isBookmarkedState ? '#a78bfa' : 'currentColor'"
                stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
              </svg>
              <span>{{ isBookmarkedState ? '已收藏' : '收藏' }}</span>
            </button>

            <!-- Share button -->
            <button class="action-btn share-btn" @click="handleShare">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
              <span>分享</span>
            </button>

            <span class="meta-divider">·</span>
            <span class="comment-count">💬 {{ commentCount }} 条评论</span>
          </div>
        </footer>

        <!-- Share toast -->
        <Teleport to="body">
          <Transition name="toast">
            <div v-if="showShareToast" class="share-toast">🔗 链接已复制到剪贴板</div>
          </Transition>
        </Teleport>

        <!-- Comment Section -->
        <section class="comment-section">
          <h2 class="comment-section-title">评论 ({{ commentCount }})</h2>

          <!-- Top-level comment form -->
          <CommentForm @submitted="handleAddComment" />

          <!-- Loading comments -->
          <div v-if="commentsLoading" class="comments-loading">
            <div class="loading-spinner loading-spinner--sm"></div>
            <span>加载评论中...</span>
          </div>

          <!-- Comment list -->
          <div v-else-if="comments.length" class="comments-list-wrapper">
            <CommentList
              :comments="comments"
              @reply="handleAddComment"
              @delete="handleDeleteComment"
            />
          </div>

          <!-- Empty state -->
          <div v-else class="comments-empty">
            <span class="empty-icon">💬</span>
            <p>还没有评论，来说两句吧</p>
          </div>
        </section>
      </article>
    </template>

    <!-- Back to top button -->
    <Teleport to="body">
      <Transition name="backtop">
        <button v-if="showBackTop" class="back-to-top" @click="scrollToTop" aria-label="回到顶部">↑</button>
      </Transition>
    </Teleport>

    <!-- Delete Confirmation Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
          <div class="modal-content">
            <h3 class="modal-title">确认删除</h3>
            <p class="modal-desc">确定要删除这篇文章吗？此操作不可撤销，文章的评论和点赞也将被删除。</p>
            <div class="modal-actions">
              <button class="btn btn-ghost-sm" @click="showDeleteModal = false">取消</button>
              <button class="btn btn-danger" :disabled="deleting" @click="confirmDelete">
                {{ deleting ? '删除中...' : '确认删除' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '~/stores/auth'
import { useArticle } from '~/composables/useArticle'
import { useComment } from '~/composables/useComment'
import { useLike } from '~/composables/useLike'
import { useBookmark } from '~/composables/useBookmark'
import { estimateReadingTime } from '~/utils/readingTime'

const route = useRoute()
const authStore = useAuthStore()
const { deleteArticle } = useArticle()
const { fetchComments, createComment, deleteComment } = useComment()
const { toggleLike } = useLike()

const articleId = route.params.id

const { data: article, error, status } = await useAsyncData(
  `article-${articleId}`,
  () => $fetch(`/api/articles/${articleId}`).then(res => res.data)
)

const isAuthor = computed(() => {
  return authStore.isLoggedIn && article.value && authStore.user?.id === article.value.authorId
})

const canEditOrDelete = computed(() => {
  return isAuthor.value || authStore.isAdmin
})

// Reading time
const readingMinutes = computed(() => {
  return article.value ? estimateReadingTime(article.value.content) : 1
})

// Bookmark
const { toggleBookmark, syncState } = useBookmark(authStore.user?.id)
const isBookmarkedState = ref(false)

watch(() => article.value, (val) => {
  if (val) {
    isBookmarkedState.value = syncState(val.id).value
  }
}, { immediate: true })

function handleBookmark() {
  if (!article.value) return
  isBookmarkedState.value = toggleBookmark(article.value.id, article.value.title)
}

// Share
const showShareToast = ref(false)
let shareToastTimer = null

async function handleShare() {
  const url = window.location.href
  try {
    await navigator.clipboard.writeText(url)
  } catch {
    const el = document.createElement('textarea')
    el.value = url
    el.style.position = 'fixed'
    el.style.opacity = '0'
    document.body.appendChild(el)
    el.select()
    document.execCommand('copy')
    document.body.removeChild(el)
  }
  showShareToast.value = true
  clearTimeout(shareToastTimer)
  shareToastTimer = setTimeout(() => { showShareToast.value = false }, 3000)
}

// Like state
const liked = ref(false)
const likeCount = ref(0)
const likeLoading = ref(false)
const likeBounce = ref(false)

// Initialize like state from article data
watch(() => article.value, (val) => {
  if (val) {
    liked.value = !!val.likedByCurrentUser
    likeCount.value = val.likeCount || 0
  }
}, { immediate: true })

async function handleLike() {
  if (!authStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  if (likeLoading.value) return

  // Optimistic update
  const prevLiked = liked.value
  const prevCount = likeCount.value
  liked.value = !prevLiked
  likeCount.value = prevLiked ? prevCount - 1 : prevCount + 1
  likeBounce.value = true
  setTimeout(() => { likeBounce.value = false }, 400)

  likeLoading.value = true
  try {
    const data = await toggleLike(articleId)
    liked.value = data.liked
    likeCount.value = data.likeCount
  } catch (err) {
    // Revert on failure
    liked.value = prevLiked
    likeCount.value = prevCount
    console.error('Like failed:', err)
  } finally {
    likeLoading.value = false
  }
}

// Comments
const comments = ref([])
const commentsLoading = ref(false)

const commentCount = computed(() => {
  function countAll(list) {
    let total = 0
    for (const c of list) {
      total += 1
      if (c.children && c.children.length) {
        total += countAll(c.children)
      }
    }
    return total
  }
  return countAll(comments.value)
})

async function loadComments() {
  commentsLoading.value = true
  try {
    comments.value = await fetchComments(articleId)
  } catch (err) {
    console.error('Failed to load comments:', err)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

async function handleAddComment(payload) {
  try {
    await createComment(articleId, payload.content, payload.parentId)
    await loadComments()
  } catch (err) {
    alert(err.data?.message || '评论失败，请重试')
  }
}

async function handleDeleteComment(commentId) {
  try {
    await deleteComment(commentId)
    await loadComments()
  } catch (err) {
    alert(err.data?.message || '删除失败，请重试')
  }
}

// Reading progress + back-to-top
const readProgress = ref(0)
const showBackTop = ref(false)

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  loadComments()

  window.addEventListener('scroll', () => {
    const h = document.documentElement.scrollHeight - window.innerHeight
    readProgress.value = h > 0 ? Math.min(100, window.scrollY / h * 100) : 0
    showBackTop.value = window.scrollY > 400
  }, { passive: true })
})

// Markdown rendering is now handled by MarkdownRenderer component

const showDeleteModal = ref(false)
const deleting = ref(false)

function handleDelete() {
  showDeleteModal.value = true
}

async function confirmDelete() {
  deleting.value = true
  try {
    await deleteArticle(articleId)
    navigateTo('/')
  } catch (err) {
    alert(err.data?.message || '删除失败')
  } finally {
    deleting.value = false
    showDeleteModal.value = false
  }
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
/* ===== Reading Progress ===== */
.reading-progress {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba(255,255,255,0.06);
  z-index: 200;
}

.reading-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-accent), var(--color-accent-cyan));
  transition: width 0.12s linear;
}

/* ===== Back to Top ===== */
.back-to-top {
  position: fixed;
  bottom: 7rem;
  right: 1.8rem;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--glass-bg-strong);
  border: 1px solid var(--glass-border-accent);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  color: var(--color-accent);
  font-size: 1.1rem;
  cursor: pointer;
  z-index: 90;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-glow);
}

.back-to-top:hover {
  background: var(--color-accent);
  color: white;
  transform: translateY(-3px);
}

.backtop-enter-active, .backtop-leave-active { transition: all 0.3s; }
.backtop-enter-from, .backtop-leave-to { opacity: 0; transform: translateY(10px); }

/* ===== Article Hero Banner ===== */
.article-hero-banner {
  height: 220px;
  margin: 0 calc(-1 * var(--space-xl)) var(--space-2xl);
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.article-hero-img {
  position: absolute;
  inset: 0;
  background: url('/images/article-hero.jpg') center / cover;
  opacity: 0.2;
}

.article-hero-fade {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 0%, #0f0c29 100%);
}

.article-detail {
  min-height: 60vh;
}

/* ===== Loading & Error ===== */
.detail-loading,
.detail-error {
  text-align: center;
  padding: var(--space-3xl) var(--space-xl);
  font-family: var(--font-chinese);
  color: var(--color-text-muted);
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

.error-icon { font-size: 3rem; margin-bottom: var(--space-md); }

.detail-error h2 {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: var(--space-sm);
}

.detail-error p {
  margin-bottom: var(--space-lg);
}

/* ===== Article Container ===== */
.article-container {
  max-width: 780px;
  margin: 0 auto;
  padding: var(--space-2xl) var(--space-xl) var(--space-3xl);
  animation: fadeUp 0.6s ease-out;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== Back Link ===== */
.back-link {
  display: inline-block;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin-bottom: var(--space-xl);
  transition: color var(--transition-fast);
}

.back-link:hover { color: var(--color-accent); }

/* ===== Article Header ===== */
.article-header {
  margin-bottom: var(--space-2xl);
  padding-bottom: var(--space-xl);
  border-bottom: 1px solid var(--color-border-light);
}

.article-title {
  font-family: var(--font-chinese);
  font-size: clamp(1.8rem, 4vw, 2.6rem);
  font-weight: 700;
  background: linear-gradient(135deg, rgba(255,255,255,0.95), #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.4;
  margin-bottom: var(--space-lg);
  letter-spacing: -0.01em;
}

.article-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  font-family: var(--font-chinese);
  font-size: 0.88rem;
  color: var(--color-text-muted);
}

.meta-icon { margin-right: 0.15rem; }
.meta-author { font-weight: 500; color: var(--color-text-secondary); }
.meta-author-link {
  text-decoration: none;
  transition: color var(--transition-fast);
}
.meta-author-link:hover { color: var(--color-accent); }
.meta-updated { font-style: italic; }
.meta-divider { color: var(--color-border); }
.meta-reading-time { color: var(--color-text-muted); }

/* ===== Article Tags ===== */
.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: var(--space-md);
}

.article-tag-pill {
  display: inline-flex;
  align-items: center;
  padding: 0.3rem 0.9rem;
  font-family: var(--font-chinese);
  font-size: 0.8rem;
  font-weight: 500;
  color: #a78bfa;
  background: rgba(139, 92, 246, 0.15);
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: 100px;
  text-decoration: none;
  transition: all var(--transition-fast);
  backdrop-filter: blur(8px);
}

.article-tag-pill:hover {
  background: var(--color-accent);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.35);
  border-color: transparent;
}

/* ===== Author Actions ===== */
.article-actions {
  display: flex;
  gap: var(--space-sm);
  margin-top: var(--space-lg);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  font-weight: 500;
  padding: 0.5rem 1.2rem;
  border-radius: 100px;
  border: none;
  cursor: pointer;
  transition: all var(--transition-fast);
  text-decoration: none;
  gap: 0.3rem;
}

.btn-primary {
  background: var(--color-dark);
  color: white;
}

.btn-primary:hover {
  background: var(--color-accent);
}

.btn-edit {
  background: rgba(139, 92, 246, 0.15);
  color: #a78bfa;
  border: 1.5px solid rgba(139, 92, 246, 0.4);
  backdrop-filter: blur(8px);
}

.btn-edit:hover {
  background: var(--color-accent);
  color: white;
  border-color: transparent;
}

.btn-delete {
  background: transparent;
  color: #d44;
  border: 1.5px solid #ecc;
}

.btn-delete:hover {
  background: #d44;
  color: white;
  border-color: #d44;
}

/* ===== Article Body ===== */
.article-body {
  margin-bottom: var(--space-2xl);
}

.article-content {
  font-family: var(--font-chinese);
  font-size: 1.05rem;
  line-height: 2;
  color: var(--color-text);
  letter-spacing: 0.02em;
  word-break: break-word;
}

/* ===== Article Footer ===== */
.article-footer {
  padding-top: var(--space-xl);
  border-top: 1px solid var(--color-border-light);
}

.like-section {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  background: none;
  border: 1.5px solid var(--color-border);
  border-radius: 100px;
  padding: 0.5rem 1.2rem;
  cursor: pointer;
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  user-select: none;
}

.like-btn:hover {
  border-color: #a78bfa;
  color: #a78bfa;
  background: rgba(139, 92, 246, 0.1);
}

.like-btn--liked {
  border-color: #a78bfa;
  color: #a78bfa;
  background: rgba(139, 92, 246, 0.12);
}

.like-btn--liked:hover {
  background: rgba(139, 92, 246, 0.2);
}

.like-btn:disabled {
  opacity: 0.7;
  cursor: default;
}

.like-heart {
  display: inline-flex;
  align-items: center;
  transition: transform 0.2s ease;
}

.like-heart--bounce {
  animation: heartBounce 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes heartBounce {
  0% { transform: scale(1); }
  30% { transform: scale(1.35); }
  60% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

.heart-icon {
  display: block;
  transition: all 0.2s ease;
}

.like-count-num {
  font-weight: 600;
  min-width: 1ch;
}

.like-label {
  font-weight: 400;
}

.comment-count {
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.meta-divider {
  color: var(--color-border);
}

/* ===== Comment Section ===== */
.comment-section {
  margin-top: var(--space-2xl);
  padding-top: var(--space-xl);
  border-top: 1px solid var(--color-border-light);
}

.comment-section-title {
  font-family: var(--font-chinese);
  font-size: 1.2rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: var(--space-lg);
}

.comments-loading {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-lg) 0;
  font-family: var(--font-chinese);
  font-size: 0.88rem;
  color: var(--color-text-muted);
}

.loading-spinner--sm {
  width: 18px;
  height: 18px;
  border-width: 2px;
}

.comments-empty {
  text-align: center;
  padding: var(--space-2xl) 0;
  color: var(--color-text-muted);
  font-family: var(--font-chinese);
}

.empty-icon {
  font-size: 2rem;
  display: block;
  margin-bottom: var(--space-sm);
}

.comments-empty p {
  font-size: 0.9rem;
}

/* ===== Delete Modal ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: var(--space-xl);
}

.modal-content {
  background: rgba(15, 12, 41, 0.92);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: var(--space-xl) var(--space-2xl);
  max-width: 420px;
  width: 100%;
  box-shadow: var(--glass-shadow), var(--shadow-glow);
}

.modal-title {
  font-family: var(--font-chinese);
  font-size: 1.2rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: var(--space-sm);
}

.modal-desc {
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin-bottom: var(--space-xl);
}

.modal-actions {
  display: flex;
  gap: var(--space-sm);
  justify-content: flex-end;
}

.btn-ghost-sm {
  background: transparent;
  color: var(--color-text-secondary);
  border: 1.5px solid var(--color-border);
  padding: 0.5rem 1.2rem;
  border-radius: 100px;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-ghost-sm:hover {
  border-color: var(--color-text-secondary);
}

.btn-danger {
  background: #d44;
  color: white;
  padding: 0.5rem 1.2rem;
  border-radius: 100px;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  border: none;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-danger:hover { background: #c33; }
.btn-danger:disabled { opacity: 0.6; cursor: not-allowed; }

/* ===== Action Buttons (Bookmark / Share) ===== */
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  background: var(--glass-bg);
  border: 1.5px solid var(--glass-border);
  backdrop-filter: blur(8px);
  border-radius: 100px;
  padding: 0.5rem 1.1rem;
  cursor: pointer;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  user-select: none;
}

.action-btn:hover {
  border-color: rgba(139, 92, 246, 0.5);
  color: #a78bfa;
  background: rgba(139, 92, 246, 0.1);
}

.action-btn--active {
  border-color: rgba(139, 92, 246, 0.6);
  color: #a78bfa;
  background: rgba(139, 92, 246, 0.15);
}

/* ===== Share Toast ===== */
.share-toast {
  position: fixed;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 12, 41, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(139, 92, 246, 0.4);
  border-radius: 100px;
  padding: 0.65rem 1.5rem;
  font-family: var(--font-chinese);
  font-size: 0.88rem;
  color: #a78bfa;
  z-index: 300;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 20px rgba(139, 92, 246, 0.2);
  white-space: nowrap;
}

.toast-enter-active { transition: all 0.3s ease-out; }
.toast-leave-active { transition: all 0.25s ease-in; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(12px); }

/* Modal transitions */
.modal-enter-active { transition: all 0.25s ease-out; }
.modal-leave-active { transition: all 0.2s ease-in; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal-content { transform: scale(0.95); }

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .article-container { padding: var(--space-lg) var(--space-lg) var(--space-2xl); }
  .article-content { font-size: 1rem; line-height: 1.9; }
  .article-actions { flex-wrap: wrap; }
}
</style>
