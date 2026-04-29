<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1 class="admin-title">管理标签</h1>
      <p class="admin-subtitle">创建、查看和删除文章标签</p>
    </div>

    <div class="admin-card glass-card">
      <form class="create-form" @submit.prevent="handleCreate">
        <input
          v-model="newTagName"
          type="text"
          class="create-input"
          placeholder="输入新标签名称..."
          maxlength="50"
          :disabled="creating"
        />
        <button type="submit" class="btn btn-primary" :disabled="creating || !newTagName.trim()">
          {{ creating ? '创建中...' : '创建标签' }}
        </button>
      </form>

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <div v-if="tags.length === 0 && !loading" class="empty-state">
        暂无标签，创建第一个吧
      </div>

      <div v-if="tags.length > 0" class="tag-count">共 {{ tags.length }} 个标签</div>

      <ul v-if="tags.length > 0" class="tag-list">
        <li v-for="tag in tags" :key="tag.id" class="tag-item">
          <span class="tag-name">{{ tag.name }}</span>
          <span class="tag-article-count">{{ tag.articleCount ?? 0 }} 篇文章</span>
          <button class="btn-delete" @click="handleDelete(tag)" :disabled="deleting === tag.id">
            {{ deleting === tag.id ? '...' : '删除' }}
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useTag } from '~/composables/useTag'

definePageMeta({ ssr: false, middleware: ['auth', 'admin'] })

const { fetchTags, createTag, deleteTag } = useTag()

const tags = ref([])
const newTagName = ref('')
const loading = ref(true)
const creating = ref(false)
const deleting = ref(null)
const errorMsg = ref('')

async function loadTags() {
  loading.value = true
  try {
    tags.value = await fetchTags()
  } catch (e) {
    errorMsg.value = '加载标签失败'
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  const name = newTagName.value.trim()
  if (!name) return
  creating.value = true
  errorMsg.value = ''
  try {
    await createTag(name)
    newTagName.value = ''
    await loadTags()
  } catch (e) {
    errorMsg.value = e?.response?._data?.message || '创建标签失败'
  } finally {
    creating.value = false
  }
}

async function handleDelete(tag) {
  if (!confirm(`确定要删除标签「${tag.name}」吗？`)) return
  deleting.value = tag.id
  errorMsg.value = ''
  try {
    await deleteTag(tag.id)
    await loadTags()
  } catch (e) {
    errorMsg.value = e?.response?._data?.message || '删除标签失败'
  } finally {
    deleting.value = null
  }
}

onMounted(loadTags)
</script>

<style scoped>
.admin-page {
  max-width: 700px;
  margin: 2rem auto;
  padding: 0 1rem;
}
.admin-header {
  text-align: center;
  margin-bottom: 2rem;
}
.admin-title {
  font-size: 2rem;
  font-family: var(--font-heading, 'Poppins', sans-serif);
  color: var(--color-text);
}
.admin-subtitle {
  color: var(--color-text-muted);
}
.admin-card {
  padding: 2rem;
  border-radius: 16px;
}
.create-form {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}
.create-input {
  flex: 1;
  padding: 0.6rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  font-size: 1rem;
  background: rgba(255,255,255,0.8);
  color: var(--color-text);
  outline: none;
  transition: border-color 0.2s;
}
.create-input:focus {
  border-color: var(--color-accent, #c8956c);
}
.btn {
  padding: 0.6rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-primary {
  background: var(--color-accent, #c8956c);
  color: white;
}
.btn-primary:hover:not(:disabled) {
  background: var(--color-accent-hover, #b07d56);
}
.error-msg {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-bottom: 1rem;
  padding: 0.5rem 0.75rem;
  background: rgba(231, 76, 60, 0.08);
  border-radius: 8px;
}
.empty-state {
  text-align: center;
  color: var(--color-text-muted);
  padding: 2rem 0;
}
.tag-count {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  margin-bottom: 0.75rem;
}
.tag-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.tag-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  background: rgba(255,255,255,0.5);
  border-radius: 10px;
  border: 1px solid var(--color-border);
  transition: background 0.2s;
}
.tag-item:hover {
  background: rgba(255,255,255,0.8);
}
.tag-name {
  font-weight: 600;
  color: var(--color-text);
  flex: 1;
}
.tag-article-count {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}
.btn-delete {
  padding: 0.3rem 0.8rem;
  border: 1px solid #e74c3c;
  border-radius: 8px;
  background: transparent;
  color: #e74c3c;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-delete:hover:not(:disabled) {
  background: #e74c3c;
  color: white;
}
.btn-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
