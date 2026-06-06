<template>
  <div id="campus-blog" :class="{ 'dark-mode': isDarkMode }">
    <a href="#main-content" class="skip-link">跳转到主要内容</a>
    <Navbar v-if="!isAuthPage" />
    <main id="main-content" class="main-content">
      <router-view v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <div :key="$route.path" style="min-height: calc(100vh - 60px);">
            <component :is="Component" />
          </div>
        </Transition>
      </router-view>
    </main>
    <Footer v-if="!isAuthPage && !isAdminPage" />
    <BackToTop />
    <Toast />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { destroyLogger } from '@/utils/logger'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import BackToTop from '@/components/common/BackToTop.vue'
import Toast from '@/components/common/Toast.vue'

const route = useRoute()
const themeStore = useThemeStore()

const isDarkMode = computed(() => themeStore.isDarkMode)
const isAuthPage = computed(() => ['Login', 'Register', 'PasswordReset'].includes(route.name as string))
const isAdminPage = computed(() => route.path.startsWith('/admin'))

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
  min-height: calc(100vh - var(--navbar-height));
  padding-top: var(--navbar-height);
}

.skip-link {
  position: absolute;
  top: -40px;
  left: 0;
  background: var(--primary);
  color: white;
  padding: 8px 16px;
  z-index: 10000;
  transition: top 0.3s;
}
.skip-link:focus {
  top: 0;
}

.page-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-leave-active {
  transition: opacity 0.15s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-leave-to {
  opacity: 0;
}
</style>
