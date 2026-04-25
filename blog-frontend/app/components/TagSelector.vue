<template>
  <div class="tag-selector">
    <label class="tag-selector-label">标签</label>
    <div v-if="loading" class="tag-selector-loading">加载标签中...</div>
    <div v-else-if="allTags.length === 0" class="tag-selector-empty">暂无标签</div>
    <div v-else class="tag-selector-list">
      <button
        v-for="tag in allTags"
        :key="tag.id"
        type="button"
        class="tag-toggle"
        :class="{ 'tag-toggle--active': selectedIds.has(tag.id) }"
        @click="toggle(tag.id)"
      >
        <span class="tag-toggle-check">{{ selectedIds.has(tag.id) ? '✓' : '' }}</span>
        {{ tag.name }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useTag } from '~/composables/useTag'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const { fetchTags } = useTag()
const allTags = ref([])
const loading = ref(false)

const selectedIds = computed(() => new Set(props.modelValue))

function toggle(id) {
  const current = new Set(props.modelValue)
  if (current.has(id)) {
    current.delete(id)
  } else {
    current.add(id)
  }
  emit('update:modelValue', Array.from(current))
}

onMounted(async () => {
  loading.value = true
  try {
    allTags.value = await fetchTags()
  } catch (err) {
    console.error('Failed to load tags:', err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.tag-selector {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.tag-selector-label {
  font-family: var(--font-chinese);
  font-size: 0.88rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  letter-spacing: 0.03em;
}

.tag-selector-loading,
.tag-selector-empty {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  padding: var(--space-sm) 0;
}

.tag-selector-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-toggle {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.4rem 1rem;
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: 100px;
  cursor: pointer;
  transition: all var(--transition-fast);
  outline: none;
}

.tag-toggle:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: var(--color-accent-light);
}

.tag-toggle--active {
  background: var(--color-accent);
  border-color: var(--color-accent);
  color: white;
}

.tag-toggle--active:hover {
  background: var(--color-accent-hover);
  color: white;
}

.tag-toggle-check {
  font-size: 0.7rem;
  min-width: 0.8rem;
}
</style>
