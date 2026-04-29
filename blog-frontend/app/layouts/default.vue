<template>
  <div class="app-layout">
    <!-- Subtle light orbs background -->
    <div class="orb-container" aria-hidden="true">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <header class="navbar" :class="{ 'navbar--scrolled': isScrolled }">
      <nav class="navbar-inner">
        <NuxtLink to="/" class="navbar-brand">
          <span class="brand-icon">✦</span>
          <span class="brand-text">Wanderlust</span>
          <span class="brand-sub">生活 · 旅行 · 记录</span>
        </NuxtLink>
        <div class="navbar-links">
          <NuxtLink to="/" class="nav-link">首页</NuxtLink>
          <SearchBar />
          <template v-if="authStore.isLoggedIn">
            <template v-if="authStore.isAdmin">
              <div class="nav-admin-dropdown" @mouseenter="adminMenuOpen = true" @mouseleave="adminMenuOpen = false">
                <button class="nav-link nav-link--admin">
                  <span class="admin-icon">⚙</span> 管理
                </button>
                <Transition name="dropdown-fade">
                  <div v-if="adminMenuOpen" class="admin-dropdown-menu">
                    <NuxtLink to="/admin/tags" class="admin-dropdown-item" @click="adminMenuOpen = false">管理标签</NuxtLink>
                    <NuxtLink to="/admin/music" class="admin-dropdown-item" @click="adminMenuOpen = false">管理音乐</NuxtLink>
                  </div>
                </Transition>
              </div>
            </template>
            <NotificationBell />
            <NuxtLink :to="`/users/${authStore.user?.id}`" class="nav-user">
              <UserAvatar
                :src="authStore.user?.avatarUrl"
                :username="authStore.username"
                :size="28"
              />
              {{ authStore.username }}
            </NuxtLink>
            <button class="nav-link nav-link--logout" @click="handleLogout">退出</button>
          </template>
          <template v-else>
            <NuxtLink to="/login" class="nav-link">登录</NuxtLink>
            <NuxtLink to="/register" class="nav-link nav-link--cta">开始写作</NuxtLink>
          </template>
        </div>
        <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen" aria-label="菜单">
          <span class="hamburger" :class="{ open: mobileMenuOpen }"></span>
        </button>
      </nav>
      <!-- Mobile menu -->
      <Transition name="slide-down">
        <div v-if="mobileMenuOpen" class="mobile-menu">
          <NuxtLink to="/" class="mobile-link" @click="mobileMenuOpen = false">首页</NuxtLink>
          <div class="mobile-search">
            <SearchBar />
          </div>
          <template v-if="authStore.isLoggedIn">
            <template v-if="authStore.isAdmin">
              <NuxtLink to="/admin/tags" class="mobile-link" @click="mobileMenuOpen = false">管理标签</NuxtLink>
              <NuxtLink to="/admin/music" class="mobile-link" @click="mobileMenuOpen = false">管理音乐</NuxtLink>
            </template>
            <NuxtLink :to="`/users/${authStore.user?.id}`" class="mobile-link mobile-user" @click="mobileMenuOpen = false">
              <UserAvatar
                :src="authStore.user?.avatarUrl"
                :username="authStore.username"
                :size="24"
              />
              {{ authStore.username }}
            </NuxtLink>
            <button class="mobile-link mobile-logout-btn" @click="handleLogout">退出登录</button>
          </template>
          <template v-else>
            <NuxtLink to="/login" class="mobile-link" @click="mobileMenuOpen = false">登录</NuxtLink>
            <NuxtLink to="/register" class="mobile-link" @click="mobileMenuOpen = false">开始写作</NuxtLink>
          </template>
        </div>
      </Transition>
    </header>

    <main class="main-content">
      <slot />
    </main>

    <MusicPlayer />

    <footer class="app-footer">
      <div class="footer-inner">
        <div class="footer-top">
          <div class="footer-brand-section">
            <span class="footer-logo">✦ Wanderlust</span>
            <p class="footer-tagline">用脚步丈量世界，用文字记录生活</p>
          </div>
          <div class="footer-links">
            <NuxtLink to="/">首页</NuxtLink>
            <template v-if="!authStore.isLoggedIn">
              <NuxtLink to="/login">登录</NuxtLink>
              <NuxtLink to="/register">注册</NuxtLink>
            </template>
          </div>
        </div>
        <div class="footer-divider"></div>
        <p class="footer-copy">&copy; {{ new Date().getFullYear() }} Wanderlust Blog. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useAuthStore } from '~/stores/auth'
import { useAuth } from '~/composables/useAuth'
import { useNotification } from '~/composables/useNotification'

const authStore = useAuthStore()
const { logout } = useAuth()
const { startPolling, stopPolling } = useNotification()

const isScrolled = ref(false)
const mobileMenuOpen = ref(false)
const adminMenuOpen = ref(false)

function handleScroll() {
  isScrolled.value = window.scrollY > 20
  const h = document.documentElement.scrollHeight - window.innerHeight
  document.documentElement.style.setProperty(
    '--nav-progress',
    (h > 0 ? Math.min(100, window.scrollY / h * 100) : 0) + '%'
  )
}

function handleLogout() {
  mobileMenuOpen.value = false
  stopPolling()
  logout()
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  if (authStore.isLoggedIn) {
    startPolling()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  stopPolling()
})

watch(() => authStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    startPolling()
  } else {
    stopPolling()
  }
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Poppins:wght@400;500;600;700;800&display=swap');

:root {
  /* === Light fresh color system === */
  --color-bg: #faf9f6;
  --color-bg-warm: #f0ede8;
  --color-surface: rgba(255, 255, 255, 0.85);

  /* Glass effects — lighter, iOS-like */
  --glass-bg: rgba(255, 255, 255, 0.80);
  --glass-bg-hover: rgba(255, 255, 255, 0.95);
  --glass-bg-strong: rgba(255, 255, 255, 0.90);
  --glass-border: rgba(0, 0, 0, 0.08);
  --glass-border-accent: rgba(0, 184, 148, 0.35);
  --glass-blur: blur(12px);
  --glass-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);

  /* Primary accent: mint green + sky blue + coral */
  --color-accent: #00b894;
  --color-accent-hover: #009b7d;
  --color-accent-light: rgba(0, 184, 148, 0.10);
  --color-accent-cyan: #0984e3;
  --color-accent-cyan-light: rgba(9, 132, 227, 0.10);
  --color-accent-coral: #ff7675;
  --color-accent-coral-light: rgba(255, 118, 117, 0.10);

  /* Text (dark on light) */
  --color-text: #2d3436;
  --color-text-secondary: #636e72;
  --color-text-muted: #b2bec3;
  --color-dark: #1a1a2e;
  --color-dark-soft: #2d3436;

  /* Borders */
  --color-border: rgba(0, 0, 0, 0.08);
  --color-border-light: rgba(0, 0, 0, 0.04);

  /* Fonts */
  --font-display: 'Poppins', 'Inter', sans-serif;
  --font-body: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-chinese: 'PingFang SC', 'Microsoft YaHei', 'Inter', sans-serif;

  /* Spacing */
  --space-xs: 0.25rem;
  --space-sm: 0.5rem;
  --space-md: 1rem;
  --space-lg: 1.5rem;
  --space-xl: 2.5rem;
  --space-2xl: 4rem;
  --space-3xl: 6rem;

  --max-width: 1200px;
  --navbar-height: 72px;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 20px;

  /* Shadows — light mode */
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.06);
  --shadow-md: 0 4px 16px rgba(0,0,0,0.08);
  --shadow-lg: 0 8px 32px rgba(0,0,0,0.10);
  --shadow-hover: 0 12px 36px rgba(0,0,0,0.12);
  --shadow-glow: 0 0 24px rgba(0, 184, 148, 0.15);
  --shadow-cyan-glow: 0 0 20px rgba(9, 132, 227, 0.12);

  --transition-fast: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-smooth: 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-spring: 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* === Global glass card utility === */
.glass-card {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-md);
  box-shadow: var(--glass-shadow);
  transition: all var(--transition-smooth);
}

.glass-card:hover {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-accent);
  box-shadow: var(--glass-shadow), var(--shadow-glow);
  transform: translateY(-4px);
}

*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html { scroll-behavior: smooth; }

body {
  font-family: var(--font-body);
  background: linear-gradient(175deg, #faf9f6 0%, #f0ede8 35%, #faf9f6 70%, #e8f4f8 100%);
  background-attachment: fixed;
  min-height: 100vh;
  color: var(--color-text);
  line-height: 1.7;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
}

a { color: inherit; text-decoration: none; }

::selection {
  background: var(--color-accent);
  color: white;
}
</style>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ===== Subtle light orbs background ===== */
.orb-container {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
}

.orb-1 {
  width: 700px;
  height: 700px;
  top: -150px;
  right: -100px;
  background: radial-gradient(circle, rgba(0, 184, 148, 0.05) 0%, transparent 70%);
  animation: orbFloat1 30s ease-in-out infinite;
}

.orb-2 {
  width: 600px;
  height: 600px;
  bottom: 10%;
  left: -120px;
  background: radial-gradient(circle, rgba(9, 132, 227, 0.04) 0%, transparent 70%);
  animation: orbFloat2 35s ease-in-out infinite;
}

.orb-3 {
  width: 500px;
  height: 500px;
  top: 50%;
  left: 40%;
  background: radial-gradient(circle, rgba(255, 118, 117, 0.03) 0%, transparent 70%);
  animation: orbFloat3 28s ease-in-out infinite;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-60px, 40px) scale(1.06); }
  66% { transform: translate(40px, -30px) scale(0.94); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  40% { transform: translate(50px, -40px) scale(1.08); }
  80% { transform: translate(-30px, 30px) scale(0.95); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-40px, 50px) scale(1.05); }
}

/* ===== Navbar ===== */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--navbar-height);
  background: rgba(250, 249, 246, 0.85);
  border-bottom: 1px solid var(--color-border);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  transition: all var(--transition-smooth);
}

.navbar::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: var(--nav-progress, 0%);
  height: 2px;
  background: linear-gradient(90deg, var(--color-accent), var(--color-accent-cyan));
  transition: width 0.1s linear;
}

.navbar--scrolled {
  background: rgba(250, 249, 246, 0.95);
  border-bottom-color: var(--glass-border-accent);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}

.navbar-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 var(--space-xl);
}

/* ===== Brand ===== */
.navbar-brand {
  display: flex;
  align-items: baseline;
  gap: 0.6rem;
  text-decoration: none;
  transition: opacity var(--transition-fast);
}

.navbar-brand:hover { opacity: 0.8; }

.brand-icon {
  font-size: 1.1rem;
  color: var(--color-accent);
}

.brand-text {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #00b894, #0984e3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

.brand-sub {
  font-family: var(--font-chinese);
  font-size: 0.75rem;
  font-weight: 300;
  color: var(--color-text-muted);
  letter-spacing: 0.15em;
  margin-left: 0.2rem;
}

/* ===== Nav Links ===== */
.navbar-links {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.nav-link {
  position: relative;
  padding: 0.5rem 1rem;
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  font-weight: 400;
  color: var(--color-text-secondary);
  letter-spacing: 0.02em;
  transition: color var(--transition-fast);
  border-radius: var(--radius-sm);
}

.nav-link:hover {
  color: var(--color-text);
  background: var(--color-accent-light);
}

.nav-link.router-link-exact-active {
  color: var(--color-accent);
  font-weight: 500;
}

.nav-link--cta {
  background: linear-gradient(135deg, var(--color-accent-coral), #fdcb6e);
  color: white !important;
  font-weight: 500;
  padding: 0.5rem 1.3rem;
  border-radius: 100px;
  margin-left: 0.5rem;
  transition: all var(--transition-fast);
}

.nav-link--cta:hover {
  opacity: 0.9;
  color: white !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(255, 118, 117, 0.35);
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  font-family: var(--font-chinese);
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-text);
  letter-spacing: 0.02em;
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.nav-user:hover {
  color: var(--color-accent);
  background: var(--color-accent-light);
}

/* nav-user-icon removed - using UserAvatar component */

.nav-link--logout {
  background: none;
  border: 1.5px solid var(--color-border);
  cursor: pointer;
  border-radius: 100px;
  font-size: 0.85rem;
  padding: 0.4rem 1rem;
  color: var(--color-text-muted);
  font-family: var(--font-chinese);
  transition: all var(--transition-fast);
}

.nav-link--logout:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: var(--color-accent-light);
}

/* ===== Admin Dropdown ===== */
.nav-admin-dropdown {
  position: relative;
}

.nav-link--admin {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.5rem 1rem;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  font-weight: 400;
  color: var(--color-text-muted);
  background: none;
  border: none;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.nav-link--admin:hover {
  color: var(--color-accent);
  background: var(--color-accent-light);
}

.admin-icon {
  font-size: 0.8rem;
}

.admin-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  min-width: 140px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-lg);
  padding: 0.3rem 0;
  z-index: 110;
}

.admin-dropdown-item {
  display: block;
  padding: 0.55rem 1rem;
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}

.admin-dropdown-item:hover {
  color: var(--color-accent);
  background: var(--color-accent-light);
}

/* Dropdown transition */
.dropdown-fade-enter-active { transition: all 0.2s ease-out; }
.dropdown-fade-leave-active { transition: all 0.15s ease-in; }
.dropdown-fade-enter-from,
.dropdown-fade-leave-to { opacity: 0; transform: translateY(-4px); }

.mobile-search {
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border-light);
}

.mobile-user {
  color: var(--color-text);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
}

.mobile-logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
  width: 100%;
  font-size: 1rem;
  color: var(--color-text-muted);
  font-family: var(--font-chinese);
  padding: 0.8rem 0;
}

/* ===== Mobile Menu ===== */
.mobile-menu-btn {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
}

.hamburger {
  display: block;
  width: 22px;
  height: 2px;
  background: var(--color-text);
  position: relative;
  transition: all var(--transition-fast);
}

.hamburger::before,
.hamburger::after {
  content: '';
  position: absolute;
  width: 22px;
  height: 2px;
  background: var(--color-text);
  transition: all var(--transition-fast);
}

.hamburger::before { top: -7px; }
.hamburger::after { bottom: -7px; }

.hamburger.open { background: transparent; }
.hamburger.open::before { top: 0; transform: rotate(45deg); }
.hamburger.open::after { bottom: 0; transform: rotate(-45deg); }

.mobile-menu {
  display: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--color-border);
  padding: var(--space-md) var(--space-xl);
  box-shadow: var(--shadow-lg);
}

.mobile-link {
  display: block;
  padding: 0.8rem 0;
  font-family: var(--font-chinese);
  font-size: 1rem;
  color: var(--color-text-secondary);
  border-bottom: 1px solid var(--color-border-light);
  transition: color var(--transition-fast);
}

.mobile-link:last-child { border-bottom: none; }
.mobile-link:hover { color: var(--color-accent); }

/* ===== Main Content ===== */
.main-content {
  flex: 1;
  position: relative;
  z-index: 1;
  margin-top: var(--navbar-height);
  animation: pageEnter 0.6s ease-out;
}

@keyframes pageEnter {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== Footer ===== */
.app-footer {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-top: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  padding: var(--space-3xl) var(--space-xl) var(--space-xl);
  margin-top: var(--space-3xl);
}

.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
}

.footer-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-xl);
}

.footer-logo {
  font-family: var(--font-display);
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--color-dark);
  letter-spacing: -0.01em;
}

.footer-tagline {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  margin-top: 0.5rem;
  color: var(--color-text-muted);
  letter-spacing: 0.08em;
}

.footer-links {
  display: flex;
  gap: var(--space-lg);
}

.footer-links a {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: var(--color-text-muted);
  transition: color var(--transition-fast);
}

.footer-links a:hover { color: var(--color-accent); }

.footer-divider {
  height: 1px;
  background: var(--color-border);
  margin-bottom: var(--space-lg);
}

.footer-copy {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  letter-spacing: 0.02em;
}

/* ===== Transitions ===== */
.slide-down-enter-active { transition: all 0.3s ease-out; }
.slide-down-leave-active { transition: all 0.2s ease-in; }
.slide-down-enter-from,
.slide-down-leave-to { opacity: 0; transform: translateY(-8px); }

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .navbar-links { display: none; }
  .mobile-menu-btn { display: block; }
  .mobile-menu { display: block; }
  .navbar-inner { padding: 0 var(--space-lg); }
  .brand-sub { display: none; }
  .footer-top { flex-direction: column; gap: var(--space-lg); }
}

/* ===== Accessibility: reduced motion ===== */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
