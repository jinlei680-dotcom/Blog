<template>
  <ClientOnly>
    <div class="md-editor-wrapper">
      <MdEditor
        v-model="content"
        :placeholder="placeholder"
        :theme="'light'"
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
  box-shadow: 0 0 0 3px rgba(0, 184, 148, 0.15);
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
/* Global overrides for md-editor-v3 light theme */
.md-editor {
  --md-bk-color: rgba(255, 255, 255, 0.9) !important;
  --md-border-color: var(--color-border) !important;
  --md-color: var(--color-text) !important;
  font-family: var(--font-chinese) !important;
}

.md-editor .md-editor-toolbar-wrapper {
  background: rgba(250, 249, 246, 0.95) !important;
  border-bottom: 1px solid var(--color-border-light) !important;
}

.md-editor .md-editor-toolbar .md-editor-toolbar-item {
  color: var(--color-text-secondary) !important;
}

.md-editor .md-editor-toolbar .md-editor-toolbar-item:hover {
  color: var(--color-accent) !important;
  background: var(--color-accent-light) !important;
}

.md-editor .md-editor-input-wrapper {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace !important;
}

.md-editor .md-editor-preview {
  font-family: var(--font-chinese) !important;
  color: var(--color-text) !important;
  line-height: 2 !important;
}

.md-editor .md-editor-preview h1,
.md-editor .md-editor-preview h2,
.md-editor .md-editor-preview h3 {
  color: var(--color-dark) !important;
}

.md-editor .md-editor-preview a {
  color: var(--color-accent) !important;
}

.md-editor .md-editor-preview blockquote {
  border-left-color: var(--color-accent) !important;
  background: var(--color-accent-light) !important;
}

.md-editor .md-editor-preview code {
  background: var(--color-accent-light) !important;
  color: var(--color-accent-hover) !important;
  border-radius: 4px !important;
  padding: 0.15em 0.4em !important;
}
</style>
