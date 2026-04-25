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
import { ref, reactive } from 'vue'
import { useArticle } from '~/composables/useArticle'

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

.back-link:hover { color: var(--color-accent); }

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
  color: var(--color-dark);
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
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-dark);
  transition: all var(--transition-fast);
  outline: none;
}

.form-input:focus {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px var(--color-accent-light);
}

.form-input::placeholder { color: var(--color-text-muted); font-weight: 400; }

.form-error {
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  color: #d44;
}

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
  background: var(--color-dark);
  color: white;
}

.btn-primary:hover {
  background: var(--color-accent);
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(200, 149, 108, 0.3);
}

.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.btn-ghost {
  background: transparent;
  color: var(--color-text-secondary);
  border: 1.5px solid var(--color-border);
}

.btn-ghost:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

@media (max-width: 768px) {
  .editor-container { padding: 0 var(--space-lg); }
  .form-actions { flex-direction: column-reverse; }
  .form-actions .btn { width: 100%; }
}
</style>
