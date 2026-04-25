import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(null)
  const user = ref(null)
  const role = ref(null)

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '')
  const isAdmin = computed(() => role.value === 'ROLE_ADMIN')

  function setAuth(newToken, newUser, newRole) {
    token.value = newToken
    user.value = newUser
    role.value = newRole || null
    if (import.meta.client) {
      localStorage.setItem('auth_token', newToken)
      localStorage.setItem('auth_user', JSON.stringify(newUser))
      if (newRole) {
        localStorage.setItem('auth_role', newRole)
      }
    }
  }

  function clearAuth() {
    token.value = null
    user.value = null
    role.value = null
    if (import.meta.client) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
      localStorage.removeItem('auth_role')
    }
  }

  function restoreAuth() {
    if (import.meta.client) {
      const savedToken = localStorage.getItem('auth_token')
      const savedUser = localStorage.getItem('auth_user')
      const savedRole = localStorage.getItem('auth_role')
      if (savedToken && savedUser) {
        token.value = savedToken
        role.value = savedRole || null
        try {
          user.value = JSON.parse(savedUser)
        } catch {
          clearAuth()
        }
      }
    }
  }

  return {
    token,
    user,
    role,
    isLoggedIn,
    username,
    isAdmin,
    setAuth,
    clearAuth,
    restoreAuth
  }
})
