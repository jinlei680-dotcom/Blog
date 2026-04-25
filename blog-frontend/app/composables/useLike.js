import { useAuthStore } from '~/stores/auth'

export function useLike() {
  const authStore = useAuthStore()

  async function toggleLike(articleId) {
    const res = await $fetch(`/api/articles/${articleId}/like`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${authStore.token}`
      }
    })
    return res.data
  }

  return {
    toggleLike
  }
}
