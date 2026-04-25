import { useAuthStore } from '~/stores/auth'

export default defineNuxtRouteMiddleware((to) => {
  // Only check auth on client side (localStorage not available on server)
  if (import.meta.server) return

  const authStore = useAuthStore()
  if (!authStore.isLoggedIn) {
    return navigateTo('/login')
  }
})
