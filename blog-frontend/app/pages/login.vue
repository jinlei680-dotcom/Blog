<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-decoration">
        <span class="auth-icon">✦</span>
        <h1 class="auth-title">Welcome Back</h1>
        <p class="auth-tagline">继续你的旅途故事</p>
      </div>

      <form class="auth-form" @submit.prevent="handleLogin" novalidate>
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

        <div class="form-group" :class="{ 'has-error': errors.password }">
          <label class="form-label" for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
            autocomplete="current-password"
            @blur="validatePassword"
          />
          <Transition name="fade">
            <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
          </Transition>
        </div>

        <Transition name="fade">
          <div v-if="apiError" class="api-error">{{ apiError }}</div>
        </Transition>

        <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
          <span v-if="loading" class="btn-spinner"></span>
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <p class="auth-switch">
        还没有账号？
        <NuxtLink to="/register" class="auth-link">立即注册</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuth } from '~/composables/useAuth'

const { login } = useAuth()

const form = reactive({
  username: '',
  password: ''
})

const errors = reactive({
  username: '',
  password: ''
})

const apiError = ref('')
const loading = ref(false)

function validateUsername() {
  errors.username = form.username.trim() ? '' : '请输入用户名'
  return !errors.username
}

function validatePassword() {
  errors.password = form.password ? '' : '请输入密码'
  return !errors.password
}

function validateForm() {
  const u = validateUsername()
  const p = validatePassword()
  return u && p
}

async function handleLogin() {
  apiError.value = ''
  if (!validateForm()) return

  loading.value = true
  try {
    await login(form.username.trim(), form.password)
    navigateTo('/')
  } catch (err) {
    const msg = err?.data?.message || err?.message || '登录失败，请重试'
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
  background:
    radial-gradient(ellipse at 30% 20%, rgba(200, 149, 108, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(200, 149, 108, 0.04) 0%, transparent 50%),
    linear-gradient(180deg, var(--color-bg) 0%, var(--color-bg-warm) 100%);
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
  color: var(--color-dark);
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
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--color-border-light);
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
  background: var(--color-bg);
  border: 1.5px solid var(--color-border);
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
  background: var(--color-surface);
  box-shadow: 0 0 0 3px var(--color-accent-light);
}

.has-error .form-input {
  border-color: #d4956c;
}

.form-error {
  display: block;
  font-family: var(--font-chinese);
  font-size: 0.8rem;
  color: #c07a50;
  margin-top: var(--space-xs);
  letter-spacing: 0.01em;
}

.api-error {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: #c07a50;
  background: rgba(200, 149, 108, 0.08);
  border: 1px solid rgba(200, 149, 108, 0.2);
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
  background: var(--color-dark);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-accent);
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(200, 149, 108, 0.3);
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

/* Transitions */
.fade-enter-active { transition: all 0.25s ease-out; }
.fade-leave-active { transition: all 0.15s ease-in; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-4px); }

@media (max-width: 768px) {
  .auth-page { padding: var(--space-lg); }
  .auth-form { padding: var(--space-lg); }
  .auth-title { font-size: 1.8rem; }
}
</style>
