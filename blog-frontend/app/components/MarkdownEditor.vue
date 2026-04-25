<template>
  <ClientOnly>
    <div class="md-editor-wrapper">
      <MdEditor
        v-model="content"
        :placeholder="placeholder"
        :theme="'dark'"
        :language="'en-US'"
        :style="{ height: '500px' }"
        :toolbars="toolbarItems"
        @onUploadImg="handleUploadImg"
      />
    </div>
    <template #fallback>
      <div class="md-editor-fallback">
        <textarea
          :value="modelValue"
          :placeholder="placeholder"
          class="fallback-textarea"
          @input="$emit('update:modelValue', $event.target.value)"
        ></textarea>
      </div>
    </template>
  </ClientOnly>
</template>

<script setup>
import { computed } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useUpload } from '~/composables/useUpload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '写下你的故事...'
  }
})

const emit = defineEmits(['update:modelValue'])

const { uploadImage } = useUpload()

const content = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const toolbarItems = [
  'bold', 'underline', 'italic', 'strikeThrough',
  '-',
  'title', 'sub', 'sup', 'quote',
  '-',
  'unorderedList', 'orderedList', 'task',
  '-',
  'codeRow', 'code',
  '-',
  'link', 'image', 'table',
  '-',
  'revoke', 'next',
  '=',
  'preview', 'fullscreen'
]

async function handleUploadImg(files, callback) {
  const urls = []
  for (const file of files) {
    try {
      const result = await uploadImage(file)
      urls.push(result.url)
    } catch (e) {
      console.error('图片上传失败:', e)
      // Fallback to object URL if upload fails
      urls.push(URL.createObjectURL(file))
    }
  }
  callback(urls)
}
</script>

<style scoped>
.md-editor-wrapper {
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1.5px solid var(--glass-border);
  transition: border-color var(--transition-fast);
}

.md-editor-wrapper:focus-within {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15);
}

.md-editor-fallback {
  width: 100%;
}

.fallback-textarea {
  width: 100%;
  min-height: 300px;
  font-family: var(--font-chinese);
  font-size: 1rem;
  line-height: 1.9;
  padding: var(--space-lg);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  resize: vertical;
  outline: none;
  transition: all var(--transition-fast);
}

.fallback-textarea:focus {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px var(--color-accent-light);
}

.fallback-textarea::placeholder {
  color: var(--color-text-muted);
}
</style>

<style>
/* Global overrides for md-editor-v3 dark theme */
.md-editor-dark {
  --md-bk-color: rgba(20, 16, 50, 0.95) !important;
  --md-border-color: rgba(255, 255, 255, 0.12) !important;
  --md-color: rgba(255, 255, 255, 0.9) !important;
  font-family: var(--font-chinese) !important;
}

.md-editor-dark .md-editor-toolbar-wrapper {
  background: rgba(10, 8, 30, 0.8) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
}

.md-editor-dark .md-editor-toolbar .md-editor-toolbar-item {
  color: rgba(255, 255, 255, 0.6) !important;
}

.md-editor-dark .md-editor-toolbar .md-editor-toolbar-item:hover {
  color: #a78bfa !important;
  background: rgba(139, 92, 246, 0.1) !important;
}

.md-editor-dark .md-editor-input-wrapper {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace !important;
}

.md-editor-dark .md-editor-preview {
  font-family: var(--font-chinese) !important;
  color: rgba(255, 255, 255, 0.85) !important;
  line-height: 2 !important;
}

.md-editor-dark .md-editor-preview h1,
.md-editor-dark .md-editor-preview h2,
.md-editor-dark .md-editor-preview h3 {
  color: #e2d9fa !important;
}

.md-editor-dark .md-editor-preview a {
  color: #a78bfa !important;
}

.md-editor-dark .md-editor-preview blockquote {
  border-left-color: #8b5cf6 !important;
  background: rgba(139, 92, 246, 0.08) !important;
}

.md-editor-dark .md-editor-preview code {
  background: rgba(139, 92, 246, 0.12) !important;
  color: #c4b5fd !important;
  border-radius: 4px !important;
  padding: 0.15em 0.4em !important;
}
</style>
