import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// Plugin to replace import.meta.client/server in source code
function nuxtMetaPlugin() {
  return {
    name: 'nuxt-meta-replace',
    transform(code, id) {
      if (id.includes('node_modules') || id.includes('vitest.setup')) return null
      const transformed = code
        .replace(/import\.meta\.client/g, 'true')
        .replace(/import\.meta\.server/g, 'false')
      if (transformed !== code) {
        return { code: transformed, map: null }
      }
      return null
    }
  }
}

export default defineConfig({
  plugins: [vue(), nuxtMetaPlugin()],
  resolve: {
    alias: {
      '~': path.resolve(__dirname, 'app'),
      '#app': path.resolve(__dirname, 'app')
    }
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./vitest.setup.js']
  }
})
