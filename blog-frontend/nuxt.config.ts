// https://nuxt.com/docs/api/configuration/nuxt-config
import { fileURLToPath } from 'url'
import { dirname, resolve } from 'path'

const __dirname = dirname(fileURLToPath(import.meta.url))

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
    },
    publicAssets: [
      { dir: resolve(__dirname, 'public'), maxAge: 3600 }
    ]
  },

  // Modules
  modules: [
    '@pinia/nuxt'
  ],

  // Preload hero background image to reduce LCP
  app: {
    head: {
      link: [
        { rel: 'preload', as: 'image', href: '/images/hero-light.jpg' }
      ]
    }
  }
})
