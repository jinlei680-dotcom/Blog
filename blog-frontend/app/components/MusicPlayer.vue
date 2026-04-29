<template>
  <ClientOnly>
    <div v-if="playlist.length > 0" class="music-player">
      <!-- Progress bar at top -->
      <div class="player-progress" ref="progressRef" @click="seek">
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          <div class="progress-thumb" :style="{ left: progressPercent + '%' }"></div>
        </div>
      </div>

      <div class="player-body">
        <!-- Cover / Icon -->
        <div class="player-cover" @click="toggleList">
          <span class="cover-icon">♪</span>
        </div>

        <!-- Song info -->
        <div class="player-info">
          <span class="song-name">{{ currentTrack?.name || '未知歌曲' }}</span>
          <span class="song-time">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
        </div>

        <!-- Controls -->
        <div class="player-controls">
          <button class="ctrl-btn prev" @click="prev" aria-label="上一首">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 6h2v12H6zm3.5 6l8.5 6V6z"/>
            </svg>
          </button>

          <button class="ctrl-btn play" @click="togglePlay" aria-label="播放/暂停">
            <svg v-if="!isPlaying" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
              <path d="M8 5v14l11-7z"/>
            </svg>
            <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
            </svg>
          </button>

          <button class="ctrl-btn next" @click="next" aria-label="下一首">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"/>
            </svg>
          </button>
        </div>

        <!-- Volume & list -->
        <div class="player-right">
          <div class="volume-wrap">
            <button class="ctrl-btn vol-btn" @click="toggleMute" aria-label="音量">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path v-if="!muted && volume > 0.5" d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/>
                <path v-else-if="!muted" d="M3 9v6h4l5 5V4L7 9H3zm10 .22v-1.56c.41.38.76.83 1.03 1.34-.27.51-.62.96-1.03 1.34v-1.56l-.01-.01z"/>
                <path v-else d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3z"/>
              </svg>
            </button>
            <input
              type="range" class="volume-slider" min="0" max="1" step="0.05"
              :value="muted ? 0 : volume" @input="setVolume"
            />
          </div>
          <button class="ctrl-btn list-btn" @click="toggleList" aria-label="播放列表">
            <span class="list-count">{{ currentIdx + 1 }}/{{ playlist.length }}</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" class="list-arrow" :class="{ up: showList }">
              <path d="M7 10l5 5 5-5z"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Song list -->
      <Transition name="list-slide">
        <div v-if="showList" class="song-list">
          <div
            v-for="(song, idx) in playlist"
            :key="song.id"
            class="song-item"
            :class="{ active: idx === currentIdx }"
            @click="selectTrack(idx)"
          >
            <span class="item-idx">{{ idx + 1 }}</span>
            <span class="item-name">{{ song.name }}</span>
            <span v-if="idx === currentIdx" class="item-playing">♪</span>
          </div>
        </div>
      </Transition>
    </div>
  </ClientOnly>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const playlist = ref([])
const currentIdx = ref(0)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(0.7)
const muted = ref(false)
const showList = ref(false)
const progressRef = ref(null)

let audio = null

const currentTrack = computed(() => playlist.value[currentIdx.value] || null)

const progressPercent = computed(() => {
  if (!duration.value) return 0
  return (currentTime.value / duration.value) * 100
})

onMounted(async () => {
  try {
    const res = await $fetch('/api/music')
    playlist.value = res.data || []
    if (playlist.value.length > 0) {
      initAudio()
    }
  } catch {
    // silently fail
  }
})

onBeforeUnmount(() => {
  if (audio) {
    audio.pause()
    audio = null
  }
})

function initAudio() {
  audio = new Audio()
  audio.volume = volume.value
  audio.addEventListener('timeupdate', () => { currentTime.value = audio.currentTime })
  audio.addEventListener('loadedmetadata', () => { duration.value = audio.duration })
  audio.addEventListener('ended', next)
  audio.addEventListener('play', () => { isPlaying.value = true })
  audio.addEventListener('pause', () => { isPlaying.value = false })
  loadTrack()
}

function loadTrack() {
  if (!audio || !currentTrack.value) return
  audio.src = currentTrack.value.filePath
  audio.load()
  currentTime.value = 0
  duration.value = 0
}

function togglePlay() {
  if (!audio) return
  if (isPlaying.value) {
    audio.pause()
  } else {
    audio.play().catch(() => {})
  }
}

function next() {
  if (playlist.value.length === 0) return
  currentIdx.value = (currentIdx.value + 1) % playlist.value.length
  loadTrack()
  if (isPlaying.value) audio.play().catch(() => {})
}

function prev() {
  if (playlist.value.length === 0) return
  currentIdx.value = (currentIdx.value - 1 + playlist.value.length) % playlist.value.length
  loadTrack()
  if (isPlaying.value) audio.play().catch(() => {})
}

function selectTrack(idx) {
  currentIdx.value = idx
  showList.value = false
  loadTrack()
  audio.play().catch(() => {})
}

function seek(e) {
  if (!audio || !duration.value) return
  const rect = progressRef.value.getBoundingClientRect()
  const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  audio.currentTime = ratio * duration.value
}

function toggleMute() {
  if (!audio) return
  muted.value = !muted.value
  audio.volume = muted.value ? 0 : volume.value
}

function setVolume(e) {
  volume.value = parseFloat(e.target.value)
  muted.value = volume.value === 0
  if (audio) audio.volume = volume.value
}

function toggleList() {
  showList.value = !showList.value
}

function formatTime(seconds) {
  const s = Math.floor(seconds || 0)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${sec.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.music-player {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.06);
  font-family: var(--font-body, 'Inter', sans-serif);
}

/* Progress bar */
.player-progress {
  height: 4px;
  cursor: pointer;
  position: relative;
}
.player-progress:hover { height: 6px; margin-top: -1px; }
.progress-track {
  height: 100%;
  background: rgba(0, 0, 0, 0.06);
  position: relative;
}
.progress-fill {
  height: 100%;
  background: var(--color-accent, #c8956c);
  border-radius: 0 2px 2px 0;
  transition: width 0.15s linear;
}
.progress-thumb {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 10px;
  height: 10px;
  background: var(--color-accent, #c8956c);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.15s;
}
.player-progress:hover .progress-thumb { opacity: 1; }

/* Body */
.player-body {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

/* Cover */
.player-cover {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #c8956c, #e8c4a2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.2s;
}
.player-cover:hover { transform: scale(1.05); }
.cover-icon {
  color: white;
  font-size: 1.1rem;
}

/* Info */
.player-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}
.song-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text, #2c1810);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.song-time {
  font-size: 0.7rem;
  color: var(--color-text-muted, #7a6e65);
  font-variant-numeric: tabular-nums;
}

/* Controls */
.player-controls {
  display: flex;
  align-items: center;
  gap: 0.15rem;
  flex-shrink: 0;
}
.ctrl-btn {
  background: none;
  border: none;
  color: var(--color-text, #2c1810);
  cursor: pointer;
  padding: 0.4rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.ctrl-btn:hover { background: rgba(0, 0, 0, 0.05); }
.ctrl-btn.play {
  width: 42px;
  height: 42px;
  background: var(--color-accent, #c8956c);
  color: white;
}
.ctrl-btn.play:hover {
  background: var(--color-accent-hover, #b07d56);
  transform: scale(1.06);
}

/* Right section */
.player-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}
.volume-wrap {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}
.volume-slider {
  width: 60px;
  height: 3px;
  -webkit-appearance: none;
  appearance: none;
  background: rgba(0, 0, 0, 0.12);
  border-radius: 2px;
  outline: none;
  cursor: pointer;
}
.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--color-accent, #c8956c);
  cursor: pointer;
}
.list-btn {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  padding: 0.3rem 0.5rem;
  border-radius: 6px;
}
.list-count {
  font-size: 0.7rem;
  color: var(--color-text-muted, #7a6e65);
  font-variant-numeric: tabular-nums;
}
.list-arrow { transition: transform 0.2s; }
.list-arrow.up { transform: rotate(180deg); }

/* Song list */
.song-list {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  max-height: 280px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -2px 16px rgba(0, 0, 0, 0.04);
}
.song-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 1.5rem;
  cursor: pointer;
  transition: background 0.12s;
  max-width: 1200px;
  margin: 0 auto;
}
.song-item:hover { background: rgba(0, 0, 0, 0.03); }
.song-item.active { background: rgba(200, 149, 108, 0.07); }
.item-idx {
  font-size: 0.75rem;
  color: var(--color-text-muted, #7a6e65);
  width: 20px;
  text-align: right;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}
.item-name {
  font-size: 0.88rem;
  color: var(--color-text, #2c1810);
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-playing {
  color: var(--color-accent, #c8956c);
  font-size: 0.8rem;
  flex-shrink: 0;
}

/* Transitions */
.list-slide-enter-active, .list-slide-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}
.list-slide-enter-from, .list-slide-leave-to {
  max-height: 0;
  opacity: 0;
}
.list-slide-enter-to, .list-slide-leave-from {
  max-height: 280px;
  opacity: 1;
}

@media (max-width: 768px) {
  .player-body { padding: 0.4rem 1rem; gap: 0.6rem; }
  .volume-wrap { display: none; }
  .player-cover { width: 34px; height: 34px; }
  .ctrl-btn.play { width: 36px; height: 36px; }
}
</style>
