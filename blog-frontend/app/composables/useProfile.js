import { useAuthStore } from '~/stores/auth'

export function useProfile() {
  const authStore = useAuthStore()

  function authHeaders() {
    const headers = {}
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return headers
  }

  async function fetchProfile(userId) {
    const res = await $fetch(`/api/users/${userId}/profile`)
    return res.data
  }

  async function fetchUserArticles(userId, page = 0, size = 10) {
    const res = await $fetch(`/api/users/${userId}/articles`, {
      params: { page, size }
    })
    return res.data
  }

  return {
    fetchProfile,
    fetchUserArticles
  }
}
