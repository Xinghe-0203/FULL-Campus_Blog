<template>
  <div id="campus-blog" :class="{ 'dark-mode': isDarkMode }">
    <Navbar v-if="!isAuthPage" />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <component :is="Component" :key="$route.path" />
      </router-view>
    </main>
    <Footer v-if="!isAuthPage && !isAdminPage" />
    <BackToTop />
    <Toast />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from './stores/user'
import { useThemeStore } from './stores/theme'
import { destroyLogger } from './utils/logger'
import Navbar from './components/layout/Navbar.vue'
import Footer from './components/layout/Footer.vue'
import BackToTop from './components/common/BackToTop.vue'
import Toast from './components/common/Toast.vue'

const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()

const isDarkMode = computed(() => themeStore.isDarkMode)
const isAuthPage = computed(() => ['Login', 'Register', 'PasswordReset'].includes(route.name))
const isAdminPage = computed(() => route.path.startsWith('/admin'))

userStore.restoreFromStorage()

onMounted(async () => {
  themeStore.initTheme()
})

onUnmounted(() => {
  themeStore.destroyTheme()
  destroyLogger()
})
</script>

<style>
.main-content {
  min-height: calc(100vh - 60px);
  padding-top: 60px;
}
</style>
