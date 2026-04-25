import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '~/stores/auth'
import CommentList from '~/components/CommentList.vue'
import CommentForm from '~/components/CommentForm.vue'

// Mock Nuxt auto-imports
vi.stubGlobal('navigateTo', vi.fn())
vi.stubGlobal('$fetch', vi.fn())
vi.stubGlobal('definePageMeta', vi.fn())

const NuxtLink = { template: '<a><slot /></a>', props: ['to'] }

// ============================================================
// CommentList Tests
// ============================================================
describe('CommentList', () => {
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
  })

  function mountList(props = {}) {
    return mount(CommentList, {
      props: {
        comments: [],
        depth: 0,
        ...props
      },
      global: {
        plugins: [pinia],
        stubs: { NuxtLink, Transition: false, TransitionGroup: false }
      }
    })
  }

  it('should render top-level comments', () => {
    const comments = [
      { id: 1, content: 'First comment', username: 'alice', userId: 1, createdAt: '2024-01-01T10:00:00', children: [] },
      { id: 2, content: 'Second comment', username: 'bob', userId: 2, createdAt: '2024-01-01T11:00:00', children: [] }
    ]
    const wrapper = mountList({ comments })

    expect(wrapper.text()).toContain('First comment')
    expect(wrapper.text()).toContain('Second comment')
    expect(wrapper.text()).toContain('alice')
    expect(wrapper.text()).toContain('bob')
  })

  it('should render nested children recursively', () => {
    const comments = [
      {
        id: 1,
        content: 'Parent comment',
        username: 'alice',
        userId: 1,
        createdAt: '2024-01-01T10:00:00',
        children: [
          {
            id: 3,
            content: 'Child reply',
            username: 'charlie',
            userId: 3,
            createdAt: '2024-01-01T12:00:00',
            children: []
          }
        ]
      }
    ]
    const wrapper = mountList({ comments })

    expect(wrapper.text()).toContain('Parent comment')
    expect(wrapper.text()).toContain('Child reply')
    expect(wrapper.text()).toContain('charlie')
  })

  it('should render empty when no comments', () => {
    const wrapper = mountList({ comments: [] })
    expect(wrapper.findAll('.comment-item')).toHaveLength(0)
  })

  it('should show delete button only for own comments when logged in', () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const comments = [
      { id: 1, content: 'My comment', username: 'alice', userId: 1, createdAt: '2024-01-01T10:00:00', children: [] },
      { id: 2, content: 'Other comment', username: 'bob', userId: 2, createdAt: '2024-01-01T11:00:00', children: [] }
    ]
    const wrapper = mountList({ comments })

    const deleteButtons = wrapper.findAll('.action-delete')
    expect(deleteButtons).toHaveLength(1)
  })

  it('should show reply button when logged in', () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const comments = [
      { id: 1, content: 'A comment', username: 'alice', userId: 1, createdAt: '2024-01-01T10:00:00', children: [] }
    ]
    const wrapper = mountList({ comments })

    const replyButtons = wrapper.findAll('.action-reply')
    expect(replyButtons).toHaveLength(1)
    expect(replyButtons[0].text()).toContain('回复')
  })

  it('should not show reply/delete buttons when not logged in', () => {
    const comments = [
      { id: 1, content: 'A comment', username: 'alice', userId: 1, createdAt: '2024-01-01T10:00:00', children: [] }
    ]
    const wrapper = mountList({ comments })

    expect(wrapper.findAll('.action-reply')).toHaveLength(0)
    expect(wrapper.findAll('.action-delete')).toHaveLength(0)
  })
})

// ============================================================
// CommentForm Tests
// ============================================================
describe('CommentForm', () => {
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
  })

  function mountForm(props = {}) {
    return mount(CommentForm, {
      props: {
        parentId: null,
        replyTo: '',
        ...props
      },
      global: {
        plugins: [pinia],
        stubs: { NuxtLink, Transition: false }
      }
    })
  }

  it('should show login prompt when not logged in', () => {
    const wrapper = mountForm()
    expect(wrapper.text()).toContain('登录')
    expect(wrapper.text()).toContain('后参与评论')
    expect(wrapper.find('textarea').exists()).toBe(false)
  })

  it('should show textarea when logged in', () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const wrapper = mountForm()
    expect(wrapper.find('textarea').exists()).toBe(true)
  })

  it('should disable submit button when content is empty', () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const wrapper = mountForm()
    const submitBtn = wrapper.find('.submit-btn')
    expect(submitBtn.attributes('disabled')).toBeDefined()
  })

  it('should enable submit button when content is not empty', async () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const wrapper = mountForm()
    await wrapper.find('textarea').setValue('Hello world')

    const submitBtn = wrapper.find('.submit-btn')
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('should emit submitted event with content on form submit', async () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const wrapper = mountForm()
    await wrapper.find('textarea').setValue('My comment')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.emitted('submitted')).toBeTruthy()
    expect(wrapper.emitted('submitted')[0][0]).toEqual({
      content: 'My comment',
      parentId: null
    })
  })

  it('should emit submitted with parentId for reply', async () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const wrapper = mountForm({ parentId: 42, replyTo: 'bob' })
    await wrapper.find('textarea').setValue('Reply content')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.emitted('submitted')[0][0]).toEqual({
      content: 'Reply content',
      parentId: 42
    })
  })

  it('should clear textarea after successful submit', async () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const wrapper = mountForm()
    await wrapper.find('textarea').setValue('My comment')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('textarea').element.value).toBe('')
  })

  it('should show reply indicator when parentId is set', () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const wrapper = mountForm({ parentId: 5, replyTo: 'bob' })
    expect(wrapper.text()).toContain('回复')
    expect(wrapper.text()).toContain('bob')
  })

  it('should show character count', () => {
    const authStore = useAuthStore()
    authStore.setAuth('token', { id: 1, username: 'alice' })

    const wrapper = mountForm()
    expect(wrapper.text()).toContain('0/1000')
  })
})
