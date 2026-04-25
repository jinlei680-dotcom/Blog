import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import LoginPage from '~/pages/login.vue'

// Mock Nuxt auto-imports
vi.stubGlobal('navigateTo', vi.fn())
vi.stubGlobal('$fetch', vi.fn())

// Mock NuxtLink as a simple anchor
const NuxtLink = { template: '<a><slot /></a>', props: ['to'] }

function mountLogin() {
  return mount(LoginPage, {
    global: {
      plugins: [createPinia()],
      stubs: { NuxtLink, Transition: false }
    }
  })
}

describe('Login Page', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('form validation', () => {
    it('should show error when username is empty on submit', async () => {
      const wrapper = mountLogin()
      const passwordInput = wrapper.find('#password')
      await passwordInput.setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('请输入用户名')
    })

    it('should show error when password is empty on submit', async () => {
      const wrapper = mountLogin()
      const usernameInput = wrapper.find('#username')
      await usernameInput.setValue('testuser')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('请输入密码')
    })

    it('should show both errors when both fields are empty', async () => {
      const wrapper = mountLogin()

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('请输入用户名')
      expect(wrapper.text()).toContain('请输入密码')
    })

    it('should clear username error when user types valid input and blurs', async () => {
      const wrapper = mountLogin()

      // Trigger empty submit to show errors
      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(wrapper.find('.form-error').exists()).toBe(true)

      // Type valid username and blur
      const usernameInput = wrapper.find('#username')
      await usernameInput.setValue('testuser')
      await usernameInput.trigger('blur')
      await flushPromises()

      // The first form-group should no longer have the error class
      const usernameGroup = wrapper.find('#username').element.closest('.form-group')
      expect(usernameGroup.classList.contains('has-error')).toBe(false)
    })
  })

  describe('form submission - success', () => {
    it('should call login API and navigate to home on success', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { token: 'jwt-123', user: { id: 1, username: 'testuser' } }
      }))

      const wrapper = mountLogin()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect($fetch).toHaveBeenCalledWith('/api/auth/login', {
        method: 'POST',
        body: { username: 'testuser', password: 'password123' }
      })
      expect(navigateTo).toHaveBeenCalledWith('/')
    })
  })

  describe('form submission - failure', () => {
    it('should display API error message on login failure', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockRejectedValue({
        data: { message: '用户名或密码错误' }
      }))

      const wrapper = mountLogin()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#password').setValue('wrongpass')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('用户名或密码错误')
    })

    it('should display fallback error message when no message in response', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockRejectedValue(new Error()))

      const wrapper = mountLogin()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#password').setValue('wrongpass')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('登录失败，请重试')
    })

    it('should not submit when validation fails', async () => {
      vi.stubGlobal('$fetch', vi.fn())

      const wrapper = mountLogin()
      // Leave fields empty
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect($fetch).not.toHaveBeenCalled()
    })
  })

  describe('loading state', () => {
    it('should disable button during submission', async () => {
      let resolveLogin
      vi.stubGlobal('$fetch', vi.fn().mockImplementation(() =>
        new Promise((resolve) => { resolveLogin = resolve })
      ))

      const wrapper = mountLogin()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      const button = wrapper.find('button[type="submit"]')
      expect(button.attributes('disabled')).toBeDefined()
      expect(button.text()).toContain('登录中...')

      resolveLogin({ data: { token: 't', user: { id: 1, username: 'u' } } })
      await flushPromises()

      expect(button.attributes('disabled')).toBeUndefined()
    })
  })
})
