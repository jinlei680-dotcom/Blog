import { useAuthStore } from '~/stores/auth'
import { useNotificationStore } from '~/stores/notification'

let pollingTimer = null

export function useNotification() {
  const authStore = useAuthStore()
  const notificationStore = useNotificationStore()

  async function fetchUnreadCount() {
    if (!authStore.isLoggedIn) return
    try {
      const res = await $fetch('/api/notifications/unread-count', {
        headers: { Authorization: `Bearer ${authStore.token}` }
      })
      if (res && res.data !== undefined) {
        notificationStore.setUnreadCount(res.data)
      }
    } catch (e) {
      // silent fail — polling should not break the app
    }
  }

  function startPolling() {
    if (!authStore.isLoggedIn) return
    fetchUnreadCount()
    if (pollingTimer) clearInterval(pollingTimer)
    pollingTimer = setInterval(fetchUnreadCount, 30000)
  }

  function stopPolling() {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
    notificationStore.reset()
  }

  async function fetchNotifications(page = 0, size = 10) {
    const res = await $fetch(`/api/notifications?page=${page}&size=${size}`, {
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    return res.data
  }

  async function markAsRead(notificationId) {
    await $fetch(`/api/notifications/${notificationId}/read`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    await fetchUnreadCount()
  }

  async function markAllAsRead() {
    await $fetch('/api/notifications/read-all', {
      method: 'PUT',
      headers: { Authorization: `Bearer ${authStore.token}` }
    })
    notificationStore.setUnreadCount(0)
  }

  return {
    fetchUnreadCount,
    startPolling,
    stopPolling,
    fetchNotifications,
    markAsRead,
    markAllAsRead
  }
}
