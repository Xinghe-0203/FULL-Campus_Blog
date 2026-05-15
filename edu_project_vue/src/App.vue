<template>
  <div id="campus-blog" :class="{ 'dark-mode': isDarkMode }">
    <Navbar v-if="!isAuthPage" />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in" @before-leave="onBeforeLeave" @after-enter="onAfterEnter">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
      <div v-if="routeLoading" class="route-loader">
        <div class="route-spinner"></div>
      </div>
    </main>
    <Footer v-if="!isAuthPage && !isAdminPage" />
    <BackToTop />
    <Toast />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { useThemeStore } from './stores/theme'
import { destroyLogger } from './utils/logger'
import Navbar from './components/layout/Navbar.vue'
import Footer from './components/layout/Footer.vue'
import BackToTop from './components/common/BackToTop.vue'
import Toast from './components/common/Toast.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const routeLoading = ref(false)
let leaveTimeout = null

// 计算属性
const isDarkMode = computed(() => themeStore.isDarkMode)
const isAuthPage = computed(() => ['Login', 'Register', 'PasswordReset'].includes(route.name))
const isAdminPage = computed(() => route.path.startsWith('/admin'))

// 在组件初始化时立即恢复用户状态（早于路由守卫执行）
userStore.restoreFromStorage()

// 路由加载动画
router.beforeEach((to, from) => {
  routeLoading.value = true
})

router.afterEach(() => {
  leaveTimeout = setTimeout(() => {
    routeLoading.value = false
  }, 300)
})

const onBeforeLeave = () => {
  routeLoading.value = true
}

const onAfterEnter = () => {
  leaveTimeout = setTimeout(() => {
    routeLoading.value = false
  }, 200)
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

.route-loader {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  height: 3px;
  pointer-events: none;
}

.route-spinner {
  height: 100%;
  background: linear-gradient(90deg, #4f46e5, #6366f1, #818cf8);
  background-size: 200% 100%;
  animation: route-loading 1.2s ease infinite;
  border-radius: 0 2px 2px 0;
}

@keyframes route-loading {
  0% { width: 0%; margin-left: 0; }
  50% { width: 70%; margin-left: 30%; }
  100% { width: 0%; margin-left: 100%; }
}

.main-content {
  min-height: calc(100vh - 60px);
  padding-top: 60px;
}

@media (min-width: 1024px) {
  .main-content {
    padding-top: 64px;
  }
}

@media (min-width: 1200px) {
  .main-content {
    padding-top: 68px;
  }
}
</style>
