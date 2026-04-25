<template>
  <ClientOnly>
    <div class="markdown-renderer">
      <MdPreview
        :modelValue="sanitizedContent"
        :theme="'light'"
        :language="'en-US'"
      />
    </div>
    <template #fallback>
      <div class="markdown-renderer-fallback" v-html="fallbackHtml"></div>
    </template>
  </ClientOnly>
</template>

<script setup>
import { computed } from 'vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { sanitizeHtml } from '~/utils/markdown'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

const sanitizedContent = computed(() => {
  if (!props.content) return ''
  // MdPreview handles rendering internally; we sanitize the input to remove
  // any embedded raw HTML XSS payloads before passing to the renderer
  return sanitizeHtml(props.content)
})

const fallbackHtml = computed(() => {
  if (!props.content) return ''
  // Simple fallback: escape HTML and preserve line breaks
  return props.content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
})
</script>

<style scoped>
.markdown-renderer {
  font-family: var(--font-chinese);
  font-size: 1.05rem;
  line-height: 2;
  color: var(--color-text);
  letter-spacing: 0.02em;
  word-break: break-word;
}

.markdown-renderer-fallback {
  font-family: var(--font-chinese);
  font-size: 1.05rem;
  line-height: 2;
  color: var(--color-text);
  letter-spacing: 0.02em;
  word-break: break-word;
}
</style>

<style>
/* Global styles for MdPreview to match blog warm theme */
.markdown-renderer .md-editor {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.markdown-renderer .md-editor-preview-wrapper {
  padding: 0 !important;
  background: transparent !important;
}

.markdown-renderer .md-editor-preview {
  font-family: var(--font-chinese) !important;
  color: var(--color-text) !important;
  line-height: 2 !important;
  font-size: 1.05rem !important;
}

.markdown-renderer .md-editor-preview h1 {
  font-size: 1.8rem !important;
  font-weight: 700 !important;
  color: var(--color-dark) !important;
  margin: 1.5em 0 0.8em !important;
  padding-bottom: 0.3em !important;
  border-bottom: 1px solid var(--color-border-light) !important;
}

.markdown-renderer .md-editor-preview h2 {
  font-size: 1.5rem !important;
  font-weight: 600 !important;
  color: var(--color-dark) !important;
  margin: 1.3em 0 0.6em !important;
}

.markdown-renderer .md-editor-preview h3 {
  font-size: 1.25rem !important;
  font-weight: 600 !important;
  color: var(--color-dark) !important;
  margin: 1.2em 0 0.5em !important;
}

.markdown-renderer .md-editor-preview p {
  margin: 0.8em 0 !important;
}

.markdown-renderer .md-editor-preview a {
  color: var(--color-accent) !important;
  text-decoration: underline !important;
  text-underline-offset: 2px !important;
}

.markdown-renderer .md-editor-preview a:hover {
  color: var(--color-accent-hover) !important;
}

.markdown-renderer .md-editor-preview blockquote {
  border-left: 4px solid var(--color-accent) !important;
  background: var(--color-accent-light) !important;
  padding: 0.8em 1.2em !important;
  margin: 1em 0 !important;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0 !important;
  color: var(--color-text-secondary) !important;
}

.markdown-renderer .md-editor-preview code {
  background: var(--color-bg-warm) !important;
  color: var(--color-accent-hover) !important;
  border-radius: 4px !important;
  padding: 0.15em 0.4em !important;
  font-size: 0.9em !important;
}

.markdown-renderer .md-editor-preview pre {
  background: #1a1a2e !important;
  border-radius: var(--radius-sm) !important;
  padding: 1.2em !important;
  margin: 1em 0 !important;
  overflow-x: auto !important;
}

.markdown-renderer .md-editor-preview pre code {
  background: transparent !important;
  color: #e0e0e0 !important;
  padding: 0 !important;
  font-size: 0.88rem !important;
  line-height: 1.6 !important;
}

.markdown-renderer .md-editor-preview ul,
.markdown-renderer .md-editor-preview ol {
  padding-left: 1.5em !important;
  margin: 0.8em 0 !important;
}

.markdown-renderer .md-editor-preview li {
  margin: 0.3em 0 !important;
}

.markdown-renderer .md-editor-preview table {
  border-collapse: collapse !important;
  width: 100% !important;
  margin: 1em 0 !important;
}

.markdown-renderer .md-editor-preview th,
.markdown-renderer .md-editor-preview td {
  border: 1px solid var(--color-border) !important;
  padding: 0.6em 1em !important;
  text-align: left !important;
}

.markdown-renderer .md-editor-preview th {
  background: var(--color-bg-warm) !important;
  font-weight: 600 !important;
}

.markdown-renderer .md-editor-preview hr {
  border: none !important;
  border-top: 1px solid var(--color-border-light) !important;
  margin: 2em 0 !important;
}

.markdown-renderer .md-editor-preview img {
  max-width: 100% !important;
  border-radius: var(--radius-sm) !important;
  margin: 1em 0 !important;
}
</style>
