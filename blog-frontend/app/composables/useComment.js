import { useAuthStore } from '~/stores/auth'

export function useComment() {
  const authStore = useAuthStore()

  function authHeaders() {
    const headers = {}
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return headers
  }

  async function fetchComments(articleId) {
    const res = await $fetch(`/api/articles/${articleId}/comments`)
    return res.data
  }

  async function createComment(articleId, content, parentId = null) {
    const body = { content }
    if (parentId) {
      body.parentId = parentId
    }
    const res = await $fetch(`/api/articles/${articleId}/comments`, {
      method: 'POST',
      headers: authHeaders(),
      body
    })
    return res.data
  }

  async function deleteComment(commentId) {
    const res = await $fetch(`/api/comments/${commentId}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
    return res
  }

  return {
    fetchComments,
    createComment,
    deleteComment
  }
}
