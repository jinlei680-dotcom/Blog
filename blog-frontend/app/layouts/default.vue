<template>
  <div class="app-layout">
    <!-- Floating orbs background -->
    <div class="orb-container" aria-hidden="true">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>
    <div class="cursor-glow" ref="cursorGlowEl" aria-hidden="true"></div>

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

    <div class="music-player-container">
      <ClientOnly>
        <MusicPlayer />
      </ClientOnly>
    </div>

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
const cursorGlowEl = ref(null)

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
  window.addEventListener('mousemove', (e) => {
    if (!cursorGlowEl.value) return
    cursorGlowEl.value.style.left = (e.clientX - 200) + 'px'
    cursorGlowEl.value.style.top  = (e.clientY - 200) + 'px'
  }, { passive: true })
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
  /* === Glassmorphism color system === */
  --color-bg: #0f0c29;
  --color-bg-warm: #302b63;
  --color-surface: rgba(255, 255, 255, 0.06);

  /* Glass effects */
  --glass-bg: rgba(255, 255, 255, 0.06);
  --glass-bg-hover: rgba(255, 255, 255, 0.1);
  --glass-bg-strong: rgba(255, 255, 255, 0.12);
  --glass-border: rgba(255, 255, 255, 0.15);
  --glass-border-accent: rgba(139, 92, 246, 0.5);
  --glass-blur: blur(20px);
  --glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);

  /* Primary accent: purple + cyan */
  --color-accent: #8b5cf6;
  --color-accent-hover: #7c3aed;
  --color-accent-light: rgba(139, 92, 246, 0.15);
  --color-accent-cyan: #06b6d4;
  --color-accent-cyan-light: rgba(6, 182, 212, 0.15);

  /* Text (light on dark) */
  --color-text: rgba(255, 255, 255, 0.9);
  --color-text-secondary: rgba(255, 255, 255, 0.65);
  --color-text-muted: rgba(255, 255, 255, 0.4);
  --color-dark: #ffffff;
  --color-dark-soft: rgba(255, 255, 255, 0.85);

  /* Borders */
  --color-border: rgba(255, 255, 255, 0.12);
  --color-border-light: rgba(255, 255, 255, 0.08);

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

  /* Shadows */
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.2);
  --shadow-md: 0 4px 20px rgba(0,0,0,0.3);
  --shadow-lg: 0 8px 40px rgba(0,0,0,0.4);
  --shadow-hover: 0 12px 48px rgba(0,0,0,0.5);
  --shadow-glow: 0 0 40px rgba(139, 92, 246, 0.25);
  --shadow-cyan-glow: 0 0 30px rgba(6, 182, 212, 0.2);

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
  background:
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='200' height='200' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E"),
    linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
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

/* ===== Orb background ===== */
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
  filter: blur(80px);
}

.orb-1 {
  width: 600px;
  height: 600px;
  top: -100px;
  left: -100px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.35) 0%, transparent 70%);
  animation: orbFloat1 25s ease-in-out infinite;
}

.orb-2 {
  width: 500px;
  height: 500px;
  top: 30%;
  right: -80px;
  background: radial-gradient(circle, rgba(6, 182, 212, 0.3) 0%, transparent 70%);
  animation: orbFloat2 30s ease-in-out infinite reverse;
}

.orb-3 {
  width: 400px;
  height: 400px;
  bottom: 10%;
  left: 30%;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.25) 0%, transparent 70%);
  animation: orbFloat3 20s ease-in-out infinite;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(80px, -50px) scale(1.1); }
  50% { transform: translate(-40px, 60px) scale(0.95); }
  75% { transform: translate(60px, 40px) scale(1.05); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-60px, 40px) scale(1.08); }
  66% { transform: translate(40px, -30px) scale(0.92); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  40% { transform: translate(-50px, -40px) scale(1.12); }
  80% { transform: translate(60px, 30px) scale(0.9); }
}

/* ===== Cursor glow ===== */
.cursor-glow {
  position: fixed;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(139,92,246,0.07) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
  transition: left 0.08s linear, top 0.08s linear;
  will-change: left, top;
}

@media (pointer: coarse) { .cursor-glow { display: none; } }

/* ===== Navbar ===== */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--navbar-height);
  background: rgba(15, 12, 41, 0.5);
  border-bottom: 1px solid var(--glass-border);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
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
  background: rgba(15, 12, 41, 0.85);
  border-bottom-color: var(--glass-border-accent);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
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
  background: linear-gradient(135deg, #a78bfa, #06b6d4);
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
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-cyan));
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
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.4);
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
  background: rgba(15, 12, 41, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-sm);
  box-shadow: var(--glass-shadow);
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
  background: rgba(15, 12, 41, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--glass-border);
  padding: var(--space-md) var(--space-xl);
  box-shadow: var(--glass-shadow);
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

/* ===== Music Player ===== */
.music-player-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 50;
}

/* ===== Footer ===== */
.app-footer {
  position: relative;
  z-index: 1;
  background: rgba(10, 8, 25, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--glass-border);
  color: rgba(255,255,255,0.7);
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
  color: white;
  letter-spacing: -0.01em;
}

.footer-tagline {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  margin-top: 0.5rem;
  color: rgba(255,255,255,0.4);
  letter-spacing: 0.08em;
}

.footer-links {
  display: flex;
  gap: var(--space-lg);
}

.footer-links a {
  font-family: var(--font-chinese);
  font-size: 0.85rem;
  color: rgba(255,255,255,0.5);
  transition: color var(--transition-fast);
}

.footer-links a:hover { color: var(--color-accent); }

.footer-divider {
  height: 1px;
  background: rgba(255,255,255,0.08);
  margin-bottom: var(--space-lg);
}

.footer-copy {
  font-size: 0.8rem;
  color: rgba(255,255,255,0.3);
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
  .cursor-glow { display: none; }
}
</style>
