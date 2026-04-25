/**
 * Composable for fetching the music playlist from the backend API.
 */
export function useMusic() {
  async function fetchMusicList() {
    const res = await $fetch('/api/music')
    return res.data
  }

  return {
    fetchMusicList
  }
}
