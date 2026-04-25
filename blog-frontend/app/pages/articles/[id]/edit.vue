<template>
  <div class="editor-page">
    <div class="editor-container">
      <NuxtLink :to="`/articles/${articleId}`" class="back-link">← 返回文章</NuxtLink>

      <!-- Loading -->
      <div v-if="status === 'pending'" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="error-state">
        <p>{{ error.message || '无法加载文章' }}</p>
        <NuxtLink to="/" class="btn btn-primary">返回首页</NuxtLink>
      </div>

      <!-- Edit Form -->
      <template v-else-if="article">
        <h1 class="editor-title">编辑文章</h1>
        <p class="editor-subtitle">Edit your story</p>

        <!-- Draft restore banner -->
        <Transition name="banner-slide">
          <div v-if="showDraftBanner" class="draft-banner">
            <span class="draft-banner-text">发现草稿（{{ draftData ? formatDraftTime(draftData.savedAt) : '' }}），是否恢复？</span>
            <div class="draft-banner-actions">
              <button class="draft-btn draft-btn--restore" @click="restoreDraft">恢复</button>
              <button class="draft-btn draft-btn--discard" @click="discardDraft">忽略</button>
            </div>
          </div>
        </Transition>

        <form class="editor-form" @submit.prevent="handleSubmit">
          <div class="form-group">
            <label class="form-label" for="title">标题</label>
            <input
              id="title"
              v-model="form.title"
              type="text"
              class="form-input"
              placeholder="文章标题..."
              maxlength="200"
            />
            <span v-if="errors.title" class="form-error">{{ errors.title }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">正文</label>
            <MarkdownEditor v-model="form.content" placeholder="文章内容..." />
            <span v-if="errors.content" class="form-error">{{ errors.content }}</span>
            <!-- Writing stats -->
            <div class="writing-stats">
              <span class="stat-item">📝 {{ wordCount }} 字</span>
              <span class="stat-sep">·</span>
              <span class="stat-item">¶ {{ paragraphCount }} 段</span>
              <span class="stat-sep">·</span>
              <span class="stat-item">⏱ 约 {{ readingTime }} 分钟阅读</span>
              <span class="stat-sep" v-if="lastSavedAt">·</span>
              <span class="stat-item stat-saved" v-if="lastSavedAt">已自动保存</span>
            </div>
          </div>

          <div class="form-group">
            <TagSelector v-model="form.tagIds" />
          </div>

          <div class="form-actions">
            <NuxtLink :to="`/articles/${articleId}`" class="btn btn-ghost">取消</NuxtLink>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? '保存中...' : '✦ 保存修改' }}
            </button>
          </div>
        </form>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useArticle } from '~/composables/useArticle'
import { useDraft } from '~/composables/useDraft'
import { estimateReadingTime, countWords, countParagraphs } from '~/utils/readingTime'

definePageMeta({ middleware: 'auth' })

const route = useRoute()
const articleId = route.params.id
const { updateArticle } = useArticle()

const { data: article, error, status } = await useAsyncData(
  `edit-article-${articleId}`,
  () => $fetch(`/api/articles/${articleId}`).then(res => res.data)
)

const form = reactive({
  title: article.value?.title || '',
  content: article.value?.content || '',
  tagIds: article.value?.tags ? article.value.tags.map(t => t.id) : []
})
const errors = reactive({ title: '', content: '' })
const submitting = ref(false)

// Draft
const draftKey = `draft_article_edit_${articleId}`
const { saveDraft, loadDraft, clearDraft, hasDraft, formatDraftTime } = useDraft(draftKey)

const draftData = ref(null)
const showDraftBanner = ref(false)
const lastSavedAt = ref(null)

onMounted(() => {
  const d = loadDraft()
  if (d && (d.title || d.content)) {
    draftData.value = d
    showDraftBanner.value = true
  }
})

function restoreDraft() {
  const d = loadDraft()
  if (d) {
    form.title = d.title || form.title
    form.content = d.content || form.content
    form.tagIds = d.tagIds || form.tagIds
  }
  showDraftBanner.value = false
}

function discardDraft() {
  clearDraft()
  showDraftBanner.value = false
}

let draftTimer = null
onMounted(() => {
  draftTimer = setInterval(() => {
    if (form.title || form.content) {
      saveDraft(form.title, form.content, form.tagIds)
      lastSavedAt.value = new Date()
    }
  }, 30000)
})
onUnmounted(() => {
  clearInterval(draftTimer)
})

// Writing stats
const wordCount = computed(() => countWords(form.content))
const paragraphCount = computed(() => countParagraphs(form.content))
const readingTime = computed(() => estimateReadingTime(form.content))

function validate() {
  errors.title = ''
  errors.content = ''
  let valid = true
  if (!form.title.trim()) {
    errors.title = '标题不能为空'
    valid = false
  }
  if (!form.content.trim()) {
    errors.content = '正文不能为空'
    valid = false
  }
  return valid
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  try {
    await updateArticle(articleId, form.title.trim(), form.content.trim(), form.tagIds)
    clearDraft()
    navigateTo(`/articles/${articleId}`)
  } catch (err) {
    const msg = err.data?.message || '保存失败，请重试'
    alert(msg)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.editor-page {
  min-height: 70vh;
  padding: var(--space-2xl) 0 var(--space-3xl);
}

.editor-container {
  max-width: 720px;
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

.back-link:hover { color: #a78bfa; }

.loading-state,
.error-state {
  text-align: center;
  padding: var(--space-3xl);
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

.editor-title {
  font-family: var(--font-chinese);
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #a78bfa, #06b6d4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.2rem;
}

.editor-subtitle {
  font-family: var(--font-display);
  font-size: 0.9rem;
  font-style: italic;
  color: var(--color-text-muted);
  letter-spacing: 0.05em;
  margin-bottom: var(--space-2xl);
}

/* Draft Banner */
.draft-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: 0.75rem 1.2rem;
  margin-bottom: var(--space-lg);
  background: rgba(6, 182, 212, 0.1);
  border: 1px solid rgba(6, 182, 212, 0.3);
  border-radius: var(--radius-md);
  backdrop-filter: blur(8px);
}

.draft-banner-text {
  font-family: var(--font-chinese);
  font-size: 0.88rem;
  color: #67e8f9;
}

.draft-banner-actions {
  display: flex;
  gap: var(--space-sm);
  flex-shrink: 0;
}

.draft-btn {
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  padding: 0.3rem 0.9rem;
  border-radius: 100px;
  border: none;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.draft-btn--restore {
  background: rgba(6, 182, 212, 0.2);
  color: #67e8f9;
  border: 1px solid rgba(6, 182, 212, 0.4);
}

.draft-btn--restore:hover { background: rgba(6, 182, 212, 0.35); }

.draft-btn--discard {
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-text-muted);
  border: 1px solid var(--glass-border);
}

.banner-slide-enter-active { transition: all 0.3s ease-out; }
.banner-slide-leave-active { transition: all 0.2s ease-in; }
.banner-slide-enter-from, .banner-slide-leave-to { opacity: 0; transform: translateY(-8px); }

.editor-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.form-label {
  font-family: var(--font-chinese);
  font-size: 0.88rem;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.form-input {
  font-family: var(--font-chinese);
  font-size: 1.1rem;
  font-weight: 600;
  padding: var(--space-md) var(--space-lg);
  border: 1.5px solid var(--glass-border);
  border-radius: var(--radius-md);
  background: var(--glass-bg);
  backdrop-filter: blur(8px);
  color: rgba(255, 255, 255, 0.9);
  transition: all var(--transition-fast);
  outline: none;
}

.form-input:focus {
  border-color: var(--color-accent);
  background: rgba(139, 92, 246, 0.08);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15);
}

.form-input::placeholder { color: var(--color-text-muted); font-weight: 400; }

.form-error {
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  color: #f87171;
}

/* Writing stats */
.writing-stats {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.6rem;
  padding: 0 0.2rem;
}

.stat-item {
  font-family: var(--font-chinese);
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.stat-saved { color: #6ee7b7; }
.stat-sep { color: var(--color-border); font-size: 0.75rem; }

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-md);
  padding-top: var(--space-md);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  font-weight: 500;
  padding: 0.7rem 1.8rem;
  border-radius: 100px;
  border: none;
  cursor: pointer;
  transition: all var(--transition-smooth);
  text-decoration: none;
  gap: 0.4rem;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-cyan));
  color: white;
}

.btn-primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(139, 92, 246, 0.4);
}

.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.btn-ghost {
  background: var(--glass-bg);
  backdrop-filter: blur(8px);
  color: var(--color-text-secondary);
  border: 1.5px solid var(--glass-border);
}

.btn-ghost:hover {
  border-color: var(--color-accent);
  color: #a78bfa;
  background: var(--color-accent-light);
}

@media (max-width: 768px) {
  .editor-container { padding: 0 var(--space-lg); }
  .form-actions { flex-direction: column-reverse; }
  .form-actions .btn { width: 100%; }
  .draft-banner { flex-direction: column; align-items: flex-start; }
  .writing-stats { flex-wrap: wrap; }
}
</style>
