<template>
  <Teleport to="body">
    <Transition name="toc-slide">
      <nav v-if="visible && headings.length > 0" class="article-toc" aria-label="文章目录">
        <p class="toc-title">目录</p>
        <ul class="toc-list">
          <li
            v-for="h in headings"
            :key="h.id"
            class="toc-item"
            :class="[`toc-level-${h.level}`, { 'toc-active': h.id === activeId }]"
          >
            <a :href="`#${h.id}`" class="toc-link" @click.prevent="scrollTo(h.id)">
              {{ h.text }}
            </a>
          </li>
        </ul>
      </nav>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const headings = ref([])
const activeId = ref('')
const visible = ref(false)

let scrollIo = null

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function buildHeadings() {
  const els = document.querySelectorAll('.article-body h1, .article-body h2, .article-body h3')
  headings.value = Array.from(els).map(el => ({
    id: el.id,
    text: el.textContent,
    level: parseInt(el.tagName[1])
  })).filter(h => h.id)
}

function handleScroll() {
  visible.value = window.innerWidth >= 1200 && window.scrollY > 200
}

if (import.meta.client) {
  onMounted(async () => {
    await nextTick()
    buildHeadings()
    handleScroll()
    window.addEventListener('scroll', handleScroll, { passive: true })

    // Track active heading
    const headingEls = document.querySelectorAll('.article-body h1, .article-body h2, .article-body h3')
    if (headingEls.length) {
      scrollIo = new IntersectionObserver((entries) => {
        entries.forEach(e => {
          if (e.isIntersecting) activeId.value = e.target.id
        })
      }, { rootMargin: '-20% 0px -70% 0px' })
      headingEls.forEach(el => scrollIo.observe(el))
    }
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
    scrollIo?.disconnect()
  })
}
</script>

<style scoped>
.article-toc {
  position: fixed;
  top: 50%;
  right: 1.5rem;
  transform: translateY(-50%);
  width: 190px;
  max-height: 55vh;
  overflow-y: auto;
  background: rgba(15, 12, 41, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 184, 148, 0.2);
  border-radius: 12px;
  padding: 1rem;
  z-index: 80;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 184, 148, 0.3) transparent;
}

.article-toc::-webkit-scrollbar { width: 4px; }
.article-toc::-webkit-scrollbar-thumb { background: rgba(0, 184, 148, 0.3); border-radius: 2px; }

.toc-title {
  font-family: var(--font-chinese, sans-serif);
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--color-text-muted);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 0.6rem;
}

.toc-list {
  list-style: none;
}

.toc-item {
  margin-bottom: 0.1rem;
}

.toc-level-2 { padding-left: 0.8rem; }
.toc-level-3 { padding-left: 1.4rem; }

.toc-link {
  display: block;
  padding: 0.3rem 0.5rem;
  font-family: var(--font-chinese, sans-serif);
  font-size: 0.78rem;
  color: var(--color-text-muted);
  border-radius: 6px;
  text-decoration: none;
  line-height: 1.4;
  transition: all 0.2s ease;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toc-link:hover {
  color: var(--color-text);
  background: rgba(0, 184, 148, 0.1);
}

.toc-active .toc-link {
  color: var(--color-accent);
  background: rgba(0, 184, 148, 0.15);
  font-weight: 500;
}

.toc-slide-enter-active { transition: all 0.3s ease-out; }
.toc-slide-leave-active { transition: all 0.25s ease-in; }
.toc-slide-enter-from, .toc-slide-leave-to { opacity: 0; transform: translateY(-50%) translateX(12px); }

@media (max-width: 1200px) {
  .article-toc { display: none; }
}
</style>
