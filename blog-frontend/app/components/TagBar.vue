<template>
  <div v-if="tags && tags.length" class="tag-bar">
    <span class="tag-bar-label">🏷️ 标签</span>
    <div class="tag-bar-list">
      <NuxtLink
        v-for="tag in tags"
        :key="tag.id"
        :to="`/tags/${tag.id}`"
        class="tag-pill"
      >
        {{ tag.name }}
      </NuxtLink>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useTag } from '~/composables/useTag'

const { fetchTags } = useTag()
const tags = ref([])

onMounted(async () => {
  try {
    tags.value = await fetchTags()
  } catch (err) {
    console.error('Failed to load tags:', err)
  }
})
</script>

<style scoped>
.tag-bar {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) 0;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tag-bar::-webkit-scrollbar {
  display: none;
}

.tag-bar-label {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  white-space: nowrap;
  flex-shrink: 0;
}

.tag-bar-list {
  display: flex;
  gap: 0.5rem;
  flex-wrap: nowrap;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tag-bar-list::-webkit-scrollbar {
  display: none;
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 1rem;
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  font-weight: 500;
  color: var(--color-accent);
  background: var(--color-accent-light);
  border: 1.5px solid transparent;
  border-radius: 100px;
  white-space: nowrap;
  text-decoration: none;
  transition: all var(--transition-fast);
  cursor: pointer;
  flex-shrink: 0;
}

.tag-pill:hover {
  background: var(--color-accent);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(200, 149, 108, 0.25);
}

.tag-pill.router-link-active {
  background: var(--color-accent);
  color: white;
}
</style>
