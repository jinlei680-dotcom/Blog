import { useAuthStore } from '~/stores/auth'

export default defineNuxtRouteMiddleware((to) => {
  if (import.meta.server) return

  const authStore = useAuthStore()
  if (!authStore.isLoggedIn || !authStore.isAdmin) {
    return navigateTo('/')
  }
})
