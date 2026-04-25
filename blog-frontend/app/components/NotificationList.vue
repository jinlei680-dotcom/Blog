<template>
  <div class="notification-list">
    <div class="notification-header">
      <span class="notification-title">通知</span>
      <button
        v-if="notifications.length > 0"
        class="mark-all-btn"
        @click="handleMarkAllAsRead"
      >
        全部已读
      </button>
    </div>
    <div class="notification-body">
      <div v-if="loading" class="notification-empty">加载中...</div>
      <div v-else-if="notifications.length === 0" class="notification-empty">暂无通知</div>
      <template v-else>
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ 'notification-item--unread': !item.isRead }"
          @click="handleClick(item)"
        >
          <div class="notification-item-avatar">
            <UserAvatar
              :src="item.senderAvatarUrl"
              :username="item.senderName"
              :size="32"
            />
          </div>
          <div class="notification-item-content">
            <p class="notification-item-text">
              <strong>{{ item.senderName }}</strong>
              {{ typeLabel(item.type) }}
              <span v-if="item.articleTitle" class="notification-article">{{ item.articleTitle }}</span>
            </p>
            <span class="notification-item-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <span v-if="!item.isRead" class="notification-dot"></span>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useNotification } from '~/composables/useNotification'
import { useRouter } from 'vue-router'

const emit = defineEmits(['close'])
const router = useRouter()
const { fetchNotifications, markAsRead, markAllAsRead } = useNotification()

const notifications = ref([])
const loading = ref(true)

function typeLabel(type) {
  switch (type) {
    case 'COMMENT': return '评论了你的文章'
    case 'REPLY': return '回复了你的评论'
    case 'LIKE': return '赞了你的文章'
    default: return ''
  }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 604800) return `${Math.floor(diff / 86400)} 天前`
  return date.toLocaleDateString('zh-CN')
}

async function loadNotifications() {
  loading.value = true
  try {
    const data = await fetchNotifications(0, 10)
    notifications.value = data?.content || []
  } catch (e) {
    notifications.value = []
  } finally {
    loading.value = false
  }
}

async function handleClick(item) {
  if (!item.isRead) {
    await markAsRead(item.id)
    item.isRead = true
  }
  emit('close')
  if (item.articleId) {
    router.push(`/articles/${item.articleId}`)
  }
}

async function handleMarkAllAsRead() {
  await markAllAsRead()
  notifications.value.forEach(n => { n.isRead = true })
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notification-list {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 340px;
  max-height: 420px;
  background: var(--color-surface, #fff);
  border: 1px solid var(--color-border-light, #f0ece7);
  border-radius: var(--radius-md, 12px);
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  z-index: 200;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--color-border-light, #f0ece7);
}

.notification-title {
  font-family: var(--font-chinese, sans-serif);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-text, #2c2c2c);
}

.mark-all-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-family: var(--font-chinese, sans-serif);
  font-size: 0.78rem;
  color: var(--color-accent, #c8956c);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  transition: background 0.2s;
}

.mark-all-btn:hover {
  background: var(--color-accent-light, rgba(200, 149, 108, 0.1));
}

.notification-body {
  overflow-y: auto;
  flex: 1;
}

.notification-empty {
  padding: 2rem 1rem;
  text-align: center;
  font-size: 0.85rem;
  color: var(--color-text-muted, #999);
  font-family: var(--font-chinese, sans-serif);
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 0.65rem;
  padding: 0.7rem 1rem;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
}

.notification-item:hover {
  background: var(--color-bg-warm, #f5f0ea);
}

.notification-item--unread {
  background: rgba(200, 149, 108, 0.04);
}

.notification-item-avatar {
  flex-shrink: 0;
}

.notification-item-content {
  flex: 1;
  min-width: 0;
}

.notification-item-text {
  font-family: var(--font-chinese, sans-serif);
  font-size: 0.82rem;
  color: var(--color-text, #2c2c2c);
  line-height: 1.45;
  margin: 0;
}

.notification-item-text strong {
  font-weight: 600;
}

.notification-article {
  color: var(--color-accent, #c8956c);
  font-weight: 500;
}

.notification-item-time {
  font-size: 0.72rem;
  color: var(--color-text-muted, #999);
  margin-top: 2px;
  display: block;
}

.notification-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-accent, #c8956c);
  flex-shrink: 0;
  margin-top: 6px;
}

@media (max-width: 768px) {
  .notification-list {
    width: 290px;
    right: -60px;
  }
}
</style>
