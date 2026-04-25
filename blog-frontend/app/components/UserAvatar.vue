<template>
  <div class="user-avatar" :style="{ width: sizeStr, height: sizeStr }">
    <img
      v-if="src"
      :src="src"
      :alt="alt"
      class="avatar-img"
      @error="handleError"
    />
    <div v-else class="avatar-fallback" :style="{ fontSize: fallbackFontSize }">
      {{ initial }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '用户头像' },
  size: { type: Number, default: 48 },
  username: { type: String, default: '' }
})

const imgError = ref(false)

const sizeStr = computed(() => `${props.size}px`)
const fallbackFontSize = computed(() => `${Math.max(14, props.size * 0.4)}px`)
const initial = computed(() => {
  if (props.username) return props.username.charAt(0).toUpperCase()
  return '?'
})

function handleError() {
  imgError.value = true
}
</script>

<style scoped>
.user-avatar {
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--color-accent-light, rgba(200, 149, 108, 0.1));
  border: 2px solid var(--color-border-light, #f0ece7);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display, 'Playfair Display', serif);
  font-weight: 700;
  color: var(--color-accent, #c8956c);
  background: var(--color-accent-light, rgba(200, 149, 108, 0.1));
}
</style>
