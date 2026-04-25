export function useDraft(draftKey) {
  function saveDraft(title, content, tagIds) {
    if (!import.meta.client) return
    const data = { title, content, tagIds, savedAt: new Date().toISOString() }
    try {
      localStorage.setItem(draftKey, JSON.stringify(data))
    } catch {}
  }

  function loadDraft() {
    if (!import.meta.client) return null
    try {
      const raw = localStorage.getItem(draftKey)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  }

  function clearDraft() {
    if (!import.meta.client) return
    try {
      localStorage.removeItem(draftKey)
    } catch {}
  }

  function hasDraft() {
    return !!loadDraft()
  }

  function formatDraftTime(savedAt) {
    if (!savedAt) return ''
    const d = new Date(savedAt)
    if (isNaN(d.getTime())) return ''
    const now = new Date()
    const diffMs = now - d
    const diffMin = Math.floor(diffMs / 60000)
    if (diffMin < 1) return '刚刚'
    if (diffMin < 60) return `${diffMin} 分钟前`
    const diffH = Math.floor(diffMin / 60)
    if (diffH < 24) return `${diffH} 小时前`
    return `${d.getMonth() + 1}月${d.getDate()}日`
  }

  return { saveDraft, loadDraft, clearDraft, hasDraft, formatDraftTime }
}
