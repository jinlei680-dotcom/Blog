<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-decoration">
        <span class="auth-icon">✦</span>
        <h1 class="auth-title">Join Wanderlust</h1>
        <p class="auth-tagline">开始记录你的旅途故事</p>
      </div>

      <form class="auth-form" @submit.prevent="handleRegister" novalidate>
        <div class="form-group" :class="{ 'has-error': errors.username }">
          <label class="form-label" for="username">用户名</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名"
            autocomplete="username"
            @blur="validateUsername"
          />
          <Transition name="fade">
            <span v-if="errors.username" class="form-error">{{ errors.username }}</span>
          </Transition>
        </div>

        <div class="form-group" :class="{ 'has-error': errors.email }">
          <label class="form-label" for="email">邮箱</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            class="form-input"
            placeholder="请输入邮箱地址"
            autocomplete="email"
            @blur="validateEmail"
          />
          <Transition name="fade">
            <span v-if="errors.email" class="form-error">{{ errors.email }}</span>
          </Transition>
        </div>

        <div class="form-group" :class="{ 'has-error': errors.password }">
          <label class="form-label" for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="至少 6 位字符"
            autocomplete="new-password"
            @blur="validatePassword"
          />
          <Transition name="fade">
            <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
          </Transition>
        </div>

        <Transition name="fade">
          <div v-if="apiError" class="api-error">{{ apiError }}</div>
        </Transition>

        <Transition name="fade">
          <div v-if="success" class="api-success">注册成功！正在跳转登录页...</div>
        </Transition>

        <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
          <span v-if="loading" class="btn-spinner"></span>
          {{ loading ? '注册中...' : '创建账号' }}
        </button>
      </form>

      <p class="auth-switch">
        已有账号？
        <NuxtLink to="/login" class="auth-link">立即登录</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuth } from '~/composables/useAuth'

const { register } = useAuth()

const form = reactive({
  username: '',
  email: '',
  password: ''
})

const errors = reactive({
  username: '',
  email: '',
  password: ''
})

const apiError = ref('')
const success = ref(false)
const loading = ref(false)

function validateUsername() {
  errors.username = form.username.trim() ? '' : '请输入用户名'
  return !errors.username
}

function validateEmail() {
  const email = form.email.trim()
  if (!email) {
    errors.email = '请输入邮箱'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errors.email = '请输入有效的邮箱地址'
  } else {
    errors.email = ''
  }
  return !errors.email
}

function validatePassword() {
  if (!form.password) {
    errors.password = '请输入密码'
  } else if (form.password.length < 6) {
    errors.password = '密码至少需要 6 位字符'
  } else {
    errors.password = ''
  }
  return !errors.password
}

function validateForm() {
  const u = validateUsername()
  const e = validateEmail()
  const p = validatePassword()
  return u && e && p
}

async function handleRegister() {
  apiError.value = ''
  success.value = false
  if (!validateForm()) return

  loading.value = true
  try {
    await register(form.username.trim(), form.password, form.email.trim())
    success.value = true
    setTimeout(() => navigateTo('/login'), 1500)
  } catch (err) {
    const msg = err?.data?.message || err?.message || '注册失败，请重试'
    apiError.value = msg
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - var(--navbar-height));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl);
}

.auth-container {
  width: 100%;
  max-width: 420px;
  animation: authEnter 0.6s ease-out;
}

@keyframes authEnter {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-decoration {
  text-align: center;
  margin-bottom: var(--space-xl);
}

.auth-icon {
  display: inline-block;
  font-size: 1.5rem;
  color: var(--color-accent);
  margin-bottom: var(--space-sm);
  animation: authEnter 0.6s ease-out 0.1s both;
}

.auth-title {
  font-family: var(--font-display);
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #a78bfa, #06b6d4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
  margin-bottom: 0.3rem;
  animation: authEnter 0.6s ease-out 0.15s both;
}

.auth-tagline {
  font-family: var(--font-chinese);
  font-size: 0.95rem;
  color: var(--color-text-muted);
  letter-spacing: 0.08em;
  animation: authEnter 0.6s ease-out 0.2s both;
}

.auth-form {
  background: var(--glass-bg);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--glass-shadow);
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  animation: authEnter 0.6s ease-out 0.25s both;
}

.form-group {
  margin-bottom: var(--space-lg);
}

.form-label {
  display: block;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-xs);
  letter-spacing: 0.02em;
}

.form-input {
  width: 100%;
  padding: 0.75rem 1rem;
  font-family: var(--font-body);
  font-size: 0.95rem;
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.05);
  border: 1.5px solid var(--glass-border);
  border-radius: var(--radius-sm);
  outline: none;
  transition: all var(--transition-fast);
}

.form-input::placeholder {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.form-input:focus {
  border-color: var(--color-accent);
  background: rgba(139, 92, 246, 0.08);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15);
}

.has-error .form-input {
  border-color: #f87171;
}

.form-error {
  display: block;
  font-family: var(--font-chinese);
  font-size: 0.8rem;
  color: #f87171;
  margin-top: var(--space-xs);
  letter-spacing: 0.01em;
}

.api-error {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: #fca5a5;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--radius-sm);
  padding: 0.6rem 1rem;
  margin-bottom: var(--space-lg);
  text-align: center;
}

.api-success {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: #6ee7b7;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.25);
  border-radius: var(--radius-sm);
  padding: 0.6rem 1rem;
  margin-bottom: var(--space-lg);
  text-align: center;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-chinese);
  font-size: 0.95rem;
  font-weight: 500;
  padding: 0.8rem 2rem;
  border-radius: 100px;
  border: none;
  cursor: pointer;
  transition: all var(--transition-smooth);
  text-decoration: none;
  letter-spacing: 0.03em;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-cyan));
  color: white;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(139, 92, 246, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-full {
  width: 100%;
}

.btn-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  margin-right: 0.5rem;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-switch {
  text-align: center;
  margin-top: var(--space-lg);
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  color: var(--color-text-muted);
  animation: authEnter 0.6s ease-out 0.35s both;
}

.auth-link {
  color: var(--color-accent);
  font-weight: 500;
  transition: color var(--transition-fast);
}

.auth-link:hover {
  color: var(--color-accent-hover);
}

.fade-enter-active { transition: all 0.25s ease-out; }
.fade-leave-active { transition: all 0.15s ease-in; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-4px); }

@media (max-width: 768px) {
  .auth-page { padding: var(--space-lg); }
  .auth-form { padding: var(--space-lg); }
  .auth-title { font-size: 1.8rem; }
}
</style>
