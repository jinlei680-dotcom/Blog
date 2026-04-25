import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  function setUnreadCount(count) {
    unreadCount.value = count
  }

  function reset() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    setUnreadCount,
    reset
  }
})
