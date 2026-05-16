<template>
  <nav class="navbar" :class="{ scrolled: isScrolled }">
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
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">
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
          <router-link to="/notifications" class="notification-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="userStore.unreadNotifications > 0" class="notification-badge">
              {{ userStore.unreadNotifications > 99 ? '99+' : userStore.unreadNotifications }}
            </span>
          </router-link>

          <!-- 主题切换 -->
          <button class="theme-toggle" @click="themeStore.toggleTheme" :title="themeStore.isDarkMode ? '切换亮色模式' : '切换暗色模式'">
            <svg v-if="!themeStore.isDarkMode" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
            </svg>
          </button>

          <!-- 用户下拉菜单 -->
          <div class="user-dropdown" @click="toggleDropdown">
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
              <div v-if="isDropdownOpen" class="dropdown-menu">
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
      <button class="mobile-menu-btn hide-desktop" @click="toggleMobileMenu">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="12" x2="21" y2="12"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
      </button>
    </div>

    <!-- 移动端菜单 -->
    <transition name="mobile-menu">
      <div v-if="isMobileMenuOpen" class="mobile-menu" @click.self="closeMobileMenu">
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

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
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
let scrollTimer = null
const handleScroll = () => {
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
const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

const closeDropdown = () => {
  isDropdownOpen.value = false
}

// 移动端菜单
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
  document.body.style.overflow = isMobileMenuOpen.value ? 'hidden' : ''
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
  document.body.style.overflow = ''
}

// 退出登录
const handleLogout = async () => {
  await userStore.logout()
  closeDropdown()
  closeMobileMenu()
  router.push('/')
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event) => {
  if (!event.target.closest('.user-dropdown')) {
    isDropdownOpen.value = false
  }
}

// 点击外部关闭移动端菜单
const handleDocumentClick = (event) => {
  if (isMobileMenuOpen.value && !event.target.closest('.navbar') && !event.target.closest('.mobile-menu')) {
    closeMobileMenu()
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('click', handleDocumentClick)
})

onUnmounted(() => {
  if (scrollTimer) clearTimeout(scrollTimer)
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
  height: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border);
  z-index: 1000;
  transition: all var(--transition);
}

.dark-mode .navbar {
  background: rgba(30, 41, 59, 0.95);
}

.navbar.scrolled {
  box-shadow: var(--shadow);
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
}

.brand-icon {
  font-size: 1.5rem;
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
  background: var(--background);
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  color: var(--text-primary);
  transition: all var(--transition);
}

.navbar-search input:focus {
  outline: none;
  border-color: var(--primary);
  background: var(--surface);
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
}

.search-btn:hover {
  color: var(--primary);
}

.navbar-nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.nav-link {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius);
  transition: all var(--transition);
}

.nav-link:hover {
  color: var(--primary);
  background: var(--primary-light);
}

.nav-link.active {
  color: var(--primary);
  background: var(--primary-light);
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
}

.notification-icon:hover {
  color: var(--primary);
  background: var(--primary-light);
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
  background: var(--error);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle {
  width: 36px; height: 36px;
  border: none; background: transparent;
  border-radius: 50%; cursor: pointer;
  color: var(--text-secondary);
  display: flex; align-items: center; justify-content: center;
  transition: background 0.2s;
}
.theme-toggle:hover { background: var(--bg-secondary, #f0f0f0); }

.user-dropdown {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--border);
  transition: all var(--transition);
}

.user-avatar:hover {
  border-color: var(--primary);
}

.dropdown-arrow {
  margin-left: 4px;
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
  background: var(--surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
  overflow: hidden;
  z-index: 1001;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--background);
}

.dropdown-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
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
  background: var(--border);
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
  background: var(--background);
  color: var(--primary);
}

.dropdown-item.danger:hover {
  background: rgba(239, 68, 68, 0.1);
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
  background: var(--error);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-menu-btn {
  background: none;
  border: none;
  color: var(--text-primary);
  cursor: pointer;
  padding: 0.5rem;
}

.mobile-menu {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--surface);
  padding: var(--spacing-md);
  overflow-y: auto;
  z-index: 999;
}

.mobile-search {
  margin-bottom: var(--spacing-md);
}

.mobile-search input {
  width: 100%;
  padding: 0.75rem 1rem;
  background: var(--background);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  font-size: 1rem;
  color: var(--text-primary);
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  font-size: 1rem;
  color: var(--text-primary);
  text-decoration: none;
  border-bottom: 1px solid var(--border);
  background: none;
  width: 100%;
  border-left: none;
  border-right: none;
  border-top: none;
  cursor: pointer;
  text-align: left;
}

.mobile-nav-link.danger {
  color: var(--error);
}

.mobile-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
  background: var(--error);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 过渡动画 */
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
</style>
