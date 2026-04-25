import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import CreatePage from '~/pages/articles/create.vue'

// Mock Nuxt auto-imports
vi.stubGlobal('navigateTo', vi.fn())
vi.stubGlobal('$fetch', vi.fn())
vi.stubGlobal('definePageMeta', vi.fn())
vi.stubGlobal('useRoute', vi.fn(() => ({ params: { id: '1' }, query: {} })))
vi.stubGlobal('useAsyncData', vi.fn(() => ({
  data: { value: null },
  error: { value: null },
  status: { value: 'idle' },
  refresh: vi.fn()
})))

const NuxtLink = { template: '<a><slot /></a>', props: ['to'] }

function mountCreate() {
  return mount(CreatePage, {
    global: {
      plugins: [createPinia()],
      stubs: { NuxtLink, Transition: false }
    }
  })
}

// ============================================================
// 1. Article Create Page Tests
// ============================================================
describe('Article Create Page', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('form validation', () => {
    it('should show error when title is empty on submit', async () => {
      const wrapper = mountCreate()
      await wrapper.find('#content').setValue('Some content here')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('标题不能为空')
    })

    it('should show error when content is empty on submit', async () => {
      const wrapper = mountCreate()
      await wrapper.find('#title').setValue('Test Title')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('正文不能为空')
    })

    it('should show both errors when both fields are empty', async () => {
      const wrapper = mountCreate()

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('标题不能为空')
      expect(wrapper.text()).toContain('正文不能为空')
    })

    it('should treat whitespace-only title as empty', async () => {
      const wrapper = mountCreate()
      await wrapper.find('#title').setValue('   ')
      await wrapper.find('#content').setValue('Some content')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('标题不能为空')
    })

    it('should treat whitespace-only content as empty', async () => {
      const wrapper = mountCreate()
      await wrapper.find('#title').setValue('Test Title')
      await wrapper.find('#content').setValue('   ')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.text()).toContain('正文不能为空')
    })

    it('should not call API when validation fails', async () => {
      vi.stubGlobal('$fetch', vi.fn())

      const wrapper = mountCreate()
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect($fetch).not.toHaveBeenCalled()
    })
  })

  describe('form submission - success', () => {
    it('should call createArticle API and navigate to article page on success', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { id: 42, title: 'My Article', content: 'Article content' }
      }))

      const wrapper = mountCreate()
      await wrapper.find('#title').setValue('My Article')
      await wrapper.find('#content').setValue('Article content')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect($fetch).toHaveBeenCalledWith('/api/articles', {
        method: 'POST',
        headers: {},
        body: { title: 'My Article', content: 'Article content' }
      })
      expect(navigateTo).toHaveBeenCalledWith('/articles/42')
    })

    it('should trim title and content before submission', async () => {
      vi.stubGlobal('$fetch', vi.fn().mockResolvedValue({
        data: { id: 1, title: 'Trimmed', content: 'Content' }
      }))

      const wrapper = mountCreate()
      await wrapper.find('#title').setValue('  Trimmed  ')
      await wrapper.find('#content').setValue('  Content  ')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect($fetch).toHaveBeenCalledWith('/api/articles', expect.objectContaining({
        body: { title: 'Trimmed', content: 'Content' }
      }))
    })

    it('should disable submit button during submission', async () => {
      let resolveCreate
      vi.stubGlobal('$fetch', vi.fn().mockImplementation(() =>
        new Promise((resolve) => { resolveCreate = resolve })
      ))

      const wrapper = mountCreate()
      await wrapper.find('#title').setValue('Title')
      await wrapper.find('#content').setValue('Content')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      const button = wrapper.find('button[type="submit"]')
      expect(button.attributes('disabled')).toBeDefined()
      expect(button.text()).toContain('发布中...')

      resolveCreate({ data: { id: 1 } })
      await flushPromises()

      expect(button.attributes('disabled')).toBeUndefined()
    })
  })

  describe('form submission - failure', () => {
    it('should show alert on API error', async () => {
      const alertMock = vi.spyOn(window, 'alert').mockImplementation(() => {})
      vi.stubGlobal('$fetch', vi.fn().mockRejectedValue({
        data: { message: '标题不能为空' }
      }))

      const wrapper = mountCreate()
      await wrapper.find('#title').setValue('Title')
      await wrapper.find('#content').setValue('Content')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(alertMock).toHaveBeenCalledWith('标题不能为空')
      alertMock.mockRestore()
    })

    it('should show fallback error message when no message in response', async () => {
      const alertMock = vi.spyOn(window, 'alert').mockImplementation(() => {})
      vi.stubGlobal('$fetch', vi.fn().mockRejectedValue(new Error()))

      const wrapper = mountCreate()
      await wrapper.find('#title').setValue('Title')
      await wrapper.find('#content').setValue('Content')

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(alertMock).toHaveBeenCalledWith('发布失败，请重试')
      alertMock.mockRestore()
    })
  })
})
