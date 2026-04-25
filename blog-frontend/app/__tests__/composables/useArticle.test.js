import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '~/stores/auth'
import { useArticle } from '~/composables/useArticle'

vi.stubGlobal('$fetch', vi.fn())

describe('useArticle composable', () => {
  let authStore

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    vi.clearAllMocks()
  })

  describe('fetchArticles', () => {
    it('should call GET /api/articles with default page and size', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { content: [], totalPages: 0 }
      }))

      const { fetchArticles } = useArticle()
      await fetchArticles()

      expect($fetch).toHaveBeenCalledWith('/api/articles', {
        params: { page: 0, size: 10 }
      })
    })

    it('should pass custom page and size params', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { content: [], totalPages: 5 }
      }))

      const { fetchArticles } = useArticle()
      await fetchArticles(2, 20)

      expect($fetch).toHaveBeenCalledWith('/api/articles', {
        params: { page: 2, size: 20 }
      })
    })

    it('should return the data from response', async () => {
      const mockData = { content: [{ id: 1, title: 'Test' }], totalPages: 1 }
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({ data: mockData }))

      const { fetchArticles } = useArticle()
      const result = await fetchArticles()

      expect(result).toEqual(mockData)
    })
  })

  describe('fetchArticle', () => {
    it('should call GET /api/articles/:id without auth when not logged in', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { id: 1, title: 'Test', content: 'Content' }
      }))

      const { fetchArticle } = useArticle()
      await fetchArticle(1)

      expect($fetch).toHaveBeenCalledWith('/api/articles/1', {
        headers: {}
      })
    })

    it('should include Authorization header when logged in', async () => {
      authStore.setAuth('jwt-token-123', { id: 1, username: 'user' })
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { id: 1, title: 'Test' }
      }))

      const { fetchArticle } = useArticle()
      await fetchArticle(1)

      expect($fetch).toHaveBeenCalledWith('/api/articles/1', {
        headers: { Authorization: 'Bearer jwt-token-123' }
      })
    })
  })

  describe('createArticle', () => {
    it('should call POST /api/articles with title and content', async () => {
      authStore.setAuth('token-abc', { id: 1, username: 'author' })
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { id: 10, title: 'New Article', content: 'Body text' }
      }))

      const { createArticle } = useArticle()
      const result = await createArticle('New Article', 'Body text')

      expect($fetch).toHaveBeenCalledWith('/api/articles', {
        method: 'POST',
        headers: { Authorization: 'Bearer token-abc' },
        body: { title: 'New Article', content: 'Body text' }
      })
      expect(result).toEqual({ id: 10, title: 'New Article', content: 'Body text' })
    })
  })

  describe('updateArticle', () => {
    it('should call PUT /api/articles/:id with title and content', async () => {
      authStore.setAuth('token-abc', { id: 1, username: 'author' })
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { id: 5, title: 'Updated', content: 'Updated body' }
      }))

      const { updateArticle } = useArticle()
      const result = await updateArticle(5, 'Updated', 'Updated body')

      expect($fetch).toHaveBeenCalledWith('/api/articles/5', {
        method: 'PUT',
        headers: { Authorization: 'Bearer token-abc' },
        body: { title: 'Updated', content: 'Updated body' }
      })
      expect(result).toEqual({ id: 5, title: 'Updated', content: 'Updated body' })
    })
  })

  describe('deleteArticle', () => {
    it('should call DELETE /api/articles/:id', async () => {
      authStore.setAuth('token-abc', { id: 1, username: 'author' })
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({ code: 200 }))

      const { deleteArticle } = useArticle()
      await deleteArticle(5)

      expect($fetch).toHaveBeenCalledWith('/api/articles/5', {
        method: 'DELETE',
        headers: { Authorization: 'Bearer token-abc' }
      })
    })
  })
})
