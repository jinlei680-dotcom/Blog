<template>
  <div class="app-layout">
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

function handleScroll() {
  isScrolled.value = window.scrollY > 20
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
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;0,800;1,400;1,600&family=Source+Han+Sans+SC:wght@300;400;500;700&family=DM+Sans:ital,wght@0,400;0,500;0,700;1,400&display=swap');

:root {
  /* Warm modern travel palette */
  --color-bg: #faf8f5;
  --color-bg-warm: #f5f0ea;
  --color-surface: #ffffff;
  --color-text: #2c2c2c;
  --color-text-secondary: #6b6b6b;
  --color-text-muted: #999999;
  --color-accent: #c8956c;
  --color-accent-hover: #b07d56;
  --color-accent-light: rgba(200, 149, 108, 0.1);
  --color-border: #e8e4df;
  --color-border-light: #f0ece7;
  --color-dark: #1a1a1a;
  --color-dark-soft: #333333;

  --font-display: 'Playfair Display', 'Georgia', serif;
  --font-body: 'DM Sans', 'Source Han Sans SC', 'PingFang SC', sans-serif;
  --font-chinese: 'Source Han Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;

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

  --shadow-sm: 0 1px 3px rgba(0,0,0,0.04);
  --shadow-md: 0 4px 20px rgba(0,0,0,0.06);
  --shadow-lg: 0 8px 40px rgba(0,0,0,0.08);
  --shadow-hover: 0 12px 48px rgba(0,0,0,0.12);

  --transition-fast: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-smooth: 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-spring: 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html { scroll-behavior: smooth; }

body {
  font-family: var(--font-body);
  background-color: var(--color-bg);
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

/* ===== Navbar ===== */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--navbar-height);
  background: rgba(250, 248, 245, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: all var(--transition-smooth);
}

.navbar--scrolled {
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 1px 20px rgba(0,0,0,0.06);
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
  color: var(--color-dark);
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
  background: var(--color-dark);
  color: white !important;
  font-weight: 500;
  padding: 0.5rem 1.3rem;
  border-radius: 100px;
  margin-left: 0.5rem;
  transition: all var(--transition-fast);
}

.nav-link--cta:hover {
  background: var(--color-accent);
  color: white !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(200, 149, 108, 0.3);
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
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-md);
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
  background: var(--color-surface);
  border-top: 1px solid var(--color-border-light);
  padding: var(--space-md) var(--space-xl);
  box-shadow: var(--shadow-md);
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
  background: var(--color-dark);
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
</style>
