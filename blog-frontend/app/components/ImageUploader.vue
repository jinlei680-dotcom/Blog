<template>
  <div class="image-uploader">
    <input
      ref="fileInput"
      type="file"
      accept="image/jpeg,image/png,image/gif,image/webp"
      class="file-input"
      @change="handleFileChange"
    />
    <button type="button" class="upload-btn" @click="triggerFileInput" :disabled="uploading">
      <span v-if="uploading">上传中...</span>
      <span v-else>{{ buttonText }}</span>
    </button>
    <p v-if="errorMsg" class="upload-error">{{ errorMsg }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUpload } from '~/composables/useUpload'

const props = defineProps({
  buttonText: {
    type: String,
    default: '选择图片'
  },
  maxSize: {
    type: Number,
    default: 5 * 1024 * 1024 // 5MB
  }
})

const emit = defineEmits(['uploaded', 'error'])

const { uploadImage } = useUpload()
const fileInput = ref(null)
const uploading = ref(false)
const errorMsg = ref('')

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

function triggerFileInput() {
  fileInput.value?.click()
}

async function handleFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return

  errorMsg.value = ''

  if (!ALLOWED_TYPES.includes(file.type)) {
    errorMsg.value = '不支持的文件格式，仅支持JPEG、PNG、GIF、WebP'
    emit('error', errorMsg.value)
    resetInput()
    return
  }

  if (file.size > props.maxSize) {
    errorMsg.value = '文件大小不能超过5MB'
    emit('error', errorMsg.value)
    resetInput()
    return
  }

  uploading.value = true
  try {
    const result = await uploadImage(file)
    emit('uploaded', result.url)
  } catch (e) {
    const msg = e?.data?.message || e?.message || '图片上传失败'
    errorMsg.value = msg
    emit('error', msg)
  } finally {
    uploading.value = false
    resetInput()
  }
}

function resetInput() {
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
</script>

<style scoped>
.image-uploader {
  display: inline-block;
}

.file-input {
  display: none;
}

.upload-btn {
  padding: 0.5rem 1rem;
  background: var(--color-accent, #c97b3a);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm, 6px);
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.2s;
}

.upload-btn:hover:not(:disabled) {
  background: var(--color-accent-hover, #b06a2f);
}

.upload-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.upload-error {
  color: #d32f2f;
  font-size: 0.85rem;
  margin-top: 0.4rem;
}
</style>
