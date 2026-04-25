// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  srcDir: 'app/',

  // SSR enabled globally
  ssr: true,

  // Per-route rendering rules
  routeRules: {
    '/': { ssr: true },
    '/articles/create': { ssr: false },
    '/articles/*/edit': { ssr: false },
    '/articles/**': { ssr: true },
    '/search': { ssr: true },
    '/tags/**': { ssr: true },
    '/users/**': { ssr: true },
    '/login': { ssr: false },
    '/register': { ssr: false }
  },

  // API proxy: forward /api/** to backend on port 8080
  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080/api',
        changeOrigin: true
      }
    },
    routeRules: {
      '/api/**': {
        proxy: 'http://localhost:8080/api/**'
      }
    }
  },

  // Modules
  modules: [
    '@pinia/nuxt'
  ]
})
