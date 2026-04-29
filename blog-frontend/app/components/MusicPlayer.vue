<template>
  <Transition name="player-slide">
    <div v-if="playlist.length > 0" class="music-player">
      <div class="player-progress-bar" @click="seekTo">
        <div class="player-progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>
      <div class="player-content">
        <div class="player-track-info">
          <span class="player-icon">♪</span>
          <span class="player-track-name">{{ currentTrackName }}</span>
        </div>
        <div class="player-controls">
          <button class="player-btn" @click="playPrev" aria-label="上一曲">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 6h2v12H6zm3.5 6l8.5 6V6z"/>
            </svg>
          </button>
          <button class="player-btn player-btn--play" @click="togglePlay" :aria-label="isPlaying ? '暂停' : '播放'">
            <svg v-if="!isPlaying" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M8 5v14l11-7z"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
            </svg>
          </button>
          <button class="player-btn" @click="playNext" aria-label="下一曲">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"/>
            </svg>
          </button>
        </div>
        <div class="player-time">
          {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useMusic } from '~/composables/useMusic'
import { nextIndex, prevIndex } from '~/utils/playerNavigation'

const { fetchMusicList } = useMusic()

const playlist = ref([])
const currentIdx = ref(0)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)

let audio = null

const currentTrackName = computed(() => {
  if (playlist.value.length === 0) return ''
  return playlist.value[currentIdx.value]?.name || ''
})

const progressPercent = computed(() => {
  if (duration.value <= 0) return 0
  return (currentTime.value / duration.value) * 100
})

function formatTime(seconds) {
  const s = Math.floor(seconds || 0)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${sec.toString().padStart(2, '0')}`
}

function initAudio() {
  if (typeof window === 'undefined') return
  audio = new Audio()
  audio.addEventListener('timeupdate', onTimeUpdate)
  audio.addEventListener('loadedmetadata', onLoadedMetadata)
  audio.addEventListener('ended', onTrackEnded)
}

function onTimeUpdate() {
  currentTime.value = audio.currentTime
}

function onLoadedMetadata() {
  duration.value = audio.duration
}

function onTrackEnded() {
  currentIdx.value = nextIndex(currentIdx.value, playlist.value.length)
  loadAndPlay()
}

function loadTrack() {
  if (!audio || playlist.value.length === 0) return
  const track = playlist.value[currentIdx.value]
  if (track) {
    audio.src = track.filePath
    audio.load()
    currentTime.value = 0
    duration.value = 0
  }
}

function loadAndPlay() {
  loadTrack()
  if (audio) {
    audio.play().catch(() => {})
    isPlaying.value = true
  }
}

function togglePlay() {
  if (!audio || playlist.value.length === 0) return
  if (isPlaying.value) {
    audio.pause()
    isPlaying.value = false
  } else {
    audio.play().catch(() => {})
    isPlaying.value = true
  }
}

function playNext() {
  if (playlist.value.length === 0) return
  currentIdx.value = nextIndex(currentIdx.value, playlist.value.length)
  loadAndPlay()
}

function playPrev() {
  if (playlist.value.length === 0) return
  currentIdx.value = prevIndex(currentIdx.value, playlist.value.length)
  loadAndPlay()
}

function seekTo(event) {
  if (!audio || duration.value <= 0) return
  const bar = event.currentTarget
  const rect = bar.getBoundingClientRect()
  const ratio = (event.clientX - rect.left) / rect.width
  audio.currentTime = ratio * duration.value
}

onMounted(async () => {
  try {
    const list = await fetchMusicList()
    if (list && list.length > 0) {
      playlist.value = list
      initAudio()
      loadTrack()
    }
  } catch (e) {
    // silently fail — no music player shown
  }
})

onBeforeUnmount(() => {
  if (audio) {
    audio.pause()
    audio.removeEventListener('timeupdate', onTimeUpdate)
    audio.removeEventListener('loadedmetadata', onLoadedMetadata)
    audio.removeEventListener('ended', onTrackEnded)
    audio = null
  }
})
</script>

<style scoped>
.music-player {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-top: 1px solid var(--color-border);
  color: var(--color-text);
  font-family: var(--font-body, 'DM Sans', sans-serif);
  position: relative;
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.06);
}

/* Thin progress bar at top of player */
.player-progress-bar {
  width: 100%;
  height: 3px;
  background: rgba(0, 0, 0, 0.04);
  cursor: pointer;
  position: relative;
}

.player-progress-fill {
  height: 100%;
  background: var(--color-accent, #c8956c);
  transition: width 0.3s linear;
  border-radius: 0 2px 2px 0;
}

.player-progress-bar:hover .player-progress-fill {
  height: 5px;
  margin-top: -1px;
}

/* Main content row */
.player-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  gap: 1rem;
}

/* Track info */
.player-track-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
  flex: 1;
}

.player-icon {
  color: var(--color-accent, #c8956c);
  font-size: 0.9rem;
  flex-shrink: 0;
}

.player-track-name {
  font-size: 0.85rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.01em;
}

/* Controls */
.player-controls {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex-shrink: 0;
}

.player-btn {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 0.4rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.player-btn:hover {
  color: white;
  background: rgba(0, 0, 0, 0.04);
}

.player-btn--play {
  background: var(--color-accent, #c8956c);
  color: white;
  width: 36px;
  height: 36px;
  margin: 0 0.25rem;
}

.player-btn--play:hover {
  background: var(--color-accent-hover, #b07d56);
  color: white;
  transform: scale(1.05);
}

/* Time display */
.player-time {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  white-space: nowrap;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
  min-width: 80px;
  text-align: right;
}

/* Slide animation */
.player-slide-enter-active {
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.4s ease;
}

.player-slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}

.player-slide-enter-from,
.player-slide-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .player-content {
    padding: 0.4rem 1rem;
  }

  .player-track-name {
    max-width: 120px;
  }

  .player-time {
    display: none;
  }
}
</style>
