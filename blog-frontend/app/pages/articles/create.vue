<template>
  <div class="editor-page">
    <div class="editor-container">
      <NuxtLink to="/" class="back-link">← 返回首页</NuxtLink>
      <h1 class="editor-title">写文章</h1>
      <p class="editor-subtitle">Share your story with the world</p>

      <form class="editor-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="form-label" for="title">标题</label>
          <input
            id="title"
            v-model="form.title"
            type="text"
            class="form-input"
            placeholder="给文章起个好名字..."
            maxlength="200"
          />
          <span v-if="errors.title" class="form-error">{{ errors.title }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">正文</label>
          <MarkdownEditor v-model="form.content" placeholder="写下你的故事..." />
          <span v-if="errors.content" class="form-error">{{ errors.content }}</span>
        </div>

        <div class="form-group">
          <TagSelector v-model="form.tagIds" />
        </div>

        <div class="form-actions">
          <NuxtLink to="/" class="btn btn-ghost">取消</NuxtLink>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? '发布中...' : '✦ 发布文章' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useArticle } from '~/composables/useArticle'

definePageMeta({ middleware: 'auth' })

const { createArticle } = useArticle()

const form = reactive({ title: '', content: '', tagIds: [] })
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
    const article = await createArticle(form.title.trim(), form.content.trim(), form.tagIds)
    navigateTo(`/articles/${article.id}`)
  } catch (err) {
    const msg = err.data?.message || '发布失败，请重试'
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

/* ===== Form ===== */
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
  letter-spacing: 0.03em;
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

.form-input::placeholder {
  color: var(--color-text-muted);
  font-weight: 400;
}

.form-error {
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  color: #d44;
}

/* ===== Actions ===== */
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

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

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
