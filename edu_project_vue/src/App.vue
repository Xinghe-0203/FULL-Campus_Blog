<template>
  <div id="campus-blog" :class="{ 'dark-mode': isDarkMode }">
    <Navbar v-if="!isAuthPage" />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in"
          @before-leave="onBeforeLeave"
          @after-enter="onAfterEnter"
          @enter-cancelled="onEnterCancelled"
          @leave-cancelled="onLeaveCancelled">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
      <div v-if="routeLoading" class="route-loading-overlay">
        <div class="route-spinner"></div>
        <p class="route-loading-text">加载中...</p>
      </div>
      <div v-if="routeError" class="route-error-overlay">
        <p>{{ routeError }}</p>
        <button @click="retryRoute">重试</button>
      </div>
    </main>
    <Footer v-if="!isAuthPage && !isAdminPage" />
    <BackToTop />
    <Toast />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, onErrorCaptured } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { useThemeStore } from './stores/theme'
import { useLogger, destroyLogger } from './utils/logger'
import Navbar from './components/layout/Navbar.vue'
import Footer from './components/layout/Footer.vue'
import BackToTop from './components/common/BackToTop.vue'
import Toast from './components/common/Toast.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const logger = useLogger('App')
const routeLoading = ref(false)
const routeError = ref(null)
let leaveTimeout = null

// 计算属性
const isDarkMode = computed(() => themeStore.isDarkMode)
const isAuthPage = computed(() => ['Login', 'Register', 'PasswordReset'].includes(route.name))
const isAdminPage = computed(() => route.path.startsWith('/admin'))

// 在组件初始化时立即恢复用户状态（早于路由守卫执行）
userStore.restoreFromStorage()

onErrorCaptured((err, instance, info) => {
  routeLoading.value = false
  routeError.value = '页面加载失败，请重试'
  logger.error('Route component error', { error: err.message, info })
  return false
})

function onBeforeLeave() {
  routeLoading.value = true
  window.scrollTo({ top: 0, behavior: 'instant' })
}

function onAfterEnter() {
  leaveTimeout = setTimeout(() => {
    routeLoading.value = false
  }, 200)
}

function onEnterCancelled() {
  routeLoading.value = false
}

function onLeaveCancelled() {
  routeLoading.value = false
}

function retryRoute() {
  routeError.value = null
  routeLoading.value = true
  const currentPath = route.fullPath
  router.replace('/').then(() => router.replace(currentPath))
}

// 初始化
onMounted(async () => {
  themeStore.initTheme()
})

// 清理
onUnmounted(() => {
  themeStore.destroyTheme()
  destroyLogger()
  if (leaveTimeout) clearTimeout(leaveTimeout)
})
</script>

<style>
/* 页面过渡动画 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.route-loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--background);
  z-index: 9998;
}

.route-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: route-loading-spin 0.8s linear infinite;
}

.route-loading-text {
  margin-top: 16px;
  color: var(--text-secondary);
  font-size: 14px;
}

.route-error-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--background);
  z-index: 9998;
  gap: 16px;
}

.route-error-overlay p {
  color: var(--text-primary);
  font-size: 16px;
}

.route-error-overlay button {
  padding: 10px 24px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.route-error-overlay button:hover {
  background: var(--primary-hover);
}

@keyframes route-loading-spin {
  to { transform: rotate(360deg); }
}

.main-content {
  min-height: calc(100vh - 60px);
  padding-top: 60px;
}
</style>
