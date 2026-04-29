<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1 class="admin-title">音乐管理</h1>
      <p class="admin-subtitle">上传 mp3 文件，为博客添加背景音乐（访客可完整播放）</p>
    </div>

    <!-- Upload section -->
    <div class="admin-card glass-card">
      <h2 class="card-title">上传新歌曲</h2>
      <form class="upload-form" @submit.prevent="handleUpload">
        <div class="form-row">
          <input
            v-model="uploadName"
            type="text"
            class="input"
            placeholder="歌曲名称"
            maxlength="200"
            :disabled="uploading"
          />
          <label class="file-label" :class="{ uploading }">
            {{ fileLabel }}
            <input
              type="file"
              accept="audio/mp3,audio/wav,audio/ogg,audio/flac,audio/aac,audio/m4a"
              class="file-input"
              @change="onFileChange"
              :disabled="uploading"
            />
          </label>
        </div>

        <div v-if="selectedFile" class="file-info">
          {{ selectedFile.name }} ({{ formatSize(selectedFile.size) }})
        </div>

        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
        <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>

        <button
          type="submit"
          class="btn btn-primary"
          :disabled="uploading || !uploadName.trim() || !selectedFile"
        >
          {{ uploading ? '上传中...' : '上传' }}
        </button>
      </form>
    </div>

    <!-- Song list -->
    <div class="admin-card glass-card">
      <h2 class="card-title">歌曲列表（{{ songs.length }} 首）</h2>

      <div v-if="songs.length === 0 && !loading" class="empty-state">
        暂无歌曲，上传第一首吧
      </div>

      <ul v-if="songs.length > 0" class="song-list">
        <li v-for="song in songs" :key="song.id" class="song-item">
          <div class="song-info">
            <span class="song-icon">♪</span>
            <span class="song-name">{{ song.name }}</span>
          </div>
          <div class="song-meta">
            <span class="song-path">/uploads/music/...</span>
            <span class="song-date">{{ formatDate(song.createdAt) }}</span>
          </div>
          <button
            class="btn-delete"
            @click="handleDelete(song)"
            :disabled="deleting === song.id"
          >
            {{ deleting === song.id ? '...' : '删除' }}
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

definePageMeta({ ssr: false, middleware: ['auth', 'admin'] })

const songs = ref([])
const uploadName = ref('')
const selectedFile = ref(null)
const uploading = ref(false)
const loading = ref(true)
const deleting = ref(null)
const errorMsg = ref('')
const successMsg = ref('')

const fileLabel = computed(() => {
  return selectedFile.value ? '已选择文件' : '选择文件'
})

onMounted(fetchSongs)

async function fetchSongs() {
  loading.value = true
  try {
    const res = await $fetch('/api/music')
    songs.value = res.data || []
  } catch {
    songs.value = []
  } finally {
    loading.value = false
  }
}

function onFileChange(e) {
  selectedFile.value = e.target.files[0] || null
}

function authHeaders() {
  const token = localStorage.getItem('auth_token')
  if (token) {
    return { Authorization: `Bearer ${token}` }
  }
  return {}
}

async function handleUpload() {
  if (!uploadName.value.trim() || !selectedFile.value) return

  uploading.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    const form = new FormData()
    form.append('name', uploadName.value.trim())
    form.append('file', selectedFile.value)

    await $fetch('/api/music', {
      method: 'POST',
      headers: authHeaders(),
      body: form
    })

    successMsg.value = `"${uploadName.value.trim()}" 上传成功`
    uploadName.value = ''
    selectedFile.value = null
    // Reset file input
    const input = document.querySelector('.file-input')
    if (input) input.value = ''
    await fetchSongs()
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch (e) {
    errorMsg.value = e?.response?._data?.message || '上传失败'
  } finally {
    uploading.value = false
  }
}

async function handleDelete(song) {
  if (!confirm(`确定要删除「${song.name}」吗？`)) return
  deleting.value = song.id
  errorMsg.value = ''
  try {
    await $fetch(`/api/music/${song.id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
    await fetchSongs()
  } catch (e) {
    errorMsg.value = e?.response?._data?.message || '删除失败'
  } finally {
    deleting.value = null
  }
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}
</script>

<script>
import { computed } from 'vue'
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
  font-size: 0.9rem;
}
.admin-card {
  padding: 2rem;
  border-radius: 16px;
  margin-bottom: 1.5rem;
}
.card-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 1.25rem;
}

/* Upload */
.upload-form { display: flex; flex-direction: column; gap: 0.75rem; }
.form-row { display: flex; gap: 0.75rem; }
.input {
  flex: 1;
  padding: 0.7rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  font-size: 1rem;
  background: rgba(255,255,255,0.8);
  color: var(--color-text);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.input:focus { border-color: var(--color-accent, #c8956c); }

.file-label {
  padding: 0.7rem 1.25rem;
  border: 2px dashed rgba(0,0,0,0.15);
  border-radius: 10px;
  font-size: 0.85rem;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  display: flex;
  align-items: center;
}
.file-label:hover { border-color: var(--color-accent, #c8956c); color: var(--color-accent, #c8956c); }
.file-label.uploading { opacity: 0.5; cursor: not-allowed; }
.file-input { display: none; }

.file-info {
  font-size: 0.8rem;
  color: var(--color-accent, #c8956c);
  padding: 0.3rem 0.75rem;
  background: rgba(200, 149, 108, 0.08);
  border-radius: 6px;
  display: inline-block;
}

/* Messages */
.error-msg, .success-msg {
  font-size: 0.85rem;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
}
.error-msg { color: #e74c3c; background: rgba(231, 76, 60, 0.08); }
.success-msg { color: #27ae60; background: rgba(39, 174, 96, 0.08); }

/* Song list */
.empty-state { text-align: center; color: var(--color-text-muted); padding: 2rem 0; }
.song-list { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 0.5rem; }
.song-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  background: rgba(255,255,255,0.5);
  border-radius: 10px;
  border: 1px solid var(--color-border);
  transition: background 0.15s;
}
.song-item:hover { background: rgba(255,255,255,0.8); }
.song-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
  min-width: 0;
}
.song-icon { color: var(--color-accent, #c8956c); font-size: 0.9rem; flex-shrink: 0; }
.song-name { font-weight: 600; color: var(--color-text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.song-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}
.song-path, .song-date {
  font-size: 0.75rem;
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
  flex-shrink: 0;
}
.btn-delete:hover:not(:disabled) { background: #e74c3c; color: white; }
.btn-delete:disabled { opacity: 0.5; cursor: not-allowed; }

/* Button */
.btn {
  padding: 0.65rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  align-self: flex-start;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: var(--color-accent, #c8956c); color: white; }
.btn-primary:hover:not(:disabled) { background: var(--color-accent-hover, #b07d56); }

@media (max-width: 640px) {
  .form-row { flex-direction: column; }
  .song-meta, .song-path { display: none; }
}
</style>
