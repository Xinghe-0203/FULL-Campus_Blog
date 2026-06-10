<template>
  <nav class="navbar" :class="{ scrolled: isScrolled, 'search-expanded': isSearchExpanded }" aria-label="主导航">
    <div class="navbar-container">
      <!-- Logo -->
      <router-link to="/" class="navbar-brand">
        <span class="brand-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="var(--purple)" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            <path d="M10 2v8l3-2 3 2V2"/>
          </svg>
        </span>
        <span class="brand-text">校园博客</span>
      </router-link>

      <!-- 搜索框 - 桌面端 -->
      <div class="navbar-search hide-mobile" :class="{ expanded: isSearchExpanded }">
        <div class="search-inner">
          <svg class="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            ref="searchInputRef"
            v-model="searchQuery"
            type="text"
            placeholder="搜索文章..."
            aria-label="搜索文章"
            @keyup.enter="handleSearch"
            @focus="isSearchExpanded = true"
            @blur="handleSearchBlur"
          />
          <Transition name="search-clear">
            <button
              v-if="searchQuery.length > 0"
              class="search-clear-btn"
              aria-label="清除搜索"
              @mousedown.prevent="clearSearch"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </Transition>
          <button class="search-btn" aria-label="搜索" @click="handleSearch">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 导航链接 -->
      <div class="navbar-nav">
        <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          首页
        </router-link>
        <router-link to="/circle" class="nav-link" :class="{ active: $route.path.startsWith('/circle') }">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          校友圈
        </router-link>
        <router-link to="/trending" class="nav-link" :class="{ active: $route.path === '/trending' }">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/>
            <polyline points="16 7 22 7 22 13"/>
          </svg>
          热搜
        </router-link>
      </div>

      <!-- 用户区域 -->
      <div class="navbar-user">
        <template v-if="userStore.isLoggedIn">
          <!-- 发布按钮 -->
          <router-link to="/post-edit" class="btn btn-primary btn-sm write-btn hide-mobile">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            写文章
          </router-link>

          <!-- 通知图标 -->
          <router-link to="/notifications" class="icon-btn notification-icon" aria-label="消息通知">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="userStore.unreadNotifications > 0" class="notification-badge">
              {{ userStore.unreadNotifications > 99 ? '99+' : userStore.unreadNotifications }}
            </span>
          </router-link>

          <!-- 主题切换 -->
          <button
            class="icon-btn theme-toggle"
            @click="themeStore.toggleTheme"
            :title="themeStore.isDarkMode ? '切换亮色模式' : '切换暗色模式'"
            :aria-label="themeStore.isDarkMode ? '切换亮色模式' : '切换暗色模式'"
          >
            <Transition name="theme-icon" mode="out-in">
              <svg v-if="!themeStore.isDarkMode" key="moon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
              </svg>
              <svg v-else key="sun" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="5"/>
                <line x1="12" y1="1" x2="12" y2="3"/>
                <line x1="12" y1="21" x2="12" y2="23"/>
                <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
                <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
                <line x1="1" y1="12" x2="3" y2="12"/>
                <line x1="21" y1="12" x2="23" y2="12"/>
                <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
                <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
              </svg>
            </Transition>
          </button>

          <!-- 用户下拉菜单 -->
          <div class="user-dropdown" @mouseenter="openDropdown" @mouseleave="scheduleClose">
            <div class="avatar-wrapper">
              <img
                :src="userStore.avatar || '/default-avatar.png'"
                :alt="userStore.nickname"
                class="user-avatar"
              />
              <span class="avatar-status"></span>
            </div>
            <svg class="dropdown-arrow" :class="{ open: isDropdownOpen }" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>

            <!-- 下拉菜单 -->
            <Transition name="dropdown">
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
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  我的主页
                </router-link>
                <router-link to="/drafts" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                  我的草稿
                </router-link>
                <router-link to="/collections" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
                  </svg>
                  我的收藏
                </router-link>
                <router-link to="/my-reports" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    <circle cx="12" cy="16" r="1"/>
                    <circle cx="12" cy="12" r="1"/>
                    <circle cx="12" cy="8" r="1"/>
                  </svg>
                  我的举报
                </router-link>
                <router-link to="/messages" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  私信
                  <span v-if="userStore.unreadMessages > 0" class="dropdown-badge">
                    {{ userStore.unreadMessages > 99 ? '99+' : userStore.unreadMessages }}
                  </span>
                </router-link>
                <div class="dropdown-divider"></div>
                <router-link v-if="userStore.isAdmin" to="/admin" class="dropdown-item" @click="closeDropdown">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="7" height="7"/>
                    <rect x="14" y="3" width="7" height="7"/>
                    <rect x="14" y="14" width="7" height="7"/>
                    <rect x="3" y="14" width="7" height="7"/>
                  </svg>
                  管理后台
                </router-link>
                <button class="dropdown-item danger" @click="handleLogout">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  退出登录
                </button>
              </div>
            </Transition>
          </div>
        </template>

        <template v-else>
          <router-link to="/login" class="btn btn-ghost btn-sm">登录</router-link>
          <router-link to="/register" class="btn btn-primary btn-sm">注册</router-link>
        </template>
      </div>

      <!-- 移动端菜单按钮 -->
      <button
        class="mobile-menu-btn hide-desktop"
        :class="{ active: isMobileMenuOpen }"
        @click="toggleMobileMenu"
        aria-label="打开菜单"
        :aria-expanded="isMobileMenuOpen"
      >
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
      </button>
    </div>

    <!-- 移动端全屏菜单 -->
    <Transition name="mobile-overlay">
      <div v-if="isMobileMenuOpen" class="mobile-menu-overlay" @click="closeMobileMenu">
        <Transition name="mobile-menu-panel">
          <div v-if="isMobileMenuOpen" class="mobile-menu" @click.stop role="dialog" aria-label="移动端导航菜单" aria-modal="true">
            <!-- 移动端搜索 -->
            <div class="mobile-search">
              <div class="search-inner">
                <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"/>
                  <path d="m21 21-4.35-4.35"/>
                </svg>
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="搜索文章..."
                  @keyup.enter="handleSearch"
                />
              </div>
            </div>

            <!-- 主导航链接 -- staggered reveal -->
            <div class="mobile-nav-section">
              <router-link to="/" class="mobile-nav-link" :style="{ '--stagger': 0 }" @click="closeMobileMenu">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                  <polyline points="9 22 9 12 15 12 15 22"/>
                </svg>
                首页
              </router-link>
              <router-link to="/circle" class="mobile-nav-link" :style="{ '--stagger': 1 }" @click="closeMobileMenu">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
                校友圈
              </router-link>
              <router-link to="/trending" class="mobile-nav-link" :style="{ '--stagger': 2 }" @click="closeMobileMenu">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/>
                  <polyline points="16 7 22 7 22 13"/>
                </svg>
                热搜
              </router-link>
            </div>

            <div class="mobile-divider"></div>

            <!-- 用户链接 -- staggered reveal -->
            <div class="mobile-nav-section">
              <template v-if="userStore.isLoggedIn">
                <router-link to="/post-edit" class="mobile-nav-link" :style="{ '--stagger': 3 }" @click="closeMobileMenu">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="5" x2="12" y2="19"/>
                    <line x1="5" y1="12" x2="19" y2="12"/>
                  </svg>
                  写文章
                </router-link>
                <router-link to="/notifications" class="mobile-nav-link" :style="{ '--stagger': 4 }" @click="closeMobileMenu">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                  </svg>
                  消息通知
                  <span v-if="userStore.unreadNotifications > 0" class="mobile-badge">
                    {{ userStore.unreadNotifications > 99 ? '99+' : userStore.unreadNotifications }}
                  </span>
                </router-link>
                <router-link to="/messages" class="mobile-nav-link" :style="{ '--stagger': 5 }" @click="closeMobileMenu">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  私信
                  <span v-if="userStore.unreadMessages > 0" class="mobile-badge">
                    {{ userStore.unreadMessages > 99 ? '99+' : userStore.unreadMessages }}
                  </span>
                </router-link>
                <div class="mobile-divider" :style="{ '--stagger': 6 }"></div>
                <button class="mobile-nav-link danger" :style="{ '--stagger': 7 }" @click="handleLogout">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  退出登录
                </button>
              </template>
              <template v-else>
                <router-link to="/login" class="mobile-nav-link" :style="{ '--stagger': 3 }" @click="closeMobileMenu">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                    <polyline points="10 17 15 12 10 7"/>
                    <line x1="15" y1="12" x2="3" y2="12"/>
                  </svg>
                  登录
                </router-link>
                <router-link to="/register" class="mobile-nav-link" :style="{ '--stagger': 4 }" @click="closeMobileMenu">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="8.5" cy="7" r="4"/>
                    <line x1="20" y1="8" x2="20" y2="14"/>
                    <line x1="23" y1="11" x2="17" y2="11"/>
                  </svg>
                  注册
                </router-link>
              </template>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
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
const isSearchExpanded = ref(false)
const searchInputRef = ref<HTMLInputElement | null>(null)

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
    isSearchExpanded.value = false
    searchInputRef.value?.blur()
    closeMobileMenu()
  }
}

const clearSearch = () => {
  searchQuery.value = ''
  searchInputRef.value?.focus()
}

const handleSearchBlur = () => {
  if (!searchQuery.value.trim()) {
    isSearchExpanded.value = false
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
/* =============================================================================
   Fluid Island Nav -- Floating Pill Navigation
   Warm Canvas v5.0 compatible: flat surfaces, ultra-soft elevation
   ============================================================================= */

.navbar {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 32px);
  max-width: 1200px;
  height: 52px;
  background: var(--surface-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-full);
  z-index: var(--z-sticky);
  transition:
    box-shadow 0.3s cubic-bezier(0.16, 1, 0.3, 1),
    border-color 0.3s ease,
    transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.navbar.scrolled {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border-color: var(--border-light);
}

.navbar-container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

/* ==================== Brand ==================== */
.navbar-brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.2s ease;
  flex-shrink: 0;
  letter-spacing: -0.02em;
  font-family: var(--font-display);
}

.navbar-brand:hover {
  color: var(--accent);
}

.brand-icon {
  display: flex;
  align-items: center;
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.navbar-brand:hover .brand-icon {
  transform: rotate(-6deg) scale(1.06);
}

.brand-text {
  background: linear-gradient(135deg, var(--purple), var(--purple-muted));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ==================== Search ==================== */
.navbar-search {
  flex: 1;
  max-width: 240px;
  position: relative;
  transition: max-width 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.navbar-search.expanded {
  max-width: 360px;
}

.search-inner {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 10px;
  color: var(--text-muted);
  pointer-events: none;
  transition: color 0.2s ease;
  z-index: 1;
}

.navbar-search.expanded .search-icon {
  color: var(--purple);
}

.navbar-search input {
  width: 100%;
  padding: 6px 2.25rem 6px 2rem;
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  color: var(--text-primary);
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: none;
  font-family: var(--font-sans);
}

.navbar-search input:hover {
  border-color: var(--border-light);
}

.navbar-search input:focus {
  outline: none;
  border-color: var(--purple);
  background: var(--surface-primary);
  box-shadow: 0 0 0 3px var(--purple-light);
}

.navbar-search input::placeholder {
  color: var(--text-muted);
}

.search-clear-btn {
  position: absolute;
  right: 2rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 2px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.15s ease;
}

.search-clear-btn:hover {
  color: var(--error);
}

.search-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-full);
  transition: color 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  color: var(--purple);
}

/* search-clear transition */
.search-clear-enter-active,
.search-clear-leave-active {
  transition: all 0.15s ease;
}

.search-clear-enter-from,
.search-clear-leave-to {
  opacity: 0;
  transform: translateY(-50%) scale(0.8);
}

/* ==================== Nav Links ==================== */
.navbar-nav {
  display: flex;
  align-items: center;
  gap: 2px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  white-space: nowrap;
  font-family: var(--font-sans);
}

.nav-link svg {
  opacity: 0.45;
  transition: opacity 0.2s ease;
}

.nav-link:hover svg,
.nav-link.active svg {
  opacity: 1;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 1px;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  width: 36%;
  height: 2px;
  background: var(--accent);
  border-radius: var(--radius-full);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.nav-link:hover::after,
.nav-link.active::after {
  transform: translateX(-50%) scaleX(1);
}

.nav-link:hover {
  color: var(--accent);
  background: var(--accent-light);
}

.nav-link.active {
  color: var(--accent);
  background: var(--accent-light);
  font-weight: 600;
}

/* ==================== Icon Buttons ==================== */
.icon-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  text-decoration: none;
}

.icon-btn:hover {
  color: var(--accent);
  background: var(--accent-light);
}

/* ==================== Notification ==================== */
.notification-icon {
  position: relative;
}

.notification-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 14px;
  height: 14px;
  padding: 0 3px;
  font-size: 0.5rem;
  font-weight: 700;
  color: var(--text-inverse);
  background: var(--error);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--surface-primary);
  line-height: 1;
  animation: badge-breathe 3s ease-in-out infinite;
}

@keyframes badge-breathe {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* ==================== Theme Toggle ==================== */
.theme-toggle {
  border: 1px solid transparent;
}

.theme-toggle:hover {
  color: var(--accent);
  background: var(--accent-light);
  border-color: transparent;
  transform: rotate(15deg);
}

/* theme icon transition */
.theme-icon-enter-active,
.theme-icon-leave-active {
  transition: all 0.2s ease;
}

.theme-icon-enter-from {
  opacity: 0;
  transform: rotate(-90deg) scale(0.5);
}

.theme-icon-leave-to {
  opacity: 0;
  transform: rotate(90deg) scale(0.5);
}

/* ==================== User Dropdown ==================== */
.navbar-user {
  display: flex;
  align-items: center;
  gap: var(--spacing-1);
  margin-left: auto;
}

.write-btn {
  gap: 4px;
  font-weight: 600;
  letter-spacing: 0.01em;
  border-radius: var(--radius-full);
  padding: 6px 16px;
}

.write-btn svg {
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.write-btn:hover svg {
  transform: rotate(90deg);
}

.user-dropdown {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 4px 8px;
  border-radius: var(--radius-full);
  transition: background 0.2s ease;
}

.user-dropdown:hover {
  background: var(--accent-light);
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--border-light);
  transition: border-color 0.2s ease;
}

.user-dropdown:hover .user-avatar {
  border-color: var(--accent);
}

.avatar-status {
  position: absolute;
  bottom: -1px;
  right: -1px;
  width: 8px;
  height: 8px;
  background: var(--success);
  border: 2px solid var(--surface-primary);
  border-radius: var(--radius-full);
}

.dropdown-arrow {
  transition: transform 0.2s ease;
  color: var(--text-muted);
}

.dropdown-arrow.open {
  transform: rotate(180deg);
}

/* ==================== Dropdown Menu ==================== */
.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 260px;
  background: var(--surface-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  z-index: calc(var(--z-sticky) + 2);
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--accent-light);
}

.dropdown-avatar {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--border-light);
}

.dropdown-user-info {
  flex: 1;
  min-width: 0;
}

.dropdown-username {
  font-weight: 600;
  font-size: var(--text-sm);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-email {
  font-size: var(--text-xs);
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-light);
  margin: 4px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 0.5rem var(--spacing-lg);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.15s ease;
  width: 100%;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  font-family: var(--font-sans);
}

.dropdown-item svg {
  flex-shrink: 0;
  opacity: 0.5;
  transition: opacity 0.15s ease;
}

.dropdown-item:hover {
  background: var(--accent-light);
  color: var(--accent);
}

.dropdown-item:hover svg {
  opacity: 1;
}

.dropdown-item.danger:hover {
  background: var(--error-light);
  color: var(--error);
}

.dropdown-badge {
  margin-left: auto;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  font-size: 0.625rem;
  font-weight: 700;
  color: var(--text-inverse);
  background: var(--error);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* dropdown transition */
.dropdown-enter-active {
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dropdown-leave-active {
  transition: all 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}

/* ==================== Mobile Menu Button -- Hamburger Morph ==================== */
.mobile-menu-btn {
  background: none;
  border: none;
  color: var(--text-primary);
  cursor: pointer;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  transition: background 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 0;
}

.mobile-menu-btn:hover {
  background: var(--accent-light);
  color: var(--accent);
}

.hamburger-line {
  display: block;
  width: 18px;
  height: 2px;
  background: currentColor;
  border-radius: 2px;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: center;
}

/* Morph to X */
.mobile-menu-btn.active .hamburger-line:nth-child(1) {
  transform: translateY(7px) rotate(45deg);
}

.mobile-menu-btn.active .hamburger-line:nth-child(2) {
  opacity: 0;
  transform: scaleX(0);
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.mobile-menu-btn.active .hamburger-line:nth-child(3) {
  transform: translateY(-7px) rotate(-45deg);
}

/* ==================== Mobile Full-Screen Menu ==================== */
.mobile-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  height: 100dvh;
  background: rgba(0, 0, 0, 0.20);
  z-index: calc(var(--z-sticky) + 1);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 80px;
}

.mobile-menu {
  width: calc(100% - 32px);
  max-width: 400px;
  max-height: calc(100vh - 96px);
  max-height: calc(100dvh - 96px);
  background: var(--surface-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-2xl);
  padding: var(--spacing-5);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.10);
}

.mobile-search {
  margin-bottom: var(--spacing-4);
}

.mobile-search .search-inner {
  position: relative;
  display: flex;
  align-items: center;
}

.mobile-search .search-icon {
  position: absolute;
  left: 12px;
  color: var(--text-muted);
  pointer-events: none;
  z-index: 1;
}

.mobile-search input {
  width: 100%;
  padding: 12px 12px 12px 2.5rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  color: var(--text-primary);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  -webkit-appearance: none;
  appearance: none;
  font-family: var(--font-sans);
}

.mobile-search input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-light);
}

/* ==================== Mobile Nav Links -- Staggered Reveal ==================== */
.mobile-nav-section {
  display: flex;
  flex-direction: column;
}

.mobile-nav-link {
  --stagger: 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  padding: 14px var(--spacing-3);
  font-size: var(--text-base);
  font-weight: 500;
  color: var(--text-primary);
  text-decoration: none;
  background: none;
  width: 100%;
  border: none;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;
  border-radius: var(--radius-lg);
  min-height: 48px;
  font-family: var(--font-sans);

  /* Stagger animation */
  opacity: 0;
  transform: translateY(10px);
  animation: stagger-in 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: calc(var(--stagger) * 50ms + 60ms);
}

@keyframes stagger-in {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.mobile-nav-link svg {
  opacity: 0.4;
  flex-shrink: 0;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.mobile-nav-link:hover {
  background: var(--accent-light);
  color: var(--accent);
}

.mobile-nav-link:hover svg {
  opacity: 1;
  transform: scale(1.06);
}

.mobile-nav-link.danger {
  color: var(--error);
}

.mobile-nav-link.danger:hover {
  background: var(--error-light);
}

.mobile-badge {
  margin-left: auto;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 0.6875rem;
  font-weight: 700;
  color: var(--text-inverse);
  background: var(--error);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-divider {
  height: 1px;
  background: var(--border-light);
  margin: var(--spacing-2) 0;
}

/* ==================== Transitions ==================== */

/* mobile overlay fade */
.mobile-overlay-enter-active {
  transition: opacity 0.25s ease;
}

.mobile-overlay-leave-active {
  transition: opacity 0.2s ease;
}

.mobile-overlay-enter-from,
.mobile-overlay-leave-to {
  opacity: 0;
}

/* mobile menu panel */
.mobile-menu-panel-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.mobile-menu-panel-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.mobile-menu-panel-enter-from {
  opacity: 0;
  transform: translateY(-16px) scale(0.96);
}

.mobile-menu-panel-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.98);
}

/* ==================== Responsive ==================== */
@media (max-width: 768px) {
  .navbar {
    top: 10px;
    width: calc(100% - 16px);
    height: 48px;
  }

  .navbar-container {
    padding: 0 14px;
  }

  .navbar-nav {
    display: none;
  }

  .navbar-search {
    display: none;
  }

  .dropdown-menu {
    position: fixed;
    top: auto;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    max-height: 70vh;
    overflow-y: auto;
    border-radius: var(--radius-2xl) var(--radius-2xl) 0 0;
    border-left: none;
    border-right: none;
    border-bottom: none;
    z-index: calc(var(--z-sticky) + 2);
  }

  .dropdown-header {
    padding: var(--spacing-lg);
  }

  .dropdown-item {
    padding: var(--spacing-md) var(--spacing-lg);
    font-size: var(--text-base);
    min-height: 48px;
  }
}

@media (max-width: 480px) {
  .navbar-brand .brand-text {
    font-size: 0.9375rem;
  }

  .mobile-menu {
    width: calc(100% - 24px);
    padding: var(--spacing-4);
  }
}
</style>
