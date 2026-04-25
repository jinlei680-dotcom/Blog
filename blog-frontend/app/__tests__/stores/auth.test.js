import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '~/stores/auth'

describe('useAuthStore', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useAuthStore()
    // Clean up localStorage keys used by the store
    try { window.localStorage.removeItem('auth_token') } catch {}
    try { window.localStorage.removeItem('auth_user') } catch {}
  })

  describe('initial state', () => {
    it('should have null token and user by default', () => {
      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
    })

    it('should have isLoggedIn as false by default', () => {
      expect(store.isLoggedIn).toBe(false)
    })

    it('should have empty username by default', () => {
      expect(store.username).toBe('')
    })
  })

  describe('setAuth', () => {
    it('should set token and user in store state', () => {
      const token = 'jwt-token-123'
      const user = { id: 1, username: 'testuser' }

      store.setAuth(token, user)

      expect(store.token).toBe(token)
      expect(store.user).toEqual(user)
      expect(store.isLoggedIn).toBe(true)
      expect(store.username).toBe('testuser')
    })

    it('should persist to localStorage', () => {
      const token = 'jwt-token-123'
      const user = { id: 1, username: 'testuser' }

      store.setAuth(token, user)

      // Verify localStorage was updated
      expect(window.localStorage.getItem('auth_token')).toBe(token)
      expect(window.localStorage.getItem('auth_user')).toBe(JSON.stringify(user))
    })
  })

  describe('clearAuth', () => {
    it('should clear token and user from store state', () => {
      store.setAuth('token', { id: 1, username: 'user' })
      store.clearAuth()

      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })

    it('should remove from localStorage', () => {
      store.setAuth('token', { id: 1, username: 'user' })
      store.clearAuth()

      expect(window.localStorage.getItem('auth_token')).toBeNull()
      expect(window.localStorage.getItem('auth_user')).toBeNull()
    })
  })

  describe('restoreAuth', () => {
    it('should restore token and user from localStorage', () => {
      const token = 'saved-token'
      const user = { id: 2, username: 'saveduser' }
      window.localStorage.setItem('auth_token', token)
      window.localStorage.setItem('auth_user', JSON.stringify(user))

      store.restoreAuth()

      expect(store.token).toBe(token)
      expect(store.user).toEqual(user)
      expect(store.isLoggedIn).toBe(true)
    })

    it('should not restore if localStorage is empty', () => {
      store.restoreAuth()

      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })

    it('should clear auth if saved user JSON is invalid', () => {
      window.localStorage.setItem('auth_token', 'some-token')
      window.localStorage.setItem('auth_user', 'invalid-json{{{')

      store.restoreAuth()

      expect(store.token).toBeNull()
      expect(store.user).toBeNull()
    })
  })

  describe('isLoggedIn computed', () => {
    it('should be true when token is set', () => {
      store.setAuth('token', { id: 1, username: 'u' })
      expect(store.isLoggedIn).toBe(true)
    })

    it('should be false after clearAuth', () => {
      store.setAuth('token', { id: 1, username: 'u' })
      store.clearAuth()
      expect(store.isLoggedIn).toBe(false)
    })
  })
})
