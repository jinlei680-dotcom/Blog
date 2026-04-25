<template>
  <div class="comment-list">
    <TransitionGroup name="comment-fade">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
        :class="{ 'comment-item--nested': depth > 0 }"
      >
        <div class="comment-card">
          <div class="comment-header">
            <span class="comment-author">{{ comment.username }}</span>
            <time class="comment-time">{{ formatCommentDate(comment.createdAt) }}</time>
          </div>
          <div class="comment-body">
            <p class="comment-content">{{ comment.content }}</p>
          </div>
          <div class="comment-actions">
            <button
              v-if="authStore.isLoggedIn"
              class="action-btn action-reply"
              @click="toggleReply(comment.id)"
            >
              {{ replyingTo === comment.id ? '取消回复' : '回复' }}
            </button>
            <button
              v-if="authStore.isLoggedIn && (authStore.user?.id === comment.userId || authStore.isAdmin)"
              class="action-btn action-delete"
              @click="handleDelete(comment.id)"
            >
              删除
            </button>
          </div>

          <!-- Reply form -->
          <Transition name="slide-fade">
            <CommentForm
              v-if="replyingTo === comment.id"
              :parent-id="comment.id"
              :reply-to="comment.username"
              @submitted="(payload) => $emit('reply', payload)"
              @cancel="replyingTo = null"
            />
          </Transition>
        </div>

        <!-- Nested children -->
        <div v-if="comment.children && comment.children.length" class="comment-children">
          <CommentList
            :comments="comment.children"
            :depth="depth + 1"
            @reply="(payload) => $emit('reply', payload)"
            @delete="(id) => $emit('delete', id)"
          />
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '~/stores/auth'

const props = defineProps({
  comments: { type: Array, default: () => [] },
  depth: { type: Number, default: 0 }
})

const emit = defineEmits(['reply', 'delete'])

const authStore = useAuthStore()
const replyingTo = ref(null)

function toggleReply(commentId) {
  replyingTo.value = replyingTo.value === commentId ? null : commentId
}

function handleDelete(commentId) {
  if (confirm('确定要删除这条评论吗？')) {
    emit('delete', commentId)
  }
}

function formatCommentDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const now = new Date()
  const diff = now - d
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 7) return `${days} 天前`

  const year = d.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  if (year === now.getFullYear()) {
    return `${month}月${day}日 ${h}:${m}`
  }
  return `${year}年${month}月${day}日 ${h}:${m}`
}
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  position: relative;
}

.comment-item--nested {
  padding-left: var(--space-lg);
  border-left: 2px solid var(--color-border-light);
  margin-left: var(--space-xs);
}

.comment-card {
  padding: var(--space-md) 0;
  border-bottom: 1px solid var(--color-border-light);
}

.comment-item--nested .comment-card {
  border-bottom: none;
  padding: var(--space-sm) 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-xs);
}

.comment-author {
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-accent);
}

.comment-time {
  font-family: var(--font-chinese);
  font-size: 0.78rem;
  color: var(--color-text-muted);
}

.comment-body {
  margin-bottom: var(--space-xs);
}

.comment-content {
  font-family: var(--font-chinese);
  font-size: 0.92rem;
  line-height: 1.7;
  color: var(--color-text);
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: var(--space-sm);
}

.action-btn {
  background: none;
  border: none;
  font-family: var(--font-chinese);
  font-size: 0.8rem;
  cursor: pointer;
  padding: 0.15rem 0.4rem;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.action-reply {
  color: var(--color-text-muted);
}

.action-reply:hover {
  color: var(--color-accent);
  background: var(--color-accent-light);
}

.action-delete {
  color: var(--color-text-muted);
}

.action-delete:hover {
  color: #d44;
  background: rgba(221, 68, 68, 0.06);
}

.comment-children {
  margin-top: 0;
}

/* Transitions */
.comment-fade-enter-active {
  transition: all 0.4s ease-out;
}

.comment-fade-leave-active {
  transition: all 0.25s ease-in;
}

.comment-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.comment-fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (max-width: 768px) {
  .comment-item--nested {
    padding-left: var(--space-md);
  }
}
</style>
