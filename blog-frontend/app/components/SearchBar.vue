<template>
  <div class="search-bar">
    <input
      v-model="keyword"
      type="text"
      class="search-input"
      placeholder="搜索文章..."
      aria-label="搜索文章"
      @keydown.enter="handleSearch"
    />
    <button class="search-btn" aria-label="搜索" @click="handleSearch">
      <span class="search-icon">🔍</span>
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const keyword = ref('')

function handleSearch() {
  const q = keyword.value.trim()
  if (!q) return
  navigateTo({ path: '/search', query: { q } })
  keyword.value = ''
}
</script>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  background: var(--color-bg-warm, #f5f0ea);
  border: 1.5px solid var(--color-border, #e8e4df);
  border-radius: 100px;
  padding: 0.2rem 0.3rem 0.2rem 0.8rem;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  max-width: 180px;
}

.search-bar:focus-within {
  border-color: var(--color-accent, #c8956c);
  background: var(--color-surface, #ffffff);
  box-shadow: 0 2px 12px rgba(200, 149, 108, 0.15);
}

.search-input {
  border: none;
  background: transparent;
  outline: none;
  font-family: var(--font-chinese, 'PingFang SC', sans-serif);
  font-size: 0.82rem;
  color: var(--color-text, #2c2c2c);
  width: 100%;
  padding: 0.3rem 0;
}

.search-input::placeholder {
  color: var(--color-text-muted, #999999);
  font-size: 0.8rem;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.3rem;
  border-radius: 50%;
  transition: all 0.2s;
  flex-shrink: 0;
}

.search-btn:hover {
  background: var(--color-accent-light, rgba(200, 149, 108, 0.1));
}

.search-icon {
  font-size: 0.85rem;
  line-height: 1;
}

@media (max-width: 768px) {
  .search-bar {
    max-width: 140px;
  }
}
</style>
