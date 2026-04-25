import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import RegisterPage from '~/pages/register.vue'

// Mock Nuxt auto-imports
vi.stubGlobal('navigateTo', vi.fn())
vi.stubGlobal('$fetch', vi.fn())

const NuxtLink = { template: '<a><slot /></a>', props: ['to'] }

function mountRegister() {
  return mount(RegisterPage, {
    global: {
      plugins: [createPinia()],
      stubs: { NuxtLink, Transition: false }
    }
  })
}

describe('Register Page', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('form validation', () => {
    it('should show error when username is empty', async () => {
      const wrapper = mountRegister()
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('请输入用户名')
    })

    it('should show error when email is empty', async () => {
      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('请输入邮箱')
    })

    it('should show error when email format is invalid', async () => {
      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#email').setValue('not-an-email')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('请输入有效的邮箱地址')
    })

    it('should show error when password is empty', async () => {
      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#email').setValue('test@example.com')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('请输入密码')
    })

    it('should show error when password is less than 6 characters', async () => {
      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('12345')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('密码至少需要 6 位字符')
    })

    it('should show all errors when all fields are empty', async () => {
      const wrapper = mountRegister()

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('请输入用户名')
      expect(wrapper.text()).toContain('请输入邮箱')
      expect(wrapper.text()).toContain('请输入密码')
    })

    it('should accept valid email formats', async () => {
      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('testuser')
      await wrapper.find('#email').setValue('user@domain.com')
      await wrapper.find('#password').setValue('password123')

      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({ data: {} }))

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).not.toContain('请输入有效的邮箱地址')
    })
  })

  describe('form submission - success', () => {
    it('should call register API and show success message', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({ data: {} }))

      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('newuser')
      await wrapper.find('#email').setValue('new@example.com')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect($fetch).toHaveBeenCalledWith('/api/auth/register', {
        method: 'POST',
        body: { username: 'newuser', password: 'password123', email: 'new@example.com' }
      })
      expect(wrapper.text()).toContain('注册成功')
    })

    it('should navigate to login page after successful registration', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({ data: {} }))

      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('newuser')
      await wrapper.find('#email').setValue('new@example.com')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      vi.advanceTimersByTime(1500)
      expect(navigateTo).toHaveBeenCalledWith('/login')
    })
  })

  describe('form submission - failure', () => {
    it('should display API error message on registration failure', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockRejectedValue({
        data: { message: '用户名已被占用' }
      }))

      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('existinguser')
      await wrapper.find('#email').setValue('new@example.com')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('用户名已被占用')
    })

    it('should display fallback error when no message in response', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockRejectedValue(new Error()))

      const wrapper = mountRegister()
      await wrapper.find('#username').setValue('newuser')
      await wrapper.find('#email').setValue('new@example.com')
      await wrapper.find('#password').setValue('password123')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('注册失败，请重试')
    })

    it('should not submit when validation fails', async () => {
      vi.stubGlobal('$fetch', vi.fn())

      const wrapper = mountRegister()
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect($fetch).not.toHaveBeenCalled()
    })
  })
})
