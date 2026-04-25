import { ref } from 'vue'

export function useBookmark(userId) {
  const storageKey = userId ? `bookmarks_${userId}` : null

  function _load() {
    if (!import.meta.client || !storageKey) return []
    try {
      const raw = localStorage.getItem(storageKey)
      return raw ? JSON.parse(raw) : []
    } catch {
      return []
    }
  }

  function _save(list) {
    if (!import.meta.client || !storageKey) return
    try {
      localStorage.setItem(storageKey, JSON.stringify(list))
    } catch {}
  }

  function getBookmarks() {
    return _load()
  }

  function isBookmarked(articleId) {
    const list = _load()
    return list.some(b => String(b.id) === String(articleId))
  }

  const bookmarkState = ref(false)

  function syncState(articleId) {
    bookmarkState.value = isBookmarked(articleId)
    return bookmarkState
  }

  function toggleBookmark(articleId, articleTitle) {
    const list = _load()
    const idx = list.findIndex(b => String(b.id) === String(articleId))
    if (idx >= 0) {
      list.splice(idx, 1)
      bookmarkState.value = false
    } else {
      list.unshift({ id: String(articleId), title: articleTitle || '', savedAt: new Date().toISOString() })
      bookmarkState.value = true
    }
    _save(list)
    return bookmarkState.value
  }

  function removeBookmark(articleId) {
    const list = _load().filter(b => String(b.id) !== String(articleId))
    _save(list)
  }

  function clearBookmarks() {
    _save([])
  }

  return { getBookmarks, isBookmarked, toggleBookmark, removeBookmark, clearBookmarks, bookmarkState, syncState }
}
