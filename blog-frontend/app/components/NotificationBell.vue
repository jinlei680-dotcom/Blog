<template>
  <div class="notification-bell" ref="bellRef">
    <button class="bell-btn" @click="toggleDropdown" aria-label="通知">
      <svg class="bell-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
      <span v-if="notificationStore.unreadCount > 0" class="bell-badge">
        {{ notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount }}
      </span>
    </button>
    <Transition name="dropdown-fade">
      <NotificationList
        v-if="showDropdown"
        @close="showDropdown = false"
      />
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useNotificationStore } from '~/stores/notification'

const notificationStore = useNotificationStore()
const showDropdown = ref(false)
const bellRef = ref(null)

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
}

function handleClickOutside(e) {
  if (bellRef.value && !bellRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
  display: flex;
  align-items: center;
}

.bell-btn {
  position: relative;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.45rem;
  border-radius: var(--radius-sm, 8px);
  color: var(--color-text-secondary, #6b6b6b);
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bell-btn:hover {
  color: var(--color-accent, #c8956c);
  background: var(--color-accent-light, rgba(200, 149, 108, 0.1));
}

.bell-icon {
  width: 20px;
  height: 20px;
}

.bell-badge {
  position: absolute;
  top: 2px;
  right: 0;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  font-size: 0.65rem;
  font-weight: 600;
  line-height: 16px;
  text-align: center;
  color: #fff;
  background: var(--color-accent, #c8956c);
  border-radius: 100px;
  pointer-events: none;
}

.dropdown-fade-enter-active { transition: all 0.2s ease-out; }
.dropdown-fade-leave-active { transition: all 0.15s ease-in; }
.dropdown-fade-enter-from,
.dropdown-fade-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
