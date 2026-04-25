/**
 * Playlist navigation pure functions.
 * Uses modular arithmetic for circular (wrap-around) navigation.
 */

/**
 * Calculate the next track index in a circular playlist.
 * @param {number} currentIndex - Current track index
 * @param {number} length - Total number of tracks
 * @returns {number} Next track index
 */
export function nextIndex(currentIndex, length) {
  if (length <= 0) return 0
  return (currentIndex + 1) % length
}

/**
 * Calculate the previous track index in a circular playlist.
 * @param {number} currentIndex - Current track index
 * @param {number} length - Total number of tracks
 * @returns {number} Previous track index
 */
export function prevIndex(currentIndex, length) {
  if (length <= 0) return 0
  return (currentIndex - 1 + length) % length
}
