<template>
  <nav class="navbar" :class="{ scrolled: isScrolled }" aria-label="主导航">
    <div class="navbar-container">
      <!-- Logo -->
      <router-link to="/" class="navbar-brand">
        <span class="brand-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            <path d="M10 2v8l3-2 3 2V2"/>
          </svg>
        </span>
        <span class="brand-text">校园博客</span>
      </router-link>

      <!-- 搜索框 -->
      <div class="navbar-search hide-mobile">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索文章..."
          aria-label="搜索文章"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" aria-label="搜索" @click="handleSearch">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
        </button>
      </div>

      <!-- 导航链接 -->
      <div class="navbar-nav">
        <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">
          首页
        </router-link>
        <router-link to="/circle" class="nav-link" :class="{ active: $route.path.startsWith('/circle') }">
          校友圈
        </router-link>
        <router-link to="/trending" class="nav-link" :class="{ active: $route.path === '/trending' }">
          热搜
        </router-link>
      </div>

      <!-- 用户区域 -->
      <div class="navbar-user">
        <template v-if="userStore.isLoggedIn">
          <!-- 发布按钮 -->
          <router-link to="/post-edit" class="btn btn-primary btn-sm hide-mobile">
            写文章
          </router-link>
          
          <!-- 通知图标 -->
          <router-link to="/notifications" class="notification-icon" aria-label="消息通知">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="userStore.unreadNotifications > 0" class="notification-badge">
              {{ userStore.unreadNotifications > 99 ? '99+' : userStore.unreadNotifications }}
            </span>
          </router-link>

          <!-- 主题切换 -->
          <button class="theme-toggle" @click="themeStore.toggleTheme" :title="themeStore.isDarkMode ? '切换亮色模式' : '切换暗色模式'" :aria-label="themeStore.isDarkMode ? '切换亮色模式' : '切换暗色模式'">
            <svg v-if="!themeStore.isDarkMode" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
            </svg>
          </button>

          <!-- 用户下拉菜单 -->
          <div class="user-dropdown" @mouseenter="openDropdown" @mouseleave="scheduleClose">
            <img 
              :src="userStore.avatar || '/default-avatar.png'" 
              :alt="userStore.nickname"
              class="user-avatar"
            />
            <svg class="dropdown-arrow" :class="{ open: isDropdownOpen }" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
            
            <!-- 下拉菜单 -->
            <transition name="dropdown">
              <div v-if="isDropdownOpen" class="dropdown-menu" @mouseenter="openDropdown" @mouseleave="scheduleClose">
                <div class="dropdown-header">
                  <img 
                    :src="userStore.avatar || '/default-avatar.png'" 
                    :alt="userStore.nickname"
                    class="dropdown-avatar"
                  />
                  <div class="dropdown-user-info">
                    <div class="dropdown-username">{{ userStore.nickname }}</div>
                    <div class="dropdown-email">{{ userStore.user?.email }}</div>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <router-link to="/profile" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  我的主页
                </router-link>
                <router-link to="/drafts" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                  我的草稿
                </router-link>
                <router-link to="/collections" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
                  </svg>
                  我的收藏
                </router-link>
                <router-link to="/my-reports" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><circle cx="12" cy="16" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="8" r="1"/>
                  </svg>
                  我的举报
                </router-link>
                <router-link to="/messages" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  私信
                  <span v-if="userStore.unreadMessages > 0" class="dropdown-badge">
                    {{ userStore.unreadMessages > 99 ? '99+' : userStore.unreadMessages }}
                  </span>
                </router-link>
                <div class="dropdown-divider"></div>
                <router-link v-if="userStore.isAdmin" to="/admin" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="7" height="7"/>
                    <rect x="14" y="3" width="7" height="7"/>
                    <rect x="14" y="14" width="7" height="7"/>
                    <rect x="3" y="14" width="7" height="7"/>
                  </svg>
                  管理后台
                </router-link>
                <button class="dropdown-item danger" @click="handleLogout">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  退出登录
                </button>
              </div>
            </transition>
          </div>
        </template>

        <template v-else>
          <router-link to="/login" class="btn btn-ghost btn-sm">登录</router-link>
          <router-link to="/register" class="btn btn-primary btn-sm">注册</router-link>
        </template>
      </div>

      <!-- 移动端菜单按钮 -->
      <button class="mobile-menu-btn hide-desktop" @click="toggleMobileMenu" aria-label="打开菜单" :aria-expanded="isMobileMenuOpen">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="12" x2="21" y2="12"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
      </button>
    </div>

    <!-- 移动端菜单 -->
    <transition name="mobile-menu">
      <div v-if="isMobileMenuOpen" class="mobile-menu" @click.self="closeMobileMenu" role="dialog" aria-label="移动端导航菜单" aria-modal="true">
        <div class="mobile-search">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索文章..."
            @keyup.enter="handleSearch"
          />
        </div>
        <router-link to="/" class="mobile-nav-link" @click="closeMobileMenu">首页</router-link>
        <router-link to="/circle" class="mobile-nav-link" @click="closeMobileMenu">校友圈</router-link>
        <router-link to="/trending" class="mobile-nav-link" @click="closeMobileMenu">热搜</router-link>
        <template v-if="userStore.isLoggedIn">
          <router-link to="/post-edit" class="mobile-nav-link" @click="closeMobileMenu">写文章</router-link>
          <router-link to="/notifications" class="mobile-nav-link" @click="closeMobileMenu">
            消息通知
            <span v-if="userStore.unreadNotifications > 0" class="mobile-badge">
              {{ userStore.unreadNotifications > 99 ? '99+' : userStore.unreadNotifications }}
            </span>
          </router-link>
          <router-link to="/messages" class="mobile-nav-link" @click="closeMobileMenu">
            私信
            <span v-if="userStore.unreadMessages > 0" class="mobile-badge">
              {{ userStore.unreadMessages > 99 ? '99+' : userStore.unreadMessages }}
            </span>
          </router-link>
          <button class="mobile-nav-link danger" @click="handleLogout">退出登录</button>
        </template>
        <template v-else>
          <router-link to="/login" class="mobile-nav-link" @click="closeMobileMenu">登录</router-link>
          <router-link to="/register" class="mobile-nav-link" @click="closeMobileMenu">注册</router-link>
        </template>
      </div>
    </transition>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useThemeStore } from '../../stores/theme'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const searchQuery = ref('')
const isScrolled = ref(false)
const isDropdownOpen = ref(false)
const isMobileMenuOpen = ref(false)

// 滚动处理
let scrollTimer: ReturnType<typeof setTimeout> | null = null
const handleScroll = (): void => {
  if (scrollTimer) return
  scrollTimer = setTimeout(() => {
    isScrolled.value = window.scrollY > 10
    scrollTimer = null
  }, 100)
}

// 搜索处理
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { q: searchQuery.value.trim() } })
    searchQuery.value = ''
    closeMobileMenu()
  }
}

// 下拉菜单
let dropdownCloseTimer: ReturnType<typeof setTimeout> | null = null

const openDropdown = (): void => {
  if (dropdownCloseTimer) {
    clearTimeout(dropdownCloseTimer)
    dropdownCloseTimer = null
  }
  isDropdownOpen.value = true
}

const scheduleClose = (): void => {
  dropdownCloseTimer = setTimeout(() => {
    isDropdownOpen.value = false
  }, 300)
}

const closeDropdown = () => {
  isDropdownOpen.value = false
}

// 移动端菜单
const savedBodyOverflow = ref('')

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
  if (isMobileMenuOpen.value) {
    savedBodyOverflow.value = document.body.style.overflow
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = savedBodyOverflow.value
    savedBodyOverflow.value = ''
  }
}

const closeMobileMenu = () => {
  if (!isMobileMenuOpen.value) return
  isMobileMenuOpen.value = false
  document.body.style.overflow = savedBodyOverflow.value
  savedBodyOverflow.value = ''
}

// 退出登录
const handleLogout = async () => {
  await userStore.logout()
  closeDropdown()
  closeMobileMenu()
  router.push('/')
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event: Event): void => {
  const target = event.target as HTMLElement
  if (!target.closest('.user-dropdown')) {
    isDropdownOpen.value = false
  }
}

// 点击外部关闭移动端菜单
const handleDocumentClick = (event: Event): void => {
  const target = event.target as HTMLElement
  if (
    isMobileMenuOpen.value &&
    !target.closest('.navbar') &&
    !target.closest('.mobile-menu')
  ) {
    closeMobileMenu()
  }
}

let notificationTimer: ReturnType<typeof setInterval> | null = null

const startNotificationPolling = (): void => {
  if (notificationTimer) return
  notificationTimer = setInterval(fetchNotifications, 30000)
}

const stopNotificationPolling = (): void => {
  if (notificationTimer) {
    clearInterval(notificationTimer)
    notificationTimer = null
  }
}

const fetchNotifications = async () => {
  if (!userStore.isLoggedIn) {
    stopNotificationPolling()
    return
  }
  try {
    await userStore.fetchUnreadCounts()
  } catch { /* ignore */ }
}

watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    fetchNotifications()
    startNotificationPolling()
  } else {
    stopNotificationPolling()
  }
})

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('click', handleDocumentClick)
  if (userStore.isLoggedIn) {
    fetchNotifications()
    startNotificationPolling()
  }
})

onUnmounted(() => {
  if (scrollTimer) clearTimeout(scrollTimer)
  if (dropdownCloseTimer) clearTimeout(dropdownCloseTimer)
  if (notificationTimer) clearInterval(notificationTimer)
  if (isMobileMenuOpen.value) document.body.style.overflow = ''
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--navbar-height);
  background: var(--navbar-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-bottom: 1px solid var(--glass-border);
  z-index: var(--z-sticky);
  transition: all var(--transition-slow);
}

.navbar.scrolled {
  box-shadow: var(--shadow-md);
  background: var(--glass-hover);
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  transition: all var(--transition);
}

.navbar-brand:hover {
  transform: scale(1.02);
}

.brand-icon {
  display: flex;
  align-items: center;
  transition: transform var(--transition-spring);
}

.navbar-brand:hover .brand-icon {
  transform: rotate(-5deg) scale(1.1);
}

.navbar-search {
  flex: 1;
  max-width: 400px;
  position: relative;
}

.navbar-search input {
  width: 100%;
  padding: 0.5rem 1rem;
  padding-right: 2.5rem;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  color: var(--text-primary);
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
}

.navbar-search input:hover {
  border-color: var(--text-muted);
}

.navbar-search input:focus {
  outline: none;
  border-color: var(--primary);
  background: var(--glass-hover);
  box-shadow: 0 0 0 3px var(--primary-light), var(--glass-shadow);
}

.navbar-search input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--radius);
  transition: all var(--transition);
}

.search-btn:hover {
  color: var(--primary);
  background: var(--primary-light);
}

.navbar-nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.nav-link {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius);
  transition: all var(--transition);
  position: relative;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  width: 60%;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-start), var(--primary-end));
  border-radius: var(--radius-full);
  transition: transform var(--transition);
}

.nav-link:hover::after,
.nav-link.active::after {
  transform: translateX(-50%) scaleX(1);
}

.nav-link:hover {
  color: var(--primary);
  background: var(--primary-light);
}

.nav-link.active {
  color: var(--primary);
  background: var(--primary-light);
  font-weight: 600;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.notification-icon {
  position: relative;
  padding: 0.5rem;
  color: var(--text-secondary);
  border-radius: var(--radius);
  transition: all var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-icon:hover {
  color: var(--primary);
  background: var(--primary-light);
  transform: scale(1.05);
}

.notification-badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  font-size: 0.625rem;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, var(--error), var(--error));
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.theme-toggle {
  width: 36px;
  height: 36px;
  border: 1px solid transparent;
  background: transparent;
  border-radius: var(--radius);
  cursor: pointer;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.theme-toggle:hover {
  background: var(--primary-light);
  color: var(--primary);
  border-color: var(--primary-light);
  transform: rotate(15deg);
}

.user-dropdown {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 8px;
  border-radius: var(--radius-lg);
  transition: all var(--transition);
}

.user-dropdown:hover {
  background: var(--primary-light);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--glass-border);
  transition: all var(--transition);
}

.user-dropdown:hover .user-avatar {
  border-color: var(--primary);
  box-shadow: var(--shadow-glow-primary);
}

.dropdown-arrow {
  transition: transform var(--transition);
}

.dropdown-arrow.open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  z-index: calc(var(--z-sticky) + 1);
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--primary-light);
}

.dropdown-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--glass-border);
}

.dropdown-user-info {
  flex: 1;
  min-width: 0;
}

.dropdown-username {
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-email {
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-divider {
  height: 1px;
  background: var(--glass-border);
  margin: var(--spacing-xs) 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 0.875rem;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition);
  width: 100%;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
}

.dropdown-item:hover {
  background: var(--primary-light);
  color: var(--primary);
  transform: translateX(4px);
}

.dropdown-item.danger:hover {
  background: var(--error-light);
  color: var(--error);
}

.dropdown-badge {
  margin-left: auto;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  font-size: 0.625rem;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, var(--error), var(--error));
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-menu-btn {
  background: none;
  border: 1px solid transparent;
  color: var(--text-primary);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: var(--radius);
  transition: all var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-menu-btn:hover {
  background: var(--primary-light);
  color: var(--primary);
  border-color: var(--primary-light);
}

.mobile-menu {
  position: fixed;
  top: var(--navbar-height);
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  padding: var(--spacing-md);
  overflow-y: auto;
  z-index: 999;
  border-top: 1px solid var(--glass-border);
}

.mobile-search {
  margin-bottom: var(--spacing-md);
}

.mobile-search input {
  width: 100%;
  padding: 0.75rem 1rem;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  font-size: 1rem;
  color: var(--text-primary);
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
}

.mobile-search input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light), var(--glass-shadow);
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  font-size: 1rem;
  color: var(--text-primary);
  text-decoration: none;
  border-bottom: 1px solid var(--glass-border);
  background: none;
  width: 100%;
  border-left: none;
  border-right: none;
  border-top: none;
  cursor: pointer;
  text-align: left;
  transition: all var(--transition);
  border-radius: var(--radius);
}

.mobile-nav-link:hover {
  background: var(--primary-light);
  color: var(--primary);
  transform: translateX(4px);
}

.mobile-nav-link.danger {
  color: var(--error);
}

.mobile-nav-link.danger:hover {
  background: var(--error-light);
}

.mobile-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, var(--error), var(--error));
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: all 0.3s ease;
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 0 var(--spacing-md);
  }
  
  .navbar-nav {
    display: none;
  }
}

@media (max-width: 480px) {
  .navbar-brand .brand-text {
    font-size: 1rem;
  }
}
</style>
