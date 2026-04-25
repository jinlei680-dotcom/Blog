import { useAuthStore } from '~/stores/auth'

export function useTag() {
  const authStore = useAuthStore()

  function authHeaders() {
    const headers = {}
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return headers
  }

  async function fetchTags() {
    const res = await $fetch('/api/tags')
    return res.data
  }

  async function createTag(name) {
    const res = await $fetch('/api/tags', {
      method: 'POST',
      headers: authHeaders(),
      body: { name }
    })
    return res.data
  }

  async function deleteTag(id) {
    await $fetch(`/api/tags/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
  }

  async function fetchArticlesByTag(tagId, page = 0, size = 10) {
    const res = await $fetch(`/api/tags/${tagId}/articles`, {
      params: { page, size }
    })
    return res.data
  }

  return {
    fetchTags,
    createTag,
    deleteTag,
    fetchArticlesByTag
  }
}
