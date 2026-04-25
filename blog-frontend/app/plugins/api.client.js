import { useAuthStore } from '~/stores/auth'

export default defineNuxtPlugin(() => {
  const authStore = useAuthStore()

  // Global interceptor for $fetch
  globalThis.$fetch = new Proxy(globalThis.$fetch, {
    apply(target, thisArg, args) {
      const [url, options = {}] = args

      // Add onResponseError interceptor
      const originalOnResponseError = options.onResponseError
      options.onResponseError = (ctx) => {
        const status = ctx.response?.status

        // 401: clear auth and redirect to login
        // Skip for notification polling (silent fail) and if already on login page
        if (status === 401) {
          const requestUrl = typeof url === 'string' ? url : ''
          const isNotificationRequest = requestUrl.includes('/api/notifications')
          const isOnLoginPage = window.location.pathname === '/login'

          if (!isNotificationRequest && !isOnLoginPage) {
            authStore.clearAuth()
            navigateTo('/login')
          }
          return
        }

        // 403: show permission denied alert
        if (status === 403) {
          const msg = ctx.response?._data?.message || '你没有权限执行此操作'
          if (typeof window !== 'undefined') {
            alert(msg)
          }
        }

        // Call original handler if exists
        if (typeof originalOnResponseError === 'function') {
          originalOnResponseError(ctx)
        }
      }

      return Reflect.apply(target, thisArg, [url, options])
    }
  })
})
