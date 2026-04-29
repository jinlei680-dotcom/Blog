<template>
  <div class="profile-page">
    <div class="page-inner">
      <NuxtLink to="/" class="back-link">← 返回首页</NuxtLink>

      <!-- Loading -->
      <div v-if="profileStatus === 'pending'" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- Error -->
      <div v-else-if="profileError" class="empty-state">
        <div class="empty-icon">😔</div>
        <h3 class="empty-title">用户不存在</h3>
        <p class="empty-desc">{{ profileError.message || '无法加载用户信息' }}</p>
        <NuxtLink to="/" class="btn btn-primary btn-sm">返回首页</NuxtLink>
      </div>

      <!-- Profile Content -->
      <template v-else-if="profile">
        <!-- Profile Header -->
        <header class="profile-header">
          <div class="profile-avatar-section">
            <div class="avatar-wrapper">
              <UserAvatar
                :src="profile.avatarUrl"
                :username="profile.username"
                :size="96"
              />
              <button
                v-if="isOwnProfile"
                class="avatar-upload-btn"
                @click="triggerAvatarUpload"
                title="更换头像"
              >
                📷
              </button>
              <input
                v-if="isOwnProfile"
                ref="avatarInput"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                class="hidden-input"
                @change="handleAvatarUpload"
              />
            </div>
            <div class="profile-info">
              <h1 class="profile-username">{{ profile.username }}</h1>
              <p class="profile-joined">
                <span class="joined-icon">✦</span>
                {{ formatDate(profile.createdAt) }} 加入
              </p>
            </div>
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-number">{{ profile.articleCount }}</span>
              <span class="stat-label">篇文章</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-number">{{ profile.totalLikes }}</span>
              <span class="stat-label">获赞</span>
            </div>
          </div>
        </header>

        <!-- Bio -->
        <div class="profile-bio-section">
          <template v-if="editingBio">
            <textarea
              v-model="bioInput"
              class="bio-textarea"
              placeholder="写点什么介绍自己..."
              maxlength="500"
              rows="3"
            ></textarea>
            <div class="bio-edit-actions">
              <button class="bio-btn bio-btn--save" :disabled="bioSaving" @click="saveBio">
                {{ bioSaving ? '保存中...' : '保存' }}
              </button>
              <button class="bio-btn bio-btn--cancel" @click="cancelBioEdit">取消</button>
            </div>
          </template>
          <template v-else>
            <p v-if="profile.bio" class="profile-bio">{{ profile.bio }}</p>
            <p v-else-if="isOwnProfile" class="profile-bio profile-bio--empty">点击"编辑简介"，写点介绍自己的话...</p>
            <button v-if="isOwnProfile" class="bio-edit-btn" @click="startBioEdit">✏ 编辑简介</button>
          </template>
        </div>

        <!-- Avatar uploading toast -->
        <div v-if="avatarUploading" class="upload-toast">上传中...</div>
        <div v-if="avatarError" class="upload-toast upload-toast--error">{{ avatarError }}</div>

        <!-- Bookmarks Section (own profile only) -->
        <section v-if="isOwnProfile && bookmarks.length > 0" class="profile-bookmarks">
          <h2 class="section-title">
            <span class="section-title-text">我的收藏</span>
            <span class="section-title-sub">Saved</span>
          </h2>
          <div class="article-list">
            <article
              v-for="bm in bookmarks"
              :key="bm.id"
              class="article-card"
            >
              <NuxtLink :to="`/articles/${bm.id}`" class="article-card-link">
                <div class="article-card-body">
                  <h3 class="article-card-title">{{ bm.title }}</h3>
                  <div class="article-card-meta">
                    <span class="meta-date">收藏于 {{ formatRelativeTime(bm.savedAt) }}</span>
                  </div>
                </div>
                <button
                  class="remove-bookmark-btn"
                  @click.prevent.stop="removeFromBookmarks(bm.id)"
                  title="取消收藏"
                >
                  ✕
                </button>
              </NuxtLink>
            </article>
          </div>
        </section>

        <!-- Articles Section -->
        <section class="profile-articles">
          <h2 class="section-title">
            <span class="section-title-text">TA 的文章</span>
            <span class="section-title-sub">Articles</span>
          </h2>

          <!-- Loading articles -->
          <div v-if="articlesStatus === 'pending'" class="loading-state loading-state--sm">
            <div class="loading-spinner"></div>
            <p>加载文章中...</p>
          </div>

          <!-- Empty -->
          <div v-else-if="!articles || articles.length === 0" class="empty-state empty-state--compact">
            <div class="empty-icon">📝</div>
            <h3 class="empty-title">还没有文章</h3>
            <p class="empty-desc">这位作者还没有发表文章</p>
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
                    <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
                    <span v-if="article.likeCount > 0" class="meta-divider">·</span>
                    <span v-if="article.likeCount > 0" class="meta-likes">
                      <span class="meta-icon">♥</span> {{ article.likeCount }}
                    </span>
                  </div>
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
          <div v-if="articlesData && articlesData.totalPages > 1" class="pagination">
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
              :disabled="currentPage >= articlesData.totalPages - 1"
              @click="goToPage(currentPage + 1)"
            >
              下一页 →
            </button>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '~/stores/auth'
import { useUpload } from '~/composables/useUpload'
import { useBookmark } from '~/composables/useBookmark'
import { useProfile } from '~/composables/useProfile'

const route = useRoute()
const authStore = useAuthStore()
const { uploadAvatar } = useUpload()

const userId = route.params.id

const isOwnProfile = computed(() => {
  return authStore.isLoggedIn && authStore.user && String(authStore.user.id) === String(userId)
})

// Fetch profile
const { data: profile, error: profileError, status: profileStatus, refresh: refreshProfile } = await useAsyncData(
  `user-profile-${userId}`,
  () => $fetch(`/api/users/${userId}/profile`).then(res => res.data)
)

// Fetch articles
const currentPage = computed(() => {
  const p = parseInt(route.query.page)
  return isNaN(p) ? 0 : p
})

const { data: articlesData, error: articlesError, status: articlesStatus } = await useAsyncData(
  `user-articles-${userId}-${currentPage.value}`,
  () => $fetch(`/api/users/${userId}/articles`, {
    params: { page: currentPage.value, size: 10 }
  }).then(res => res.data),
  { watch: [currentPage] }
)

const articles = computed(() => articlesData.value?.content || [])

const visiblePages = computed(() => {
  if (!articlesData.value) return []
  const total = articlesData.value.totalPages
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
  navigateTo({ path: `/users/${userId}`, query: { page } })
}

// Bio editing
const { updateBio } = useProfile()
const editingBio = ref(false)
const bioInput = ref('')
const bioSaving = ref(false)

function startBioEdit() {
  bioInput.value = profile.value?.bio || ''
  editingBio.value = true
}

function cancelBioEdit() {
  editingBio.value = false
}

async function saveBio() {
  bioSaving.value = true
  try {
    await updateBio(userId, bioInput.value.trim())
    await refreshProfile()
    editingBio.value = false
  } catch (e) {
    alert(e?.data?.message || '保存失败，请重试')
  } finally {
    bioSaving.value = false
  }
}

// Bookmarks (own profile only, client-side)
const { getBookmarks, removeBookmark } = useBookmark(authStore.user?.id)
const bookmarks = ref([])

onMounted(() => {
  if (isOwnProfile.value) {
    bookmarks.value = getBookmarks()
  }
})

function removeFromBookmarks(articleId) {
  removeBookmark(articleId)
  bookmarks.value = getBookmarks()
}

function formatRelativeTime(isoStr) {
  if (!isoStr) return ''
  const d = new Date(isoStr)
  if (isNaN(d.getTime())) return ''
  const diffMin = Math.floor((Date.now() - d) / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin} 分钟前`
  const diffH = Math.floor(diffMin / 60)
  if (diffH < 24) return `${diffH} 小时前`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

// Avatar upload
const avatarInput = ref(null)
const avatarUploading = ref(false)
const avatarError = ref('')

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return

  avatarError.value = ''

  const ALLOWED = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!ALLOWED.includes(file.type)) {
    avatarError.value = '仅支持 JPEG、PNG、GIF、WebP 格式'
    resetAvatarInput()
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    avatarError.value = '文件大小不能超过 5MB'
    resetAvatarInput()
    return
  }

  avatarUploading.value = true
  try {
    const result = await uploadAvatar(file)
    // Refresh profile to get new avatar URL
    await refreshProfile()
  } catch (e) {
    avatarError.value = e?.data?.message || e?.message || '头像上传失败'
  } finally {
    avatarUploading.value = false
    resetAvatarInput()
    setTimeout(() => { avatarError.value = '' }, 3000)
  }
}

function resetAvatarInput() {
  if (avatarInput.value) avatarInput.value.value = ''
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
.profile-page {
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

/* ===== Profile Header ===== */
.profile-header {
  background-image: url('/images/profile-bg-light.jpg');
  background-size: cover;
  background-position: center top;
  border-radius: var(--radius-lg);
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  padding: var(--space-2xl);
  margin-bottom: var(--space-2xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-xl);
  position: relative;
  overflow: hidden;
}

.profile-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(15, 12, 41, 0.6);
  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);
}

.profile-avatar-section {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  position: relative;
  z-index: 1;
}

.avatar-wrapper {
  position: relative;
}

.avatar-upload-btn {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--color-surface);
  border: 2px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.avatar-upload-btn:hover {
  border-color: var(--color-accent);
  background: var(--color-accent-light);
  transform: scale(1.1);
}

.hidden-input {
  display: none;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.profile-username {
  font-family: var(--font-display);
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(135deg, #00b894, #0984e3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

.profile-joined {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.joined-icon {
  color: var(--color-accent);
  font-size: 0.7rem;
}

/* ===== Stats ===== */
.profile-stats {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  position: relative;
  z-index: 1;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-cyan));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.stat-label {
  font-family: var(--font-chinese);
  font-size: 0.8rem;
  color: var(--color-text-muted);
  letter-spacing: 0.05em;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: var(--color-border-light);
}

/* ===== Upload Toast ===== */
.upload-toast {
  text-align: center;
  padding: 0.6rem 1.2rem;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-accent);
  background: var(--color-accent-light);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-lg);
  animation: fadeUp 0.3s ease-out;
}

.upload-toast--error {
  color: #d44;
  background: rgba(221, 68, 68, 0.08);
}

/* ===== Articles Section ===== */
.profile-articles {
  margin-top: var(--space-lg);
}

.section-title {
  display: flex;
  align-items: baseline;
  gap: 0.6rem;
  margin-bottom: var(--space-xl);
}

.section-title-text {
  font-family: var(--font-chinese);
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--color-text);
}

.section-title-sub {
  font-family: var(--font-display);
  font-size: 0.85rem;
  font-style: italic;
  color: var(--color-text-muted);
  letter-spacing: 0.08em;
}

/* ===== Loading & Empty ===== */
.loading-state {
  text-align: center;
  padding: var(--space-3xl);
  color: var(--color-text-muted);
  font-family: var(--font-chinese);
}

.loading-state--sm {
  padding: var(--space-2xl);
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
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-lg);
  border: 1.5px dashed var(--glass-border);
}

.empty-state--compact {
  padding: var(--space-2xl) var(--space-xl);
}

.empty-icon { font-size: 3rem; margin-bottom: var(--space-md); }

.empty-title {
  font-family: var(--font-chinese);
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--color-text);
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
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-cyan));
  color: white;
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-sm {
  padding: 0.6rem 1.5rem;
  font-size: 0.85rem;
}

/* ===== Bio Section ===== */
.profile-bio-section {
  padding: var(--space-lg) var(--space-2xl);
  border-top: 1px solid var(--glass-border);
}

.profile-bio {
  font-family: var(--font-chinese);
  font-size: 0.92rem;
  line-height: 1.8;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-sm);
  white-space: pre-wrap;
}

.profile-bio--empty {
  color: var(--color-text-muted);
  font-style: italic;
}

.bio-edit-btn {
  background: transparent;
  border: 1px solid var(--glass-border);
  border-radius: 100px;
  padding: 0.3rem 0.9rem;
  font-family: var(--font-chinese);
  font-size: 0.78rem;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  margin-top: var(--space-xs, 0.25rem);
}

.bio-edit-btn:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.bio-textarea {
  width: 100%;
  background: var(--color-border-light);
  border: 1.5px solid var(--glass-border);
  border-radius: var(--radius-md);
  padding: var(--space-md);
  font-family: var(--font-chinese);
  font-size: 0.92rem;
  color: var(--color-text);
  resize: vertical;
  outline: none;
  transition: border-color var(--transition-fast);
  box-sizing: border-box;
}

.bio-textarea:focus {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(0, 184, 148, 0.15);
}

.bio-textarea::placeholder {
  color: var(--color-text-muted);
}

.bio-edit-actions {
  display: flex;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}

.bio-btn {
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  padding: 0.35rem 1rem;
  border-radius: 100px;
  border: none;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.bio-btn--save {
  background: var(--color-accent);
  color: white;
}

.bio-btn--save:hover { background: var(--color-accent-hover); }
.bio-btn--save:disabled { opacity: 0.6; cursor: not-allowed; }

.bio-btn--cancel {
  background: var(--glass-bg);
  border: 1.5px solid var(--glass-border);
  color: var(--color-text-secondary);
}

.bio-btn--cancel:hover { border-color: var(--color-text-secondary); }

/* ===== Bookmarks Section ===== */
.profile-bookmarks {
  margin-bottom: var(--space-2xl);
}

.remove-bookmark-btn {
  flex-shrink: 0;
  background: transparent;
  border: 1.5px solid var(--color-border);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.remove-bookmark-btn:hover {
  border-color: #f87171;
  color: #f87171;
  background: rgba(248, 113, 113, 0.1);
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .page-inner { padding: 0 var(--space-lg); }
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--space-xl);
  }
  .profile-avatar-section {
    flex-direction: column;
    align-items: center;
    width: 100%;
    text-align: center;
  }
  .profile-info { align-items: center; }
  .profile-stats {
    width: 100%;
    justify-content: center;
  }
  .article-card-link { padding: var(--space-md) var(--space-lg); }
  .article-card-title { font-size: 1rem; }
  .article-card-arrow { display: none; }
  .pagination { flex-wrap: wrap; }
}
</style>
