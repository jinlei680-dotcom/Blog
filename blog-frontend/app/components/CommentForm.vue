<template>
  <div class="comment-form" :class="{ 'comment-form--reply': !!parentId }">
    <div v-if="!authStore.isLoggedIn" class="comment-login-prompt">
      <span class="prompt-icon">💬</span>
      <span class="prompt-text">
        <NuxtLink to="/login" class="prompt-link">登录</NuxtLink>后参与评论
      </span>
    </div>
    <form v-else @submit.prevent="handleSubmit">
      <div class="form-header" v-if="parentId && replyTo">
        <span class="reply-indicator">回复 <strong>{{ replyTo }}</strong></span>
        <button type="button" class="cancel-reply" @click="$emit('cancel')">取消</button>
      </div>
      <div class="form-body">
        <textarea
          ref="textareaRef"
          v-model="content"
          class="comment-textarea"
          :placeholder="parentId ? '写下你的回复...' : '写下你的评论...'"
          rows="3"
          maxlength="1000"
        ></textarea>
        <div class="form-footer">
          <span class="char-count" :class="{ 'char-count--warn': content.length > 900 }">
            {{ content.length }}/1000
          </span>
          <button
            type="submit"
            class="submit-btn"
            :disabled="!content.trim() || submitting"
          >
            {{ submitting ? '发送中...' : (parentId ? '回复' : '发表评论') }}
          </button>
        </div>
      </div>
      <p v-if="errorMsg" class="form-error">{{ errorMsg }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useAuthStore } from '~/stores/auth'

const props = defineProps({
  parentId: { type: [Number, null], default: null },
  replyTo: { type: String, default: '' }
})

const emit = defineEmits(['submitted', 'cancel'])

const authStore = useAuthStore()
const content = ref('')
const submitting = ref(false)
const errorMsg = ref('')
const textareaRef = ref(null)

onMounted(() => {
  if (props.parentId && textareaRef.value) {
    nextTick(() => textareaRef.value.focus())
  }
})

async function handleSubmit() {
  if (!content.value.trim()) return
  submitting.value = true
  errorMsg.value = ''
  try {
    emit('submitted', {
      content: content.value.trim(),
      parentId: props.parentId
    })
    content.value = ''
  } catch (err) {
    errorMsg.value = err.data?.message || '评论失败，请重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.comment-form {
  margin-bottom: var(--space-lg);
}

.comment-form--reply {
  margin-top: var(--space-sm);
  margin-bottom: var(--space-md);
  padding-left: var(--space-md);
  border-left: 2px solid var(--color-accent-light);
}

/* Login prompt */
.comment-login-prompt {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-lg);
  background: var(--color-bg-warm);
  border-radius: var(--radius-md);
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.prompt-icon {
  font-size: 1.2rem;
}

.prompt-link {
  color: var(--color-accent);
  font-weight: 500;
  transition: color var(--transition-fast);
}

.prompt-link:hover {
  color: var(--color-accent-hover);
  text-decoration: underline;
}

/* Form */
.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-sm);
}

.reply-indicator {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.reply-indicator strong {
  color: var(--color-accent);
  font-weight: 500;
}

.cancel-reply {
  background: none;
  border: none;
  font-family: var(--font-chinese);
  font-size: 0.8rem;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.2rem 0.5rem;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.cancel-reply:hover {
  color: #d44;
  background: rgba(221, 68, 68, 0.06);
}

.form-body {
  position: relative;
}

.comment-textarea {
  width: 100%;
  padding: var(--space-md);
  font-family: var(--font-chinese);
  font-size: 0.92rem;
  line-height: 1.7;
  color: var(--color-text);
  background: var(--color-bg-warm);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  resize: vertical;
  min-height: 80px;
  transition: all var(--transition-fast);
  outline: none;
}

.comment-textarea:focus {
  border-color: var(--color-accent);
  background: var(--color-surface);
  box-shadow: 0 0 0 3px var(--color-accent-light);
}

.comment-textarea::placeholder {
  color: var(--color-text-muted);
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-sm);
}

.char-count {
  font-family: var(--font-chinese);
  font-size: 0.78rem;
  color: var(--color-text-muted);
}

.char-count--warn {
  color: #d44;
}

.submit-btn {
  padding: 0.45rem 1.3rem;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  font-weight: 500;
  color: white;
  background: var(--color-dark);
  border: none;
  border-radius: 100px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-accent);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(200, 149, 108, 0.3);
}

.submit-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.form-error {
  margin-top: var(--space-sm);
  font-family: var(--font-chinese);
  font-size: 0.82rem;
  color: #d44;
}

@media (max-width: 768px) {
  .comment-form--reply {
    padding-left: var(--space-sm);
  }
}
</style>
