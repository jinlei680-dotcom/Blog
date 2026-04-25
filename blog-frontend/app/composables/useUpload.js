import { useAuthStore } from '~/stores/auth'

export function useUpload() {
  const authStore = useAuthStore()

  function authHeaders() {
    const headers = {}
    if (authStore.token) {
      headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return headers
  }

  async function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await $fetch('/api/upload/image', {
      method: 'POST',
      headers: authHeaders(),
      body: formData
    })
    return res.data
  }

  async function uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await $fetch('/api/users/avatar', {
      method: 'POST',
      headers: authHeaders(),
      body: formData
    })
    return res.data
  }

  return {
    uploadImage,
    uploadAvatar
  }
}
