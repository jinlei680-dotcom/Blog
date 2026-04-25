import { useAuthStore } from '~/stores/auth'

export function useAuth() {
  const authStore = useAuthStore()

  async function login(username, password) {
    const res = await $fetch('/api/auth/login', {
      method: 'POST',
      body: { username, password }
    })
    authStore.setAuth(res.data.token, res.data.user, res.data.role)
    return res
  }

  async function register(username, password, email) {
    const res = await $fetch('/api/auth/register', {
      method: 'POST',
      body: { username, password, email }
    })
    return res
  }

  function logout() {
    authStore.clearAuth()
    navigateTo('/')
  }

  return {
    login,
    register,
    logout
  }
}
