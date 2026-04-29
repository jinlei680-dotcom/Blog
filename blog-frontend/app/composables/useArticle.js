import { useAuthStore } from '~/stores/auth'

export function useArticle() {
  const authStore = useAuthStore()

  function authHeaders() {
    const headers = {}
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return headers
  }

  async function fetchArticles(page = 0, size = 10) {
    const res = await $fetch('/api/articles', {
      params: { page, size }
    })
    return res.data
  }

  async function fetchArticle(id) {
    const res = await $fetch(`/api/articles/${id}`, {
      headers: authHeaders()
    })
    return res.data
  }

  async function createArticle(title, content, tagIds) {
    const body = { title, content }
    if (tagIds && tagIds.length > 0) {
      body.tagIds = tagIds
    }
    const res = await $fetch('/api/articles', {
      method: 'POST',
      headers: authHeaders(),
      body
    })
    return res.data
  }

  async function updateArticle(id, title, content, tagIds) {
    const body = { title, content }
    if (tagIds !== undefined) {
      body.tagIds = tagIds || []
    }
    const res = await $fetch(`/api/articles/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body
    })
    return res.data
  }

  async function deleteArticle(id) {
    const res = await $fetch(`/api/articles/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
    return res
  }

  async function fetchFeatured(size = 3) {
    const res = await $fetch('/api/articles/featured', { params: { size } })
    return res.data
  }

  async function fetchStats() {
    const res = await $fetch('/api/articles/stats')
    return res.data
  }

  return {
    fetchArticles,
    fetchArticle,
    fetchFeatured,
    fetchStats,
    createArticle,
    updateArticle,
    deleteArticle
  }
}
