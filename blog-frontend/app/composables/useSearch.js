export function useSearch() {
  async function searchArticles(keyword, page = 0, size = 10) {
    const res = await $fetch('/api/articles/search', {
      params: { q: keyword, page, size }
    })
    return res.data
  }

  return {
    searchArticles
  }
}
