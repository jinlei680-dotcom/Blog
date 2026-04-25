<template>
  <div class="error-page">
    <div class="error-container">
      <div class="error-visual">
        <span class="error-code">{{ error?.statusCode || 500 }}</span>
        <div class="error-decoration">✦</div>
      </div>

      <h1 class="error-title">{{ errorTitle }}</h1>
      <p class="error-message">{{ errorMessage }}</p>

      <div class="error-actions">
        <button class="btn btn-primary" @click="handleBack">
          ← 返回首页
        </button>
        <button v-if="error?.statusCode !== 404" class="btn btn-ghost" @click="handleRetry">
          重试
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  error: Object
})

const errorTitle = computed(() => {
  const code = props.error?.statusCode
  if (code === 404) return '页面不存在'
  if (code === 403) return '无权访问'
  if (code === 401) return '请先登录'
  return '出了点问题'
})

const errorMessage = computed(() => {
  const code = props.error?.statusCode
  if (code === 404) return '你访问的页面可能已被移除，或者地址输入有误。'
  if (code === 403) return '你没有权限访问此页面。'
  if (code === 401) return '请登录后再访问此页面。'
  return props.error?.message || '服务器开小差了，请稍后再试。'
})

function handleBack() {
  clearError({ redirect: '/' })
}

function handleRetry() {
  clearError({ redirect: window.location.pathname })
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;0,800;1,400;1,600&family=Source+Han+Sans+SC:wght@300;400;500;700&family=DM+Sans:ital,wght@0,400;0,500;0,700;1,400&display=swap');
</style>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(ellipse at 30% 40%, rgba(200, 149, 108, 0.06) 0%, transparent 50%),
    linear-gradient(180deg, #faf8f5 0%, #f5f0ea 100%);
  padding: 2rem;
  font-family: 'DM Sans', 'Source Han Sans SC', 'PingFang SC', sans-serif;
}

.error-container {
  text-align: center;
  max-width: 480px;
  animation: fadeUp 0.6s ease-out;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

.error-visual {
  position: relative;
  margin-bottom: 2rem;
}

.error-code {
  font-family: 'Playfair Display', Georgia, serif;
  font-size: clamp(5rem, 15vw, 8rem);
  font-weight: 800;
  color: rgba(200, 149, 108, 0.15);
  line-height: 1;
  letter-spacing: -0.04em;
  user-select: none;
}

.error-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 2rem;
  color: #c8956c;
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.15); }
}

.error-title {
  font-family: 'Source Han Sans SC', 'PingFang SC', sans-serif;
  font-size: 1.6rem;
  font-weight: 700;
  color: #2c2c2c;
  margin-bottom: 0.6rem;
}

.error-message {
  font-family: 'Source Han Sans SC', 'PingFang SC', sans-serif;
  font-size: 0.95rem;
  color: #6b6b6b;
  line-height: 1.7;
  margin-bottom: 2.5rem;
}

.error-actions {
  display: flex;
  gap: 0.8rem;
  justify-content: center;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: 'Source Han Sans SC', 'PingFang SC', sans-serif;
  font-size: 0.9rem;
  font-weight: 500;
  padding: 0.7rem 1.8rem;
  border-radius: 100px;
  border: none;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
}

.btn-primary {
  background: #1a1a1a;
  color: white;
}

.btn-primary:hover {
  background: #c8956c;
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(200, 149, 108, 0.3);
}

.btn-ghost {
  background: transparent;
  color: #6b6b6b;
  border: 1.5px solid #e8e4df;
}

.btn-ghost:hover {
  border-color: #c8956c;
  color: #c8956c;
}

@media (max-width: 480px) {
  .error-actions {
    flex-direction: column;
  }
}
</style>
